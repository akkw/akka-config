package com.akka.config.server.transaction;

import com.akka.config.ha.etcd.EtcdClient;
import com.akka.config.protocol.Metadata;

import java.util.List;

public class VerifyConfigTransaction extends Transaction {

    private final Integer version;

    private final String clientIp;

    private final List<Metadata.ClientVersion> verifyVersionList;

    public VerifyConfigTransaction(EtcdClient etcdClient, String transactionId, String lockKey, String namespace,
                                   String environment, Integer version, String clientIp,
                                   List<Metadata.ClientVersion> verifyVersionList) {

        super(etcdClient, transactionId, lockKey, namespace, environment);
        this.version = version;
        this.clientIp = clientIp;
        this.verifyVersionList = verifyVersionList;
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
