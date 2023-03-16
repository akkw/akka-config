package com.akka.config.server.transaction;/*
    create qiangzhiwei time 2023/3/16
 */

import com.akka.config.ha.etcd.EtcdClient;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TransactionManager {

    private final Map<String, CreateConfigTransactionSnapshot> transactionSnapshotMap = new ConcurrentHashMap<>();

    private final EtcdClient etcdClient;

    public TransactionManager(EtcdClient etcdClient) {
        this.etcdClient = etcdClient;
    }

    public CreateConfigTransactionSnapshot begin(String namespace, String environment, byte[] contents, TransactionKind transactionKind) {
        final String transactionId = getTransactionId(namespace, environment, transactionKind);
        final CreateConfigTransactionSnapshot transactionSnapshot = new CreateConfigTransactionSnapshot(namespace, environment, contents, etcdClient);
        transactionSnapshotMap.putIfAbsent(transactionId, transactionSnapshot);
        return transactionSnapshot;
    }

    private String getTransactionId(String namespace, String environment, TransactionKind transactionKind) {
        return String.format("[%s,%s,%s]", namespace, environment, transactionKind);
    }


    public void end(String namespace, String environment, TransactionKind transactionKind) {
        final String transactionId = getTransactionId(namespace, environment, transactionKind);
        final CreateConfigTransactionSnapshot transactionSnapshot = transactionSnapshotMap.remove(transactionId);
        transactionSnapshot.down();
    }



}
