package com.akka.config.server.handler;/* 
    create qiangzhiwei time 2023/2/10
 */

import com.akka.config.protocol.ReadConfigRequest;
import com.akka.config.protocol.Response;
import com.akka.config.store.Store;
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

        // TODO readConfig
        return null;
    }
}
