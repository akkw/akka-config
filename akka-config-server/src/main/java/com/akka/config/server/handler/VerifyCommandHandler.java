package com.akka.config.server.handler;/* 
    create qiangzhiwei time 2023/2/11
 */

import com.akka.config.ha.etcd.EtcdClient;
import com.akka.config.protocol.Metadata;
import com.akka.config.protocol.Response;
import com.akka.config.protocol.VerifyConfigRequest;
import com.akka.config.protocol.VerifyConfigResponse;
import com.akka.config.server.core.MetadataManager;
import com.akka.config.store.Store;
import com.akka.remoting.protocol.Command;
import com.alibaba.fastjson2.JSON;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

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
        final Integer reqVersion = request.getVersion();

        final Metadata etcdMetadata = getEtcdMetadata(namespace, environment);

        final Response noPassResp = checkRequestNoPass(etcdMetadata, request);

        if (noPassResp != null) {
            return CompletableFuture.completedFuture(noPassResp);
        }

        clearUpClientVersion(reqVersion, etcdMetadata.getVerifyVersions(), request.getVerifyVersionList());
        etcdMetadata.setVerifyVersion(reqVersion);
        etcdClient.put(getEtcdMetadataPath(namespace, environment), JSON.toJSONString(etcdMetadata));
        return CompletableFuture.completedFuture(new VerifyConfigResponse());
    }


    private Response checkRequestNoPass(Metadata metadata, VerifyConfigRequest request) {

        final Integer reqVersion = request.getVersion();
        final List<Metadata.ClientVersion> reqVerifyVersionList = request.getVerifyVersionList();

        if (reqVersion == null || reqVerifyVersionList == null) {
            return null;
        }

        final int maxVersion = metadata.getMaxVersion();


        Response response = checkVersion(reqVersion, maxVersion);
        if (response != null) {
            return response;
        }

        for (Metadata.ClientVersion reqClientVersion : reqVerifyVersionList) {
            response = checkVersion(reqClientVersion.getVersion(), maxVersion);
            if (response != null) {
                return response;
            }
        }
        return null;
    }

}
