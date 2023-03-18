package com.akka.config.server.handler;/* 
    create qiangzhiwei time 2023/2/10
 */

import com.akka.config.ha.etcd.EtcdClient;
import com.akka.config.protocol.Metadata;
import com.akka.config.protocol.MetadataRequest;
import com.akka.config.protocol.MetadataResponse;
import com.akka.config.protocol.Response;
import com.akka.config.protocol.ResponseCode;
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

        final Metadata.ClientVersion clientActivateVersion = etcdMetadata.getActivateVersions().
                stream().collect(Collectors.toMap(Metadata.ClientVersion::getClient, Function.identity())).get(metadataRequest.getClientIp());
        if (clientActivateVersion != null) {
            metadataResponse.setActivateVersion(clientActivateVersion.getVersion());
        } else {
            metadataResponse.setActivateVersion(etcdMetadata.getGlobalVersion());
        }
        final Metadata.ClientVersion clientVerifyVersion = etcdMetadata.getVerifyVersions().
                stream().collect(Collectors.toMap(Metadata.ClientVersion::getClient, Function.identity())).get(metadataRequest.getClientIp());


        if (clientVerifyVersion != null) {
            metadataResponse.setVerifyVersion(clientVerifyVersion.getVersion());
        } else if (etcdMetadata.getVerifyVersion() != null) {
            metadataResponse.setVerifyVersion(etcdMetadata.getVerifyVersion());
        } else {
            if (clientActivateVersion != null) {
                metadataResponse.setVerifyVersion(clientActivateVersion.getVersion());
            } else {
                metadataResponse.setVerifyVersion(etcdMetadata.getGlobalVersion());
            }
        }
        metadataResponse.setNamespace(namespace);
        metadataResponse.setEnvironment(environment);
        return CompletableFuture.completedFuture(metadataResponse);
    }
}
