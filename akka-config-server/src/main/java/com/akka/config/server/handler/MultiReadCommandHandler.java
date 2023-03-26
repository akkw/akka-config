package com.akka.config.server.handler;/* 
    create qiangzhiwei time 2023/2/14
 */

import com.akka.config.ha.etcd.EtcdClient;
import com.akka.config.protocol.MultiReadConfigRequest;
import com.akka.config.protocol.MutliReadConfigResponse;
import com.akka.config.protocol.Response;
import com.akka.config.store.Store;
import com.akka.config.store.mysql.model.MysqlConfigModel;
import com.akka.remoting.protocol.Command;
import com.alibaba.fastjson2.JSON;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class MultiReadCommandHandler extends AbstractCommandHandler {
    private final Store store;
    public MultiReadCommandHandler(EtcdClient etcdClient, Store store) {
        super(etcdClient);
        this.store = store;
    }

    @Override
    public CompletableFuture<Response> commandHandler(Command command) throws ExecutionException, InterruptedException, SQLException, NoSuchFieldException, InstantiationException, IllegalAccessException {
        final byte[] body = command.getBody();
        final MultiReadConfigRequest multiReadConfigRequest = JSON.parseObject(body, MultiReadConfigRequest.class);

        final int minVersion = multiReadConfigRequest.getMinVersion();
        final int maxVersion = multiReadConfigRequest.getMaxVersion();
        final String namespace = multiReadConfigRequest.getNamespace();
        final String environment = multiReadConfigRequest.getEnvironment();

        final List<MysqlConfigModel> mysqlConfigModelList = store.multiRead(namespace, environment, minVersion, maxVersion);
        final MutliReadConfigResponse readAllConfigResponse = new MutliReadConfigResponse();
        readAllConfigResponse.setNamespace(namespace);
        readAllConfigResponse.setEnvironment(environment);
        readAllConfigResponse.setBody(JSON.toJSONString(mysqlConfigModelList));
        return CompletableFuture.completedFuture(readAllConfigResponse);
    }
}
