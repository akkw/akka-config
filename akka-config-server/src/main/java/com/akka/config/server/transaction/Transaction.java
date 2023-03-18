package com.akka.config.server.transaction;/* 
    create qiangzhiwei time 2023/3/17
 */

import com.akka.config.ha.common.PathUtils;
import com.akka.config.ha.etcd.EtcdClient;
import com.akka.config.ha.etcd.EtcdConfig;
import com.akka.config.protocol.Metadata;
import com.alibaba.fastjson2.JSON;
import javafx.util.Pair;

import java.util.concurrent.ExecutionException;

public abstract class Transaction {
    private String transactionId;

    private String lockKey;

    protected Exception exception;

    protected final EtcdClient etcdClient;

    protected final EtcdConfig etcdConfig;


    protected final Once once = Once.create();

    protected final Once rollbackOnce = Once.create();
    public Transaction(EtcdClient etcdClient, String transactionId, String lockKey) {
        this.etcdClient = etcdClient;
        this.etcdConfig = etcdClient == null ? null : etcdClient.getConfig();
        this.transactionId = transactionId;
        this.lockKey = lockKey;
    }

    public String getTransactionId() {
        return transactionId;
    }
    public abstract void executor();

    abstract Exception exception();

    abstract void await() throws InterruptedException;

    public Exception getException() {
        return exception;
    }

    public String getLockKey() {
        return lockKey;
    }

    public void setLockKey(String lockKey) {
        this.lockKey = lockKey;
    }

    protected Metadata metadata(String namespace, String environment) throws ExecutionException, InterruptedException {
        final Pair<String, String> etcdMetadataPair = etcdClient.get(
                PathUtils.createEnvironmentPath(etcdConfig.getPathConfig(), namespace, environment));
        return JSON.parseObject(etcdMetadataPair.getValue(), Metadata.class);
    }
}
