package com.akka.config.server.handler;/* 
    create qiangzhiwei time 2023/2/10
 */

import com.akka.config.protocol.ActivateConfigResponse;
import com.akka.config.protocol.DeleteConfigRequest;
import com.akka.config.protocol.Metadata;
import com.akka.config.protocol.Response;
import com.akka.config.server.core.MetadataManager;
import com.akka.config.store.Store;
import com.akka.remoting.protocol.Command;
import com.alibaba.fastjson2.JSON;

import java.util.concurrent.CompletableFuture;

public class DeleteCommandHandler extends AbstractCommandHandler {

    private final Store store;

    private final MetadataManager metadataManager;


    public DeleteCommandHandler(Store store, MetadataManager metadataManager) {
        this.store = store;
        this.metadataManager = metadataManager;
    }


    @Override
    public CompletableFuture<Response> commandHandler(Command command) {
        final DeleteConfigRequest deleteConfigRequest = JSON.parseObject(command.getBody(), DeleteConfigRequest.class);
        final String namespace = deleteConfigRequest.getNamespace();
        final String environment = deleteConfigRequest.getEnvironment();
        final int delVersion = deleteConfigRequest.getVersion();

        final Metadata metadata = metadataManager.getMetadata(namespace, environment);
        boolean canDel = true;



        return CompletableFuture.completedFuture(new ActivateConfigResponse(false));
    }
}
