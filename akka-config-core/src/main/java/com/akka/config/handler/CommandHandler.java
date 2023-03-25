package com.akka.config.handler;/* 
    create qiangzhiwei time 2023/2/10
 */

import com.akka.config.protocol.Response;
import com.akka.config.protocol.ResponseCode;
import com.akka.remoting.protocol.Command;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public interface CommandHandler {

    default Response fillResponse(Response response, ResponseCode responseCode) {
        response.setCode(responseCode.code());
        response.setMessage(responseCode.getDesc().getBytes(StandardCharsets.UTF_8));
        return response;
    }

    default Response fillResponse(Response response, ResponseCode responseCode, String message) {
        response.setCode(responseCode.code());
        response.setMessage(message.getBytes(StandardCharsets.UTF_8));
        return response;
    }

    CompletableFuture<Response> commandHandler(Command command) throws ExecutionException, InterruptedException, SQLException, NoSuchFieldException, InstantiationException, IllegalAccessException, TimeoutException;
}
