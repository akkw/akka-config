package com.akka.config.server.handler;/* 
    create qiangzhiwei time 2023/2/11
 */

import com.akka.config.ha.common.PathUtils;
import com.akka.config.ha.etcd.EtcdClient;
import com.akka.config.ha.etcd.EtcdConfig;
import com.akka.config.handler.CommandHandler;
import com.akka.config.protocol.Response;
import com.akka.config.protocol.VerifyConfigRequest;
import com.akka.config.protocol.VerifyConfigResponse;
import com.akka.config.server.core.MetadataManager;
import com.akka.config.protocol.Metadata;
import com.akka.config.store.Store;
import com.akka.remoting.protocol.Command;
import com.alibaba.fastjson2.JSON;
import javafx.util.Pair;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class VerifyCommandHandler extends AbstractCommandHandler {

    private final Store store;
    private final MetadataManager metadataManager;


    public VerifyCommandHandler(Store store, MetadataManager metadataManager) {
        this.store = store;
        this.metadataManager = metadataManager;
    }


    @Override
    public CompletableFuture<Response> commandHandler(Command command) throws ExecutionException, InterruptedException {
        return CompletableFuture.completedFuture(new VerifyConfigResponse());
    }
}
