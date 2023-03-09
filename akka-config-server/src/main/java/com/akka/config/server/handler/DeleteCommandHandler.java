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
import java.sql.SQLException;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class DeleteCommandHandler extends AbstractCommandHandler {

    private final Store store;

    private final MetadataManager metadataManager;


    public DeleteCommandHandler(EtcdClient etcdClient, Store store, MetadataManager metadataManager) {
        super(etcdClient);
        this.store = store;
        this.metadataManager = metadataManager;
    }


    @Override
    public CompletableFuture<Response> commandHandler(Command command) throws ExecutionException, InterruptedException {
        final DeleteConfigRequest deleteConfigRequest = JSON.parseObject(command.getBody(), DeleteConfigRequest.class);
        final String namespace = deleteConfigRequest.getNamespace();
        final String environment = deleteConfigRequest.getEnvironment();
        final Integer delVersion = deleteConfigRequest.getVersion();

        final Pair<String, String> etcdMetadata = etcdClient.get(PathUtils.createEnvironmentPatch(etcdClient.getConfig().getPathConfig(), namespace, environment));
        final Metadata metadata = JSON.parseObject(etcdMetadata.getValue(), Metadata.class);


        final DeleteConfigResponse deleteConfigResponse = checkDelete(metadata, delVersion);
        if (deleteConfigResponse != null) {
            return CompletableFuture.completedFuture(deleteConfigResponse);
        }
        try {
            this.store.delete(namespace, environment, delVersion);
        } catch (SQLException e) {
            return CompletableFuture.completedFuture(new DeleteConfigResponse(ResponseCode.CONFIG_DELETE_ERROR.code(),
                    ResponseCode.CONFIG_DELETE_ERROR.getDesc().getBytes(StandardCharsets.UTF_8)));
        }


        return CompletableFuture.completedFuture(new DeleteConfigResponse(ResponseCode.SUCCESS.code()));
    }

    private DeleteConfigResponse checkDelete(Metadata metadata, int delVersion) {
        final Integer globalVersion = metadata.getGlobalVersion();
        final int maxVersion = metadata.getMaxVersion();
        final Set<Metadata.ClientVersion> verifyVersions = metadata.getVerifyVersions();
        final Set<Metadata.ClientVersion> activateVersions = metadata.getActivateVersions();

        if (delVersion > maxVersion) {
            return new DeleteConfigResponse();
        }

        if (delVersion == globalVersion) {
            return new DeleteConfigResponse();
        }

        final boolean verifyCheckResult = verifyVersions.stream().anyMatch((x) -> x.getVersion() == delVersion);

        if (verifyCheckResult) {
            return new DeleteConfigResponse();
        }
        final boolean activateCheckResult = activateVersions.stream().anyMatch((x) -> x.getVersion() == delVersion);
        if (activateCheckResult) {
            return new DeleteConfigResponse();
        }

        return null;
    }
}
