package com.akka.cli.config.admin;/*
    create qiangzhiwei time 2023/2/13
 */

import com.akka.config.client.core.ClientConfig;
import com.akka.config.client.core.ConfigNetworkClient;
import com.akka.config.protocol.ActivateConfigRequest;
import com.akka.config.protocol.ActivateConfigResponse;
import com.akka.config.protocol.CommandCode;
import com.akka.config.protocol.CreateConfigRequest;
import com.akka.config.protocol.CreateConfigResponse;
import com.akka.config.protocol.CreateNamespaceRequest;
import com.akka.config.protocol.CreateNamespaceResponse;
import com.akka.config.protocol.DeleteConfigRequest;
import com.akka.config.protocol.DeleteConfigResponse;
import com.akka.config.protocol.Metadata;
import com.akka.config.protocol.MultiReadConfigRequest;
import com.akka.config.protocol.MutliReadConfigResponse;
import com.akka.config.protocol.VerifyConfigRequest;
import com.akka.config.protocol.VerifyConfigResponse;
import com.akka.remoting.exception.RemotingConnectException;
import com.akka.remoting.exception.RemotingSendRequestException;
import com.akka.remoting.exception.RemotingTimeoutException;
import com.akka.remoting.protocol.Command;
import com.alibaba.fastjson2.JSON;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class Admin extends ConfigNetworkClient {

    public Admin(ClientConfig clientConfig) {
        super(clientConfig);
    }


    public CreateNamespaceResponse createNamespace(String namespace, String environment) throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, InterruptedException {
        final CreateNamespaceRequest request = new CreateNamespaceRequest(namespace, environment);
        final Command respCommand = socketClient.invokeSync(clientConfig.getRemoteAddress(), buildRequestCommand(request, CommandCode.CREATE_NAMESPACE), 3000);
        return JSON.parseObject(respCommand.getBody(), CreateNamespaceResponse.class);
    }

    public CreateConfigResponse createConfig(String namespace, String environment, String body) throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, InterruptedException {
        final CreateConfigRequest request = new CreateConfigRequest(namespace, environment, body.getBytes(StandardCharsets.UTF_8));
        final Command respCommand = socketClient.invokeSync(clientConfig.getRemoteAddress(), buildRequestCommand(request, CommandCode.CREATE), 3000);
        return JSON.parseObject(respCommand.getBody(), CreateConfigResponse.class);
    }

    public DeleteConfigResponse deleteConfig(String namespace, String environment, Integer version) throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, InterruptedException {
        final DeleteConfigRequest request = new DeleteConfigRequest(namespace, environment, version);
        final Command respCommand = socketClient.invokeSync(clientConfig.getRemoteAddress(), buildRequestCommand(request, CommandCode.DELETE), 3000);
        return JSON.parseObject(respCommand.getBody(), DeleteConfigResponse.class);
    }

    public MutliReadConfigResponse readAllConfig(String namespace, String environment, int minVersion, int maxVersion) throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, InterruptedException {
        final MultiReadConfigRequest request = new MultiReadConfigRequest(namespace, environment, minVersion, maxVersion);
        final Command respCommand = socketClient.invokeSync(clientConfig.getRemoteAddress(), buildRequestCommand(request, CommandCode.READ_ALL_CONFIG), 3000);
        return JSON.parseObject(respCommand.getBody(), MutliReadConfigResponse.class);
    }

    public ActivateConfigResponse activateConfig(String namespace, String environment, Integer version, String clientIp, List<Metadata.ClientVersion> activateVersionList) throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, InterruptedException {
        final ActivateConfigRequest request = new ActivateConfigRequest(namespace, environment,version, clientIp, activateVersionList);
        final Command respCommand = socketClient.invokeSync(clientConfig.getRemoteAddress(), buildRequestCommand(request, CommandCode.ACTIVATE), 3000);
        return JSON.parseObject(respCommand.getBody(), ActivateConfigResponse.class);
    }

    public VerifyConfigResponse verifyConfig(String namespace, String environment, Integer version, String clientIp, List<Metadata.ClientVersion> verifyVersionList) throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, InterruptedException {
        final VerifyConfigRequest request = new VerifyConfigRequest(namespace, environment,version, clientIp, verifyVersionList);
        final Command respCommand = socketClient.invokeSync(clientConfig.getRemoteAddress(), buildRequestCommand(request,CommandCode.VERIFY), 3000);
        return JSON.parseObject(respCommand.getBody(), VerifyConfigResponse.class);
    }

}
