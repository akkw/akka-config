package com.akka.config.server.handler;/* 
    create qiangzhiwei time 2023/2/11
 */

import com.akka.config.ha.common.PathUtils;
import com.akka.config.ha.etcd.EtcdClient;
import com.akka.config.handler.CommandHandler;
import com.akka.config.protocol.*;
import com.alibaba.fastjson2.JSON;
import javafx.util.Pair;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
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
                PathUtils.createEnvironmentPath(etcdClient.getConfig().getPathConfig(), namespace, environment));
        return JSON.parseObject(metadataPair != null ? metadataPair.getValue() : null, Metadata.class);
    }

    protected Response checkRequest(Request request) {
        final String namespace = request.getNamespace();
        final String environment = request.getEnvironment();
        if (namespace == null || "".equals(namespace.trim())) {
            return new CreateConfigResponse(ResponseCode.CONFIG_CREATE_ERROR.code(), "namespace is blank".getBytes(StandardCharsets.UTF_8));
        }
        if (environment == null || "".equals(environment.trim())) {
            return new CreateConfigResponse(ResponseCode.CONFIG_CREATE_ERROR.code(), "environment is blank".getBytes(StandardCharsets.UTF_8));
        }
        return null;
    }
}
