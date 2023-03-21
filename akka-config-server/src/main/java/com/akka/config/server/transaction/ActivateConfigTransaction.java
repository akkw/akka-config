package com.akka.config.server.transaction;

import com.akka.config.ha.etcd.EtcdClient;
import com.akka.config.protocol.Metadata;

import java.util.List;

public class ActivateConfigTransaction extends Transaction {

    private final Integer verison;

    private final String clientIp;

    private final List<Metadata.ClientVersion> activateVersionList;


    public ActivateConfigTransaction(EtcdClient etcdClient, String transactionId, String lockKey, String namespace,
                                     String environment, Integer version, String clientIp,
                                     List<Metadata.ClientVersion> activateVersionList) {

        super(etcdClient, transactionId, lockKey, namespace, environment);
        this.verison = version;
        this.clientIp = clientIp;
        this.activateVersionList = activateVersionList;

    }

    @Override
    public void executor() {

    }

    @Override
    public Exception exception() {
        return null;
    }

    @Override
    void await() throws InterruptedException {

    }

    @Override
    public Thread newThread() {
        return null;
    }





}
