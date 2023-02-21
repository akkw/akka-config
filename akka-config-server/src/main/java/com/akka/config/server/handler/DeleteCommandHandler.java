package com.akka.config.server.handler;/* 
    create qiangzhiwei time 2023/2/10
 */

import com.akka.config.protocol.DeleteConfigRequest;
import com.akka.config.protocol.DeleteConfigResponse;
import com.akka.config.protocol.Response;
import com.akka.config.protocol.ResponseCode;
import com.akka.config.server.core.MetadataManager;
import com.akka.config.store.Store;
import com.akka.remoting.protocol.Command;
import com.alibaba.fastjson2.JSON;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
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

        try {
            this.store.delete(namespace, environment, delVersion);
        } catch (SQLException e) {
            return CompletableFuture.completedFuture(new DeleteConfigResponse(ResponseCode.CONFIG_DELETE_ERROR.code(),
                    ResponseCode.CONFIG_DELETE_ERROR.getDesc().getBytes(StandardCharsets.UTF_8)));
        }


        return CompletableFuture.completedFuture(new DeleteConfigResponse(ResponseCode.SUCCESS.code()));
    }
}
