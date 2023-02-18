package com.akka.config.server.handler;/* 
    create qiangzhiwei time 2023/2/14
 */

import com.akka.config.ha.common.PathUtils;
import com.akka.config.ha.etcd.EtcdClient;
import com.akka.config.protocol.*;
import com.akka.remoting.protocol.Command;
import com.alibaba.fastjson.JSON;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class CreateNamespaceHandler extends AbstractCommandHandler {

    public CreateNamespaceHandler(EtcdClient etcdClient) {
        this.etcdClient = etcdClient;
    }

    @Override
    public CompletableFuture<Response> commandHandler(Command command) throws ExecutionException, InterruptedException {
        final CreateNamespaceRequest request = JSON.parseObject(command.getBody(), CreateNamespaceRequest.class);
        final String namespace = request.getNamespace();
        final String environment = request.getEnvironment();
        final String environmentPatch = PathUtils.createEnvironmentPatch(etcdClient.getConfig().getPathConfig(), namespace, environment);
        final Metadata metadata = new Metadata();
        metadata.setNamespace(namespace);
        metadata.setEnvironment(environment);
        metadata.setGlobalVersion(-1);
        metadata.setMaxVersion(-1);
        metadata.setActivateVersions(new HashMap<>());
        metadata.setVerifyVersions(new HashMap<>());
        final boolean createResult = etcdClient.putIfAbsent(environmentPatch, JSON.toJSONString(metadata));

        final CreateNamespaceResponse createNamespaceResponse = createResult ? new CreateNamespaceResponse() :
                new CreateNamespaceResponse(ResponseCode.NAMESPACE_EXIST.code(), ResponseCode.NAMESPACE_EXIST.getDesc().getBytes(StandardCharsets.UTF_8));

        return CompletableFuture.completedFuture(createNamespaceResponse);

    }
}
