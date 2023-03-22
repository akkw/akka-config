package com.akka.config.server.transaction;/* 
    create qiangzhiwei time 2023/3/17
 */

import com.akka.config.ha.common.PathUtils;
import com.akka.config.ha.etcd.EtcdClient;
import com.akka.config.ha.etcd.EtcdConfig;
import com.akka.config.protocol.*;
import com.akka.config.server.exception.IllegalVersionException;
import com.akka.tools.retry.Retry;
import com.alibaba.fastjson2.JSON;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

public abstract class Transaction {

    private final static Logger logger = LoggerFactory.getLogger(Transaction.class);

    private String transactionId;

    private String lockKey;

    protected Exception exception;

    protected final EtcdClient etcdClient;
    protected final EtcdConfig etcdConfig;

    protected final String namespace;

    protected final String environment;
    protected final Once once = Once.create();
    protected volatile boolean undoLogWriteSuccess;
    protected Metadata etcdMetadata;
    protected final Thread worker;

    protected final CountDownLatch countDownLatch;

    protected long beginTimeMs;

    protected TransactionUndoLog transactionUndoLog;

    public Transaction(EtcdClient etcdClient, String transactionId, String lockKey, String namespace, String environment) {
        this.etcdClient = etcdClient;
        this.etcdConfig = etcdClient == null ? null : etcdClient.getConfig();
        this.transactionId = transactionId;
        this.lockKey = lockKey;
        this.namespace = namespace;
        this.environment = environment;
        this.worker = new TransactionThread(namespace + "_" + environment);
        this.countDownLatch = new CountDownLatch(1);
    }

    public String getTransactionId() {
        return transactionId;
    }

    public Exception getException() {
        return exception;
    }

    public String getLockKey() {
        return lockKey;
    }

    public void setLockKey(String lockKey) {
        this.lockKey = lockKey;
    }


    abstract void rollback(TransactionUndoLog transactionUndoLog, boolean prev) throws ExecutionException, InterruptedException, SQLException;

    abstract void undoLog() throws ExecutionException, InterruptedException;

    protected void prepare() throws ExecutionException, InterruptedException, SQLException {
        checkNoPaas();

        etcdMetadata = metadata(namespace, environment);
        if (etcdMetadata == null) {
            throw new IllegalArgumentException("etcd metadata does not exist");
        }

        final String undoLogPath = PathUtils.createUndoLogPath(etcdConfig.getPathConfig(), namespace, environment, TransactionKey.METADATA.name());
        final Pair<String, String> etcdMetadataPair = etcdClient.get(undoLogPath);
        if (etcdMetadataPair != null) {
            transactionUndoLog = JSON.parseObject(etcdMetadataPair.getValue(), TransactionUndoLog.class);
            rollback(transactionUndoLog, true);
        }
    }

    protected void down() {
        if (once.isDown()) {
            return;
        }
        try {
            deleteUndoLog();
        } catch (Exception ignored) {
        }
        once.down();
        countDownLatch.countDown();
    }

    public void executor() {
        if (once.isDown()) {
            throw new RuntimeException(String.format("The transaction has been executed, namespace: %s, environment: %s", namespace, environment));
        }
        this.beginTimeMs = System.currentTimeMillis();
        this.worker.start();
    }

    void await() throws InterruptedException {
        countDownLatch.await();
    }


    public Exception exception() {
        return exception;
    }

    private void deleteUndoLog() throws ExecutionException, InterruptedException {
        final String undoLogPath = PathUtils.createUndoLogPath(etcdConfig.getPathConfig(), namespace, environment, TransactionKey.METADATA.name());
        etcdClient.del(undoLogPath);
    }

    private void checkNoPaas() {
        if (namespace == null || "".equals(namespace)) {
            throw new IllegalArgumentException("namespace is null");
        }

        if (environment == null || "".equals(environment)) {
            throw new IllegalArgumentException("environment is null");
        }

        if (etcdClient == null) {
            throw new IllegalArgumentException("internal error");
        }
    }


    protected Metadata metadata(String namespace, String environment) throws ExecutionException, InterruptedException {
        final Pair<String, String> etcdMetadataPair = etcdClient.get(
                PathUtils.createEnvironmentPath(etcdConfig.getPathConfig(), namespace, environment));
        return JSON.parseObject(etcdMetadataPair.getValue(), Metadata.class);
    }

    protected void writeEtcdMetadata(Metadata metadata) throws ExecutionException, InterruptedException {
        etcdClient.put(PathUtils.createEnvironmentPath(etcdConfig.getPathConfig(), namespace, environment), JSON.toJSONString(metadata));
    }

    protected void checkMetadataNoPass(Integer version, List<Metadata.ClientVersion> reqActivateVersionList) {
        if (version == null || reqActivateVersionList == null) {
            return;
        }

        final int maxVersion = etcdMetadata.getMaxVersion();

        for (Metadata.ClientVersion reqClientVersion: reqActivateVersionList) {
            checkVersion(reqClientVersion.getVersion(), maxVersion);
        }
    }

    protected void checkVersion(Integer version, Integer maxVersion) {
        if (version < 0 || version > maxVersion) {
            throw new IllegalVersionException("The version verification fails.");
        }
    }


    protected void clearUpClientVersion(Integer reqVersion, Set<Metadata.ClientVersion> metadataVerifyVersionsSet, List<Metadata.ClientVersion> requestVersionList) {
        if (requestVersionList == null || requestVersionList.isEmpty()) {
            return;
        }

        doClearUpClientVersion(reqVersion, metadataVerifyVersionsSet, requestVersionList);
    }

    private void doClearUpClientVersion(Integer reqVersion, Set<Metadata.ClientVersion> metadataVerifyVersionsSet, List<Metadata.ClientVersion> requestVersionList) {
        if (reqVersion != null) {
            requestVersionList.removeIf(reqClientVersion -> reqVersion.equals(reqClientVersion.getVersion()));
            metadataVerifyVersionsSet.removeIf(metadataClientVersion -> reqVersion.equals(metadataClientVersion.getVersion()));
        }
        for (Metadata.ClientVersion clientVersion: requestVersionList) {
            metadataVerifyVersionsSet.remove(clientVersion);
        }
        metadataVerifyVersionsSet.addAll(requestVersionList);
    }


    abstract void transaction() throws Exception;
    private class TransactionThread extends Thread {
        public TransactionThread(String name) {
            super(name);
        }

        @Override
        public void run() {

            try {
                prepare();
                Retry.retry(Transaction.this::undoLog);
                transaction();
            } catch (Exception e) {
                try {
                    rollback(transactionUndoLog, false);
                } catch (Exception exc) {
                    logger.error("Transaction rollback failed, namespace: {}, environment: {}", namespace, environment, exc);
                }
                Transaction.this.exception = e;
            } finally {
                down();
            }


        }
    }

}
