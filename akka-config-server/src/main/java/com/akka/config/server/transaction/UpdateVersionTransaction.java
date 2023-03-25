package com.akka.config.server.transaction;

import com.akka.config.ha.common.PathUtils;
import com.akka.config.ha.etcd.EtcdClient;
import com.akka.config.protocol.Metadata;
import com.akka.tools.retry.Retry;
import com.alibaba.fastjson2.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class UpdateVersionTransaction extends Transaction {

    private final static Logger logger = LoggerFactory.getLogger(CreateConfigTransaction.class);

    private final Integer verison;

    private final String clientIp;

    private final List<Metadata.ClientVersion> clientVersionList;

    private final TransactionKind transactionKind;
    public UpdateVersionTransaction(EtcdClient etcdClient, long transactionId, String lockKey, String namespace,
                                    String environment, Integer version, String clientIp,
                                    List<Metadata.ClientVersion> clientVersionList, TransactionKind transactionKind) {

        super(etcdClient, transactionId, lockKey, namespace, environment);
        this.verison = version;
        this.clientIp = clientIp;
        this.clientVersionList = clientVersionList;
        this.transactionKind = transactionKind;
    }

    @Override
    void rollback(TransactionUndoLog transactionUndoLog, boolean prev) {
        if (undoLogWriteSuccess || prev) {
            try {
                etcdClient.put(PathUtils.createEnvironmentPath(etcdConfig.getPathConfig(), namespace, environment), JSON.toJSONString(transactionUndoLog.getMetadata()));
            } catch (Exception ignored) {}
        }
    }

    @Override
    void undoLog() throws ExecutionException, InterruptedException, TimeoutException {
        writeUndoLog();
        undoLogWriteSuccess = true;
    }

    private void writeUndoLog() throws ExecutionException, InterruptedException, TimeoutException {
        transactionUndoLog = new TransactionUndoLog(etcdMetadata, beginTimeMs);
        final String undoLogPath = PathUtils.createUndoLogPath(etcdConfig.getPathConfig(), namespace, environment, TransactionKey.METADATA.name());
        etcdClient.put(undoLogPath, JSON.toJSONString(transactionUndoLog));
    }

    @Override
    void transaction() throws Exception {

        checkMetadataNoPass(verison, clientVersionList);

        if (transactionKind == TransactionKind.ACTIVATE_VERSION) {
            clearUpClientVersion(verison, etcdMetadata.getActivateVersions(), clientVersionList);
            etcdMetadata.setGlobalVersion(verison);
        } else if (transactionKind == TransactionKind.VERIFY_VERSION){
            clearUpClientVersion(verison, etcdMetadata.getVerifyVersions(), clientVersionList);
            etcdMetadata.setVerifyVersion(verison);
        }
        Retry.retry(UpdateVersionTransaction.this::writeMetadata);
    }

    private void writeMetadata() throws ExecutionException, InterruptedException, TimeoutException {
        writeEtcdMetadata(etcdMetadata);
    }


}
