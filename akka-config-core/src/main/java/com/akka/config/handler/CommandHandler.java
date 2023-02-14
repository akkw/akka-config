package com.akka.config.handler;/* 
    create qiangzhiwei time 2023/2/10
 */

import com.akka.config.protocol.Response;
import com.akka.remoting.protocol.Command;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public interface CommandHandler {
    CompletableFuture<Response> commandHandler(Command command) throws ExecutionException, InterruptedException;
}
