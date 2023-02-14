package com.akka.config.server.handler;/* 
    create qiangzhiwei time 2023/2/10
 */

import com.akka.config.handler.CommandHandler;
import com.akka.config.protocol.ActivateConfigResponse;
import com.akka.config.protocol.CreateConfigRequest;
import com.akka.config.protocol.Metadata;
import com.akka.config.protocol.Response;
import com.akka.config.server.core.MetadataManager;
import com.akka.config.store.Store;
import com.akka.remoting.protocol.Command;
import com.alibaba.fastjson2.JSON;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class CreateCommandHandler extends AbstractCommandHandler {

    private final Store store;
    private final MetadataManager metadataManager;
    public CreateCommandHandler(Store store, MetadataManager metadataManager) {
        this.store = store;
        this.metadataManager = metadataManager;
    }


    @Override
    public CompletableFuture<Response> commandHandler(Command command) throws ExecutionException, InterruptedException {
        final CreateConfigRequest createConfigRequest = JSON.parseObject(command.getBody(), CreateConfigRequest.class);
        final byte[] body = createConfigRequest.getBody();
        final String namespace = createConfigRequest.getNamespace();
        final String environment = createConfigRequest.getEnvironment();
        final Metadata metadata = metadataManager.getMetadata(namespace, environment);

        final int maxVersion = metadata.getMaxVersion();
        // TODO 持久化配置

        return CompletableFuture.completedFuture(new ActivateConfigResponse());
    }
}
