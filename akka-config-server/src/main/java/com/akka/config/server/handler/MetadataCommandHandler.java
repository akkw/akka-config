package com.akka.config.server.handler;/* 
    create qiangzhiwei time 2023/2/10
 */

import com.akka.config.ha.etcd.EtcdClient;
import com.akka.config.protocol.Metadata;
import com.akka.config.protocol.MetadataRequest;
import com.akka.config.protocol.MetadataResponse;
import com.akka.config.protocol.Response;
import com.akka.config.server.core.MetadataManager;
import com.akka.remoting.protocol.Command;
import com.alibaba.fastjson.JSON;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class MetadataCommandHandler extends AbstractCommandHandler {

    private final EtcdClient etcdClient;
    private final MetadataManager metadataManager;

    public MetadataCommandHandler(EtcdClient etcdClient, MetadataManager metadataManager) {
        this.etcdClient = etcdClient;
        this.metadataManager = metadataManager;
    }


    @Override
    public CompletableFuture<Response> commandHandler(Command command) throws ExecutionException, InterruptedException {
        final MetadataRequest metadataRequest = JSON.parseObject(command.getBody(), MetadataRequest.class);
        final String namespace = metadataRequest.getNamespace();
        final String environment = metadataRequest.getEnvironment();
        final Metadata etcdMetadata = getEtcdMetadata(namespace, environment);

        final MetadataResponse metadataResponse = new MetadataResponse();
        final Metadata.ClientVersion activateVersion = etcdMetadata.getActivateVersions().get(metadataRequest.getClientIp());
        if (activateVersion != null) {
            metadataResponse.setActivateVersion(activateVersion.getVersion());
        }
        final Metadata.ClientVersion verifyVersion = etcdMetadata.getVerifyVersions().get(metadataRequest.getClientIp());
        if (verifyVersion != null) {
            metadataResponse.setVerifyVersion(verifyVersion.getVersion());
        }
        metadataResponse.setNamespace(namespace);
        metadataResponse.setEnvironment(environment);
        metadataResponse.setGlobalVersion(etcdMetadata.getGlobalVersion());
        return CompletableFuture.completedFuture(metadataResponse);
    }
}
