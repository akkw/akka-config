package com.akka.config.server.transaction;/*
    create qiangzhiwei time 2023/3/16
 */

import com.akka.config.ha.common.PathUtils;
import com.akka.config.ha.etcd.EtcdClient;
import com.akka.config.ha.etcd.EtcdConfig;
import com.akka.config.store.Store;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public Transaction begin(String namespace, String environment, byte[] contents, TransactionKind transactionKind) {
        final String transactionId = getTransactionId(namespace, environment, transactionKind);
        final Transaction checkNoPaas = checkNoPaas(namespace, environment, transactionKind, contents);
        if (checkNoPaas != null) {
            transactionSnapshotMap.putIfAbsent(transactionId, checkNoPaas);
            return checkNoPaas;
        }


        final String lockPath = PathUtils.createLockPath(etcdConfig.getPathConfig(), namespace, environment, transactionKind.name());
        String lockKey;
        try {
            lockKey = etcdClient.lock(lockPath, 500);
        } catch (Exception e) {
            logger.error("create transaction failed, namespace: {}, environment: {}, transactionKind: {},", namespace, environment, transactionKind);
            return new NOPTransaction(null, e);
        }

        final CreateConfigTransaction transactionSnapshot = new CreateConfigTransaction(namespace, environment, contents, etcdClient, store, transactionId, lockKey);
        transactionSnapshotMap.putIfAbsent(transactionId, transactionSnapshot);
        return transactionSnapshot;
    }

    private Transaction checkNoPaas(String namespace, String environment, TransactionKind transactionKind, byte[] contents) {
        if (namespace == null || "".equals(namespace)) {
            return new NOPTransaction(null, new IllegalArgumentException("namespace is null"));
        }

        if (environment == null || "".equals(environment)) {
            return new NOPTransaction(null, new IllegalArgumentException("environment is null"));
        }

        if (transactionKind == null) {
            return new NOPTransaction(null, new IllegalArgumentException("transactionKind is null"));
        }

        if (contents == null) {
            return new NOPTransaction(null, new IllegalArgumentException("contents is null"));
        }

        return null;
    }

    private String getTransactionId(String namespace, String environment, TransactionKind transactionKind) {
        return String.format("[%s,%s,%s]", namespace, environment, transactionKind);
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

    public TransactionResult buildTransactionResult(@NonNull Transaction transaction) {
        final TransactionResult result = new TransactionResult();

        final Exception exception = transaction.getException();
        final boolean success = exception == null;
        result.setSuccess(success);
        if (!success) {
            result.setMessage(exception.getMessage());
            result.setException(exception);
        }
        return result;
    }
}
