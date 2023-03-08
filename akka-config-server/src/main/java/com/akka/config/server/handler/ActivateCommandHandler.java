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
import io.etcd.jetcd.Client;
import javafx.util.Pair;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ActivateCommandHandler extends AbstractCommandHandler {

    private final Store store;
    private final MetadataManager metadataManager;

    public ActivateCommandHandler(EtcdClient etcdClient, Store store, MetadataManager metadataManager) {
        super(etcdClient);
        this.store = store;
        this.metadataManager = metadataManager;
    }
    // TODO 验证数据库是否存在这个版本
    @Override
    public CompletableFuture<Response> commandHandler(Command command) throws ExecutionException, InterruptedException {
        final ActivateConfigRequest request = JSON.parseObject(command.getBody(), ActivateConfigRequest.class);
        final String namespace = request.getNamespace();
        final String environment = request.getEnvironment();
        final int version = request.getVersion();
        final List<Metadata.ClientVersion> activateVersionList = request.getActivateVersionList();
        final String etcdEnvMetadataPath = PathUtils.createEnvironmentPatch(etcdClient.getConfig().getPathConfig(), namespace, environment);


        final Pair<String, String> etcdMetadata = etcdClient.get(etcdEnvMetadataPath);
        final Metadata metadata = JSON.parseObject(etcdMetadata.getValue(), Metadata.class);

        final ActivateConfigResponse noPassResp = checkMetadataNoPass(metadata, request);
        if (noPassResp != null) {
            return CompletableFuture.completedFuture(noPassResp);
        }

        metadata.setGlobalVersion(version);
        fillClientVersion(metadata.getActivateVersions(), activateVersionList);

        etcdClient.put(etcdEnvMetadataPath, JSON.toJSONString(metadata));
        return CompletableFuture.completedFuture(new ActivateConfigResponse(ResponseCode.SUCCESS.code(),
                ResponseCode.SUCCESS.getDesc().getBytes(StandardCharsets.UTF_8)));
    }


    private ActivateConfigResponse checkMetadataNoPass(Metadata metadata, ActivateConfigRequest request) {
        final int maxVersion = metadata.getMaxVersion();
        final int globalVersion = metadata.getGlobalVersion();
        final int reqVersion = request.getVersion();
        if (reqVersion > maxVersion) {
            return new ActivateConfigResponse();
        }

        List<Metadata.ClientVersion> clientVersionList = new ArrayList<>();
        final Map<String, Metadata.ClientVersion> activateVersions = request.getActivateVersionList()
                .stream().collect(Collectors.toMap(Metadata.ClientVersion::getClient, Function.identity()));

        for (Metadata.ClientVersion clientVersion : activateVersions.values()) {
            if (clientVersion.getVersion() > maxVersion) {
                clientVersionList.add(clientVersion);
                continue;
            }

            if (clientVersion.getVersion() == globalVersion) {
                request.getActivateVersionList().remove(clientVersion);
            }
        }

        if (!clientVersionList.isEmpty()) {
            return new ActivateConfigResponse();
        }
        return null;
    }
}
