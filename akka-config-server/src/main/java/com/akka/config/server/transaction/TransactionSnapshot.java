package com.akka.config.server.transaction;/* 
    create qiangzhiwei time 2023/3/17
 */

import com.akka.config.ha.etcd.EtcdClient;
import com.akka.config.ha.etcd.EtcdConfig;

public abstract class TransactionSnapshot {
    protected final EtcdClient etcdClient;

    protected final EtcdConfig etcdConfig;
    public TransactionSnapshot(EtcdClient etcdClient) {
        this.etcdClient = etcdClient;
        this.etcdConfig = etcdClient.getConfig();
    }


    abstract void start();
}
