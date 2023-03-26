package com.akka.config.server.handler;/* 
    create qiangzhiwei time 2023/2/11
 */

import com.akka.config.ha.etcd.EtcdClient;
import com.akka.config.protocol.*;
import com.akka.config.server.core.MetadataManager;
import com.akka.config.server.transaction.Transaction;
import com.akka.config.server.transaction.TransactionKind;
import com.akka.config.server.transaction.TransactionManager;
import com.akka.config.server.transaction.TransactionResult;
import com.akka.config.server.transaction.protocol.UpdateVersionTransactionSnapshot;
import com.akka.config.store.Store;
import com.akka.remoting.protocol.Command;
import com.alibaba.fastjson2.JSON;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class ActivateCommandHandler extends AbstractCommandHandler {

    private final Store store;
    private final MetadataManager metadataManager;

    private final TransactionManager transactionManager;

    public ActivateCommandHandler(EtcdClient etcdClient, Store store, MetadataManager metadataManager, TransactionManager transactionManager) {
        super(etcdClient);
        this.store = store;
        this.metadataManager = metadataManager;
        this.transactionManager = transactionManager;
    }

    @Override
    public CompletableFuture<Response> commandHandler(Command command) throws ExecutionException, InterruptedException, TimeoutException {
        final ActivateConfigRequest request = JSON.parseObject(command.getBody(), ActivateConfigRequest.class);

        Response response = checkRequest(request);
        if (response != null) {
            return CompletableFuture.completedFuture(response);
        }

        UpdateVersionTransactionSnapshot transactionSnapshot = new UpdateVersionTransactionSnapshot(request.getNamespace(),
                request.getEnvironment(), request.getVersion(), request.getClientIp(), request.getActivateVersionList());

        Transaction transaction = transactionManager.begin(transactionSnapshot, TransactionKind.ACTIVATE_VERSION);

        transaction.executor();

        TransactionResult result = transactionManager.end(transaction.getTransactionId());
        response = new ActivateConfigResponse();
        return result.isSuccess() ? CompletableFuture.completedFuture(fillResponse(response, ResponseCode.SUCCESS)) :
                CompletableFuture.completedFuture(fillResponse(response, ResponseCode.ACTIVE_VERSION_ERROR, result.getMessage()));
    }
}
