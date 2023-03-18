package com.akka.config.server.handler;/* 
    create qiangzhiwei time 2023/2/10
 */

import com.akka.config.ha.etcd.EtcdClient;
import com.akka.config.protocol.*;
import com.akka.config.server.core.MetadataManager;
import com.akka.config.server.transaction.*;
import com.akka.config.store.Store;
import com.akka.remoting.protocol.Command;
import com.alibaba.fastjson2.JSON;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class CreateCommandHandler extends AbstractCommandHandler {

    private final Store store;
    private final MetadataManager metadataManager;

    private final TransactionManager transactionManager;
    public CreateCommandHandler(EtcdClient etcdClient, Store store, MetadataManager metadataManager, TransactionManager transactionManager) {
        super(etcdClient);
        this.store = store;
        this.metadataManager = metadataManager;
        this.transactionManager = transactionManager;
    }



    // TODO 验证数据库是否存在这个版本
    @Override
    public CompletableFuture<Response> commandHandler(Command command) throws ExecutionException, InterruptedException {
        final CreateConfigRequest createConfigRequest = JSON.parseObject(command.getBody(), CreateConfigRequest.class);
        final byte[] contents = createConfigRequest.getContents();

        final String namespace = createConfigRequest.getNamespace();
        final String environment = createConfigRequest.getEnvironment();

        Response response = checkRequest(createConfigRequest);

        if (response != null) {
            return CompletableFuture.completedFuture(response);
        }

        final Transaction transaction = transactionManager.begin(namespace, environment, contents, TransactionKind.METADATA);
        if (transaction instanceof NOPTransaction) {
            response = new CreateConfigResponse();
            fillResponse(response, ResponseCode.CONFIG_CREATE_ERROR);
            return CompletableFuture.completedFuture(response);
        }

        transaction.executor();
        final TransactionResult end = transactionManager.end(transaction.getTransactionId());
        response = new CreateConfigResponse();
        return end.isSuccess() ? CompletableFuture.completedFuture(response) : CompletableFuture.completedFuture(fillResponse(response, ResponseCode.CONFIG_CREATE_ERROR));
    }


    private Response checkRequest(CreateConfigRequest request) {
        final String namespace = request.getNamespace();
        final String environment = request.getEnvironment();
        if (namespace == null || "".equals(namespace.trim())) {
            return new CreateConfigResponse(ResponseCode.CONFIG_CREATE_ERROR.code(),"namespace is blank".getBytes(StandardCharsets.UTF_8));
        }
        if (environment == null || "".equals(environment.trim())) {
            return new CreateConfigResponse(ResponseCode.CONFIG_CREATE_ERROR.code(),"environment is blank".getBytes(StandardCharsets.UTF_8));
        }
        return null;
    }
}
