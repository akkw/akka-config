package com.akka.config.server.handler;/* 
    create qiangzhiwei time 2023/2/11
 */

import com.akka.config.ha.common.PathUtils;
import com.akka.config.ha.etcd.EtcdClient;
import com.akka.config.protocol.*;
import com.akka.config.server.core.MetadataManager;
import com.akka.config.store.Store;
import com.akka.remoting.protocol.Command;
import com.alibaba.fastjson2.JSON;
import javafx.util.Pair;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;

public class VerifyCommandHandler extends AbstractCommandHandler {

    private final Store store;
    private final MetadataManager metadataManager;


    public VerifyCommandHandler(EtcdClient etcdClient, Store store, MetadataManager metadataManager) {
        super(etcdClient);
        this.store = store;
        this.metadataManager = metadataManager;
    }


    @Override
    public CompletableFuture<Response> commandHandler(Command command) throws ExecutionException, InterruptedException {
        final byte[] body = command.getBody();
        final VerifyConfigRequest request = JSON.parseObject(body, VerifyConfigRequest.class);

        final String namespace = request.getNamespace();
        final String environment = request.getEnvironment();
        final int version = request.getVersion();
        final List<Metadata.ClientVersion> verifyVersionList = request.getVerifyVersionList();
        final String etcdMetadataPath = PathUtils.createEnvironmentPatch(etcdClient.getConfig().getPathConfig(), namespace, environment);

        final Pair<String, String> etcdMetadata = etcdClient.get(etcdMetadataPath);
        final Metadata metadata = JSON.parseObject(etcdMetadata.getValue(), Metadata.class);

        final VerifyConfigResponse noPassResp = checkMetadataNoPass(metadata, request);

        if (noPassResp != null) {
            return CompletableFuture.completedFuture(noPassResp);
        }

        metadata.setVerifyVersion(version);
        fillClientVersion(metadata.getVerifyVersions(), verifyVersionList);

        etcdClient.put(etcdMetadataPath, JSON.toJSONString(metadata));
        return CompletableFuture.completedFuture(new VerifyConfigResponse());
    }



    private VerifyConfigResponse checkMetadataNoPass(Metadata metadata, VerifyConfigRequest request) {
        final int maxVersion = metadata.getMaxVersion();
        final int verifyVersion = metadata.getVerifyVersion();
        final int version = request.getVersion();

        if (version > maxVersion) {
            return new VerifyConfigResponse();
        }

        final List<Metadata.ClientVersion> verifyVersionList = request.getVerifyVersionList();

        if (verifyVersionList != null) {
            List<Metadata.ClientVersion> clientVersionList = new ArrayList<>();
            final Map<String, Metadata.ClientVersion> verifyVersions = verifyVersionList.
                    stream().collect(Collectors.toMap(Metadata.ClientVersion::getClient, Function.identity()));

            for (Metadata.ClientVersion clientVersion : verifyVersions.values()) {
                if (clientVersion.getVersion() > maxVersion) {
                    clientVersionList.add(clientVersion);
                    continue;
                }

                if (clientVersion.getVersion() == verifyVersion) {
                    request.getVerifyVersionList().remove(clientVersion);
                }
            }

            if (!clientVersionList.isEmpty()) {
                return new VerifyConfigResponse();
            }
        }
        return null;
    }
}
