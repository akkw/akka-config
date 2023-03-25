package com.akka.config.server.handler;/* 
    create qiangzhiwei time 2023/2/10
 */

import com.akka.config.ha.etcd.EtcdClient;
import com.akka.config.protocol.CreateConfigRequest;
import com.akka.config.protocol.CreateConfigResponse;
import com.akka.config.protocol.Response;
import com.akka.config.protocol.ResponseCode;
import com.akka.config.server.core.MetadataManager;
import com.akka.config.server.transaction.Transaction;
import com.akka.config.server.transaction.TransactionKind;
import com.akka.config.server.transaction.TransactionManager;
import com.akka.config.server.transaction.TransactionResult;
import com.akka.config.server.transaction.protocol.CreateConfigTransactionSnapshot;
import com.akka.config.server.transaction.protocol.TransactionSnapshot;
import com.akka.config.store.Store;
import com.akka.remoting.protocol.Command;
import com.alibaba.fastjson2.JSON;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

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

    @Override
    public CompletableFuture<Response> commandHandler(Command command) throws ExecutionException, InterruptedException, TimeoutException {
        final CreateConfigRequest createConfigRequest = JSON.parseObject(command.getBody(), CreateConfigRequest.class);
        final byte[] contents = createConfigRequest.getContents();

        final String namespace = createConfigRequest.getNamespace();
        final String environment = createConfigRequest.getEnvironment();

        Response response = checkRequest(createConfigRequest);

        if (response != null) {
            return CompletableFuture.completedFuture(response);
        }
        TransactionSnapshot transactionSnapshot = new CreateConfigTransactionSnapshot(namespace, environment, contents);
        final Transaction transaction = transactionManager.begin(transactionSnapshot, TransactionKind.CREATE_CONFIG);

        transaction.executor();

        final TransactionResult result = transactionManager.end(transaction.getTransactionId());


        response = new CreateConfigResponse();
        ((CreateConfigResponse)response).setVersion((Integer) result.getResult());
        return result.isSuccess() ? CompletableFuture.completedFuture(fillResponse(response, ResponseCode.SUCCESS)) :
                CompletableFuture.completedFuture(fillResponse(response, ResponseCode.CONFIG_CREATE_ERROR, result.getMessage()));
    }


}
