package com.akka.config.server.transaction;/* 
    create qiangzhiwei time 2023/3/17
 */

import com.akka.config.ha.common.PathUtils;
import com.akka.config.ha.etcd.EtcdClient;
import com.akka.config.ha.etcd.EtcdConfig;
import com.akka.config.protocol.*;
import com.alibaba.fastjson2.JSON;
import javafx.util.Pair;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public abstract class Transaction {
    private String transactionId;

    private String lockKey;

    protected Exception exception;

    protected final EtcdClient etcdClient;
    protected final EtcdConfig etcdConfig;

    protected final String namespace;

    protected final String environment;
    protected final Once once = Once.create();
    protected volatile boolean undoLogWriteSuccess = false;

    protected final Thread worker;

    public Transaction(EtcdClient etcdClient, String transactionId, String lockKey, String namespace, String environment) {
        this.etcdClient = etcdClient;
        this.etcdConfig = etcdClient == null ? null : etcdClient.getConfig();
        this.transactionId = transactionId;
        this.lockKey = lockKey;
        this.namespace = namespace;
        this.environment = environment;
        this.worker = newThread();
    }

    public String getTransactionId() {
        return transactionId;
    }
    public abstract void executor();

    public abstract Exception exception();

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

    public abstract Thread newThread();

    protected Metadata metadata(String namespace, String environment) throws ExecutionException, InterruptedException {
        final Pair<String, String> etcdMetadataPair = etcdClient.get(
                PathUtils.createEnvironmentPath(etcdConfig.getPathConfig(), namespace, environment));
        return JSON.parseObject(etcdMetadataPair.getValue(), Metadata.class);
    }


    private void checkMetadataNoPass(Metadata metadata, ActivateConfigRequest request) {
        final Integer version = request.getVersion();
        final List<Metadata.ClientVersion> reqActivateVersionList = request.getActivateVersionList();
        if (version == null || reqActivateVersionList == null) {
            return;
        }

        final int maxVersion = metadata.getMaxVersion();

        for (Metadata.ClientVersion reqClientVersion: reqActivateVersionList) {
            checkVersion(reqClientVersion.getVersion(), maxVersion);
        }
    }

    protected void checkVersion(Integer version, Integer maxVersion) {
        if (version < 0 || version > maxVersion) {
            return;
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
}
