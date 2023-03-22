package com.akka.config.server.transaction;/*
    create qiangzhiwei time 2023/3/16
 */

import com.akka.config.ha.common.PathUtils;
import com.akka.config.ha.etcd.EtcdClient;
import com.akka.config.ha.etcd.EtcdConfig;
import com.akka.config.protocol.Metadata;
import com.akka.config.server.transaction.protocol.UpdateVersionTransactionSnapshot;
import com.akka.config.server.transaction.protocol.CreateConfigTransactionSnapshot;
import com.akka.config.server.transaction.protocol.TransactionSnapshot;
import com.akka.config.store.Store;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

public class TransactionManager {

    private final static Logger logger = LoggerFactory.getLogger(TransactionManager.class);
    private final Map<String, Transaction> transactionSnapshotMap = new ConcurrentHashMap<>();

    private final EtcdClient etcdClient;

    private final EtcdConfig etcdConfig;

    private final Store store;

    public TransactionManager(EtcdClient etcdClient, Store store) {
        this.store = store;
        this.etcdClient = etcdClient;
        this.etcdConfig = etcdClient.getConfig();
    }

    public Transaction begin(TransactionSnapshot transactionSnapshot, TransactionKind transactionKind) {
        String namespace = transactionSnapshot.getNamespace();
        String environment = transactionSnapshot.getEnvironment();

        final String transactionId = getTransactionId(namespace, environment, transactionKind);
        final Transaction checkNoPaas = checkNoPaas(namespace, environment, transactionKind);
        if (checkNoPaas != null) {
            transactionSnapshotMap.putIfAbsent(transactionId, checkNoPaas);
            return checkNoPaas;
        }

        Transaction transaction = null;
        switch (transactionKind) {
            case CREATE_CONFIG:
            case ACTIVATE_VERSION:
                final String lockPath = PathUtils.createLockPath(etcdConfig.getPathConfig(), namespace, environment, transactionKind.name());
                String lockKey;
                try {
                    lockKey = etcdClient.lock(lockPath, 500);
                } catch (Exception e) {
                    logger.error("create transaction failed, namespace: {}, environment: {}, transactionKind: {},", namespace, environment, transactionKind);
                    return new NOPTransaction(null, e);
                }

                if (transactionKind == TransactionKind.CREATE_CONFIG) {
                    CreateConfigTransactionSnapshot snapshot = (CreateConfigTransactionSnapshot) transactionSnapshot;
                    transaction = new CreateConfigTransaction(namespace, environment, snapshot.getContents(), etcdClient, store, transactionId, lockKey);
                } else {
                    UpdateVersionTransactionSnapshot snapshot = (UpdateVersionTransactionSnapshot) transactionSnapshot;
                    String clientIp = snapshot.getClientIp();
                    Integer version = snapshot.getVersion();
                    List<Metadata.ClientVersion> clientVersionList = snapshot.getClientVersionList();
                    transaction = new UpdateVersionTransaction(etcdClient, transactionId, lockKey, namespace, environment, version, clientIp, clientVersionList, transactionKind);
                }
                transactionSnapshotMap.putIfAbsent(transactionId, transaction);
        }
        return transaction;
    }

    public TransactionResult end(String transactionId) throws InterruptedException, ExecutionException {
        final Transaction transactionSnapshot = transactionSnapshotMap.remove(transactionId);
        if (transactionSnapshot instanceof NOPTransaction) {
            return buildTransactionResult(transactionSnapshot);
        }
        transactionSnapshot.await();
        etcdClient.unlock(transactionSnapshot.getLockKey());
        return buildTransactionResult(transactionSnapshot);
    }

    private Transaction checkNoPaas(String namespace, String environment, TransactionKind transactionKind) {
        if (namespace == null || "".equals(namespace)) {
            return new NOPTransaction(null, new IllegalArgumentException("namespace is null"));
        }

        if (environment == null || "".equals(environment)) {
            return new NOPTransaction(null, new IllegalArgumentException("environment is null"));
        }

        if (transactionKind == null) {
            return new NOPTransaction(null, new IllegalArgumentException("transactionKind is null"));
        }

        return null;
    }

    private String getTransactionId(String namespace, String environment, TransactionKind transactionKind) {
        return String.format("[%s,%s,%s]", namespace, environment, transactionKind);
    }


    public TransactionResult buildTransactionResult(@NonNull Transaction transaction) {
        final TransactionResult result = new TransactionResult();

        final Exception exception = transaction.getException();
        final boolean success = exception == null;
        result.setSuccess(success);
        if (!success) {
            result.setException(exception);
        }
        return result;
    }
}
