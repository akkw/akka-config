package com.akka.config.server.handler;/* 
    create qiangzhiwei time 2023/2/11
 */

import com.akka.config.ha.common.PathUtils;
import com.akka.config.ha.etcd.EtcdClient;
import com.akka.config.handler.CommandHandler;
import com.akka.config.protocol.Metadata;
import com.akka.config.protocol.Response;
import com.akka.config.protocol.ResponseCode;
import com.akka.config.protocol.VerifyConfigResponse;
import com.alibaba.fastjson2.JSON;
import javafx.util.Pair;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;

abstract class AbstractCommandHandler implements CommandHandler {

    protected EtcdClient etcdClient;

    public AbstractCommandHandler() {
    }


    // TODO 抽象读etcd中的元数据方法
    public AbstractCommandHandler(EtcdClient etcdClient) {
        this.etcdClient = etcdClient;
    }




    protected Metadata getEtcdMetadata(String namespace, String environment) throws ExecutionException, InterruptedException {
        final Pair<String, String> metadataPair = etcdClient.get(
                PathUtils.createEnvironmentPatch(etcdClient.getConfig().getPathConfig(), namespace, environment));
        return JSON.parseObject(metadataPair != null ? metadataPair.getValue() : null, Metadata.class);
    }


    protected String getEtcdMetadataPath(String namespace, String environment) {
        return PathUtils.createEnvironmentPatch(etcdClient.getConfig().getPathConfig(), namespace, environment);
    }


    protected Response checkVersion(Integer version, Integer maxVersion) {
        if (version < 0 || version > maxVersion) {
            final VerifyConfigResponse response = new VerifyConfigResponse();
            fillResponse(response, ResponseCode.VERSION_BORDER_ERROR);
            return response;
        }

        return null;
    }


    protected void clearUpClientVersion(Integer reqVersion, Set<Metadata.ClientVersion> metadataVerifyVersionsSet, List<Metadata.ClientVersion> requestVersionList) {
        if (requestVersionList == null || requestVersionList.isEmpty()) {
            return;
        }

        doClearUpClientVersion(reqVersion, metadataVerifyVersionsSet, requestVersionList);
    }

    private void doClearUpClientVersion(Integer reqVersion, Set<Metadata.ClientVersion> metadataVerifyVersionsSet, List<Metadata.ClientVersion> requestVersionList) {
        if (reqVersion != null) {
            requestVersionList.removeIf(reqClientVersion -> reqVersion.equals(reqClientVersion.getVersion()));
            metadataVerifyVersionsSet.removeIf(metadataClientVersion -> reqVersion.equals(metadataClientVersion.getVersion()));
        }
        for (Metadata.ClientVersion clientVersion: requestVersionList) {
            metadataVerifyVersionsSet.remove(clientVersion);
        }
        metadataVerifyVersionsSet.addAll(requestVersionList);
    }
}
