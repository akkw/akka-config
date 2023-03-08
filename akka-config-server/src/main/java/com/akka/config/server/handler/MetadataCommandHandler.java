package com.akka.config.server.handler;/* 
    create qiangzhiwei time 2023/2/10
 */

import com.akka.config.ha.etcd.EtcdClient;
import com.akka.config.protocol.*;
import com.akka.config.server.core.MetadataManager;
import com.akka.remoting.protocol.Command;
import com.alibaba.fastjson.JSON;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MetadataCommandHandler extends AbstractCommandHandler {

    private final MetadataManager metadataManager;

    public MetadataCommandHandler(EtcdClient etcdClient, MetadataManager metadataManager) {
        super(etcdClient);
        this.metadataManager = metadataManager;
    }

    // TODO 验证数据库是否存在这个版本
    @Override
    public CompletableFuture<Response> commandHandler(Command command) throws ExecutionException, InterruptedException {
        final MetadataRequest metadataRequest = JSON.parseObject(command.getBody(), MetadataRequest.class);
        final String namespace = metadataRequest.getNamespace();
        final String environment = metadataRequest.getEnvironment();
        final Metadata etcdMetadata = getEtcdMetadata(namespace, environment);

        final MetadataResponse metadataResponse = new MetadataResponse();

        if (etcdMetadata == null) {
            fillResponse(metadataResponse, ResponseCode.METADATA_NOT_EXIST);
            return CompletableFuture.completedFuture(metadataResponse);
        }

        final Metadata.ClientVersion activateVersion = etcdMetadata.getActivateVersions().
                stream().collect(Collectors.toMap(Metadata.ClientVersion::getClient, Function.identity())).get(metadataRequest.getClientIp());
        if (activateVersion != null) {
            metadataResponse.setActivateVersion(activateVersion.getVersion());
        }
        final Metadata.ClientVersion verifyVersion = etcdMetadata.getVerifyVersions().
                stream().collect(Collectors.toMap(Metadata.ClientVersion::getClient, Function.identity())).get(metadataRequest.getClientIp());
        if (verifyVersion != null) {
            metadataResponse.setVerifyVersion(verifyVersion.getVersion());
        }
        metadataResponse.setNamespace(namespace);
        metadataResponse.setEnvironment(environment);
        metadataResponse.setGlobalVersion(etcdMetadata.getGlobalVersion());
        return CompletableFuture.completedFuture(metadataResponse);
    }
}
