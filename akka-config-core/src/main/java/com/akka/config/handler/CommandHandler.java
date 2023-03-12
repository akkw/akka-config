package com.akka.config.handler;/* 
    create qiangzhiwei time 2023/2/10
 */

import com.akka.config.protocol.Metadata;
import com.akka.config.protocol.Response;
import com.akka.config.protocol.ResponseCode;
import com.akka.remoting.protocol.Command;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public interface CommandHandler {

    default void fillResponse(Response response, ResponseCode responseCode) {
        response.setCode(responseCode.code());
        response.setMessage(responseCode.getDesc().getBytes(StandardCharsets.UTF_8));
    }



    CompletableFuture<Response> commandHandler(Command command) throws ExecutionException, InterruptedException, SQLException, NoSuchFieldException, InstantiationException, IllegalAccessException;
}
