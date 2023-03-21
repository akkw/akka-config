package com.akka.config.server.handler;/* 
    create qiangzhiwei time 2023/2/11
 */

import com.akka.config.ha.common.PathUtils;
import com.akka.config.ha.etcd.EtcdClient;
import com.akka.config.protocol.ActivateConfigRequest;
import com.akka.config.protocol.ActivateConfigResponse;
import com.akka.config.protocol.Metadata;
import com.akka.config.protocol.Response;
import com.akka.config.protocol.ResponseCode;
import com.akka.config.server.core.MetadataManager;
import com.akka.config.server.transaction.Transaction;
import com.akka.config.server.transaction.TransactionKind;
import com.akka.config.server.transaction.TransactionManager;
import com.akka.config.server.transaction.TransactionResult;
import com.akka.config.server.transaction.protocol.ActiveConfigTransactionSnapshot;
import com.akka.config.store.Store;
import com.akka.remoting.protocol.Command;
import com.alibaba.fastjson2.JSON;
import javafx.util.Pair;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

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
    public CompletableFuture<Response> commandHandler(Command command) throws ExecutionException, InterruptedException {
        final ActivateConfigRequest request = JSON.parseObject(command.getBody(), ActivateConfigRequest.class);

        ActiveConfigTransactionSnapshot transactionSnapshot = new ActiveConfigTransactionSnapshot(request.getNamespace(),  request.getEnvironment(),
                request.getVersion(), request.getClientIp(), request.getActivateVersionList());

        Transaction transaction = transactionManager.begin(transactionSnapshot, TransactionKind.ACTIVATE);

        transaction.executor();

        TransactionResult end = transactionManager.end(transaction.getTransactionId());
        return CompletableFuture.completedFuture(new ActivateConfigResponse(ResponseCode.SUCCESS.code(),
                ResponseCode.SUCCESS.getDesc().getBytes(StandardCharsets.UTF_8)));
    }
}
