package com.akka.config.server.handler;/* 
    create qiangzhiwei time 2023/2/10
 */

import com.akka.config.ha.common.PathUtils;
import com.akka.config.ha.etcd.EtcdClient;
import com.akka.config.protocol.*;
import com.akka.config.server.core.MetadataManager;
import com.akka.config.store.Store;
import com.akka.remoting.protocol.Command;
import com.alibaba.fastjson2.JSON;
import javafx.util.Pair;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class CreateCommandHandler extends AbstractCommandHandler {

    private final Store store;
    private final MetadataManager metadataManager;
    public CreateCommandHandler(EtcdClient etcdClient, Store store, MetadataManager metadataManager) {
        super(etcdClient);
        this.store = store;
        this.metadataManager = metadataManager;
    }



    // TODO 验证数据库是否存在这个版本
    @Override
    public CompletableFuture<Response> commandHandler(Command command) throws ExecutionException, InterruptedException {
        final CreateConfigRequest createConfigRequest = JSON.parseObject(command.getBody(), CreateConfigRequest.class);
        final byte[] body = createConfigRequest.getBody();

        final String namespace = createConfigRequest.getNamespace();
        final String environment = createConfigRequest.getEnvironment();

        final Response response = checkRequest(createConfigRequest);

        if (response != null) {
            return CompletableFuture.completedFuture(response);
        }

        final String environmentPatch = PathUtils.createEnvironmentPath(etcdClient.getConfig().getPathConfig(), namespace, environment);
        final Pair<String, String> etcdMetadata = etcdClient.get(environmentPatch);
        final Metadata metadata = JSON.parseObject(etcdMetadata.getValue(), Metadata.class);
        final int maxVersion = metadata.getMaxVersion();

        try {
            final int newVersion = maxVersion + 1;
            store.write(namespace, environment, newVersion, body);
            metadata.setMaxVersion(newVersion);
            etcdClient.put(environmentPatch, JSON.toJSONString(metadata));
        } catch (Exception e) {
            return CompletableFuture.completedFuture(new CreateConfigResponse(ResponseCode.CONFIG_CREATE_ERROR.code()));
        }
        return CompletableFuture.completedFuture(new CreateConfigResponse());
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
