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

    public ActivateCommandHandler(EtcdClient etcdClient, Store store, MetadataManager metadataManager) {
        super(etcdClient);
        this.store = store;
        this.metadataManager = metadataManager;
    }

    @Override
    public CompletableFuture<Response> commandHandler(Command command) throws ExecutionException, InterruptedException {
        final ActivateConfigRequest request = JSON.parseObject(command.getBody(), ActivateConfigRequest.class);
        final String namespace = request.getNamespace();
        final String environment = request.getEnvironment();
        final Integer reqVersion = request.getVersion();
        final String etcdEnvMetadataPath = PathUtils.createEnvironmentPath(etcdClient.getConfig().getPathConfig(), namespace, environment);


        final Pair<String, String> etcdMetadata = etcdClient.get(etcdEnvMetadataPath);
        final Metadata metadata = JSON.parseObject(etcdMetadata.getValue(), Metadata.class);

        final Response noPassResp = checkMetadataNoPass(metadata, request);
        if (noPassResp != null) {
            return CompletableFuture.completedFuture(noPassResp);
        }

        clearUpClientVersion(reqVersion, metadata.getActivateVersions(), request.getActivateVersionList());
        metadata.setGlobalVersion(reqVersion);
        etcdClient.put(etcdEnvMetadataPath, JSON.toJSONString(metadata));
        return CompletableFuture.completedFuture(new ActivateConfigResponse(ResponseCode.SUCCESS.code(),
                ResponseCode.SUCCESS.getDesc().getBytes(StandardCharsets.UTF_8)));
    }


    private Response checkMetadataNoPass(Metadata metadata, ActivateConfigRequest request) {
        final Integer version = request.getVersion();
        final List<Metadata.ClientVersion> reqActivateVersionList = request.getActivateVersionList();
        if (version == null || reqActivateVersionList == null) {
            return null;
        }

        final int maxVersion = metadata.getMaxVersion();

        Response response = checkVersion(request.getVersion(), maxVersion);
        if (response != null) {
            return response;
        }
        for (Metadata.ClientVersion reqClientVersion: reqActivateVersionList) {
            response = checkVersion(reqClientVersion.getVersion(), maxVersion);
            if (response != null) {
                return response;
            }
        }
        return null;
    }
}
