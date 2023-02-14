package com.akka.config.server.handler;/* 
    create qiangzhiwei time 2023/2/11
 */

import com.akka.config.protocol.Response;
import com.akka.config.server.core.MetadataManager;
import com.akka.config.store.Store;
import com.akka.remoting.protocol.Command;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class ActivateCommandHandler extends AbstractCommandHandler {

    private final Store store;
    private final MetadataManager metadataManager;

    public ActivateCommandHandler(Store store, MetadataManager metadataManager) {
        this.store = store;
        this.metadataManager = metadataManager;
    }

    @Override
    public CompletableFuture<Response> commandHandler(Command command) throws ExecutionException, InterruptedException {
        return null;
    }
}
