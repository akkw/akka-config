package com.akka.config.server.handler;/* 
    create qiangzhiwei time 2023/2/10
 */

import com.akka.config.protocol.ReadConfigRequest;
import com.akka.config.protocol.ReadConfigResponse;
import com.akka.config.protocol.Response;
import com.akka.config.protocol.ResponseCode;
import com.akka.config.store.Store;
import com.akka.config.store.mysql.model.MysqlConfigModel;
import com.akka.remoting.protocol.Command;
import com.alibaba.fastjson.JSON;

import java.util.concurrent.CompletableFuture;

public class ReadCommandHandler extends AbstractCommandHandler {

    private final Store store;

    public ReadCommandHandler(Store store) {
        this.store = store;
    }



    @Override
    public CompletableFuture<Response> commandHandler(Command command) {
        final ReadConfigRequest readConfigRequest = JSON.parseObject(command.getBody(), ReadConfigRequest.class);
        final String namespace = readConfigRequest.getNamespace();
        final String environment = readConfigRequest.getEnvironment();
        final Integer version = readConfigRequest.getVersion();
        final ReadConfigResponse readConfigResponse = new ReadConfigResponse();
        try {
            final MysqlConfigModel result = store.read(namespace, environment, version);
            if (result != null) {
                readConfigResponse.setBody(result.getContent());
                readConfigResponse.setNamespace(result.getNamespace());
                readConfigResponse.setEnvironment(result.getEnvironment());
                readConfigResponse.setVersion(result.getVersion());
            } else {
                fillResponse(readConfigResponse, ResponseCode.CONFIG_NOT_EXIST);
                return CompletableFuture.completedFuture(readConfigResponse);
            }
        } catch (Exception e) {
            fillResponse(readConfigResponse, ResponseCode.CONFIG_READ_ERROR);
            return CompletableFuture.completedFuture(readConfigResponse);
        }
        fillResponse(readConfigResponse, ResponseCode.SUCCESS);
        return CompletableFuture.completedFuture(readConfigResponse);
    }
}
