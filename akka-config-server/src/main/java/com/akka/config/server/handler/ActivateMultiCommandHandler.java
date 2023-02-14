package com.akka.config.server.handler;/* 
    create qiangzhiwei time 2023/2/14
 */

import com.akka.config.ha.etcd.EtcdClient;
import com.akka.config.protocol.Response;
import com.akka.remoting.protocol.Command;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class ActivateMultiCommandHandler extends AbstractCommandHandler{
    public ActivateMultiCommandHandler(EtcdClient etcdClient) {

    }

    @Override
    public CompletableFuture<Response> commandHandler(Command command) throws ExecutionException, InterruptedException {
        return null;
    }
}
