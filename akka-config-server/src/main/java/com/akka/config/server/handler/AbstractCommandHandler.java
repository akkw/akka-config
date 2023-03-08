package com.akka.config.server.handler;/* 
    create qiangzhiwei time 2023/2/11
 */

import com.akka.config.ha.common.PathUtils;
import com.akka.config.ha.etcd.EtcdClient;
import com.akka.config.handler.CommandHandler;
import com.akka.config.protocol.Metadata;
import com.alibaba.fastjson2.JSON;
import javafx.util.Pair;

import java.util.concurrent.ExecutionException;

abstract class AbstractCommandHandler implements CommandHandler {

    protected EtcdClient etcdClient;

    public AbstractCommandHandler() {
    }

    public AbstractCommandHandler(EtcdClient etcdClient) {
        this.etcdClient = etcdClient;
    }

    protected Metadata getEtcdMetadata(String namespace, String environment) throws ExecutionException, InterruptedException {
        final Pair<String, String> metadataPair = etcdClient.get(
                PathUtils.createEnvironmentPatch(etcdClient.getConfig().getPathConfig(), namespace, environment));
        return JSON.parseObject(metadataPair != null ? metadataPair.getValue() : null, Metadata.class);
    }
}
