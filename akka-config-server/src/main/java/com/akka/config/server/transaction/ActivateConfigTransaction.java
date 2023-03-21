package com.akka.config.server.transaction;

import com.akka.config.ha.etcd.EtcdClient;

public class ActivateConfigTransaction extends Transaction {


    public ActivateConfigTransaction(EtcdClient etcdClient, String transactionId, String lockKey, String namespace, String environment) {
        super(etcdClient, transactionId, lockKey, namespace, environment);
    }

    @Override
    public void executor() {

    }

    @Override
    Exception exception() {
        return null;
    }

    @Override
    void await() throws InterruptedException {

    }
}
