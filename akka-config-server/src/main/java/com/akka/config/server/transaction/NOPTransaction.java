package com.akka.config.server.transaction;/* 
    create qiangzhiwei time 2023/3/18
 */

import com.akka.config.ha.etcd.EtcdClient;

public class NOPTransaction extends Transaction {
    private final Exception exception;

    public NOPTransaction(EtcdClient etcdClient, Exception exception) {
        super(null, null, null);
        this.exception = exception;
    }

    @Override
    public void executor() {
    }

    @Override
    Exception exception() {
        return exception;
    }

    @Override
    void await() throws InterruptedException {

    }
}
