package com.akka.cli.admin;/* 
    create qiangzhiwei time 2023/2/13
 */

import com.akka.config.client.core.ClientConfig;
import com.akka.config.client.core.ConfigNetworkClient;
import com.akka.config.protocol.ActivateConfigRequest;
import com.akka.config.protocol.ActivateConfigResponse;
import com.akka.config.protocol.ActivateMultiConfigRequest;
import com.akka.config.protocol.ActivateMultiConfigResponse;
import com.akka.config.protocol.CommandCode;
import com.akka.config.protocol.CreateConfigRequest;
import com.akka.config.protocol.CreateConfigResponse;
import com.akka.config.protocol.CreateNamespaceRequest;
import com.akka.config.protocol.CreateNamespaceResponse;
import com.akka.config.protocol.DeleteConfigRequest;
import com.akka.config.protocol.DeleteConfigResponse;
import com.akka.config.protocol.Metadata;
import com.akka.config.protocol.ReadAllConfigRequest;
import com.akka.config.protocol.ReadAllConfigResponse;
import com.akka.config.protocol.ReadConfigRequest;
import com.akka.config.protocol.ReadConfigResponse;
import com.akka.config.protocol.VerifyConfigRequest;
import com.akka.config.protocol.VerifyConfigResponse;
import com.akka.config.protocol.VerifyMultiConfigRequest;
import com.akka.remoting.exception.RemotingConnectException;
import com.akka.remoting.exception.RemotingSendRequestException;
import com.akka.remoting.exception.RemotingTimeoutException;
import com.akka.remoting.protocol.Command;
import com.alibaba.fastjson2.JSON;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public class AdminNetworkClient extends ConfigNetworkClient {

    public AdminNetworkClient(ClientConfig clientConfig) {
        super(clientConfig);
    }


    public CreateNamespaceResponse createNamespace(String namespace, String environment) throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, InterruptedException {
        final CreateNamespaceRequest request = new CreateNamespaceRequest(namespace, environment);
        final Command respCommand = socketClient.invokeSync(clientConfig.getMetadataRemoteAddress(), buildRequestCommand(request, CommandCode.CREATE_NAMESPACE), 3000);
        return JSON.parseObject(respCommand.getBody(), CreateNamespaceResponse.class);
    }

    public CreateConfigResponse createConfig(String namespace, String environment, String body) throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, InterruptedException {
        final CreateConfigRequest request = new CreateConfigRequest(namespace, environment, body.getBytes(StandardCharsets.UTF_8));
        final Command respCommand = socketClient.invokeSync(clientConfig.getMetadataRemoteAddress(), buildRequestCommand(request, CommandCode.CREATE), 3000);
        return JSON.parseObject(respCommand.getBody(), CreateConfigResponse.class);
    }

    public DeleteConfigResponse deleteConfig(String namespace, String environment, Integer version) throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, InterruptedException {
        final DeleteConfigRequest request = new DeleteConfigRequest(namespace, environment, version);
        final Command respCommand = socketClient.invokeSync(clientConfig.getMetadataRemoteAddress(), buildRequestCommand(request, CommandCode.DELETE), 3000);
        return JSON.parseObject(respCommand.getBody(), DeleteConfigResponse.class);
    }
    public ReadConfigResponse readConfig(String namespace, String environment, Integer version) throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, InterruptedException {
        final ReadConfigRequest request = new ReadConfigRequest(namespace, environment,version);
        final Command respCommand = socketClient.invokeSync(clientConfig.getMetadataRemoteAddress(), buildRequestCommand(request, CommandCode.READ), 3000);
        return JSON.parseObject(respCommand.getBody(), ReadConfigResponse.class);
    }
    public ReadAllConfigResponse readAllConfig(String namespace, String environment) throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, InterruptedException {
        final ReadAllConfigRequest request = new ReadAllConfigRequest(namespace, environment);
        final Command respCommand = socketClient.invokeSync(clientConfig.getMetadataRemoteAddress(), buildRequestCommand(request, CommandCode.READ_ALL_CONFIG), 3000);
        return JSON.parseObject(respCommand.getBody(), ReadAllConfigResponse.class);
    }

    public ActivateConfigResponse activateConfig(String namespace, String environment, Integer version, String clientIp) throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, InterruptedException {
        final ActivateConfigRequest request = new ActivateConfigRequest(namespace, environment,version, clientIp);
        final Command respCommand = socketClient.invokeSync(clientConfig.getMetadataRemoteAddress(), buildRequestCommand(request, CommandCode.ACTIVATE), 3000);
        return JSON.parseObject(respCommand.getBody(), ActivateConfigResponse.class);
    }

    public VerifyConfigResponse verifyConfig(String namespace, String environment, Integer version, String clientIp) throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, InterruptedException {
        final VerifyConfigRequest request = new VerifyConfigRequest(namespace, environment,version, clientIp);
        final Command respCommand = socketClient.invokeSync(clientConfig.getMetadataRemoteAddress(), buildRequestCommand(request,CommandCode.VERIFY), 3000);
        return JSON.parseObject(respCommand.getBody(), VerifyConfigResponse.class);
    }

    public ActivateMultiConfigResponse activateMultiConfig(String namespace, String environment, Integer version, Map<String, Metadata.ClientVersion> activateVersionList) throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, InterruptedException {
        final ActivateMultiConfigRequest request = new ActivateMultiConfigRequest(namespace, environment,version, activateVersionList);
        final Command respCommand = socketClient.invokeSync(clientConfig.getMetadataRemoteAddress(), buildRequestCommand(request,CommandCode.ACTIVATE_MULTI), 3000);
        return JSON.parseObject(respCommand.getBody(), ActivateMultiConfigResponse.class);
    }

    public VerifyMultiConfigRequest verifyMultiConfig(String namespace, String environment, Integer version, Map<String, Metadata.ClientVersion> verifyVersionMap) throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, InterruptedException {
        final VerifyMultiConfigRequest request = new VerifyMultiConfigRequest(namespace, environment,version, verifyVersionMap);
        final Command respCommand = socketClient.invokeSync(clientConfig.getMetadataRemoteAddress(), buildRequestCommand(request, CommandCode.VERIFY_MULTI_CONFIG), 3000);
        return JSON.parseObject(respCommand.getBody(), VerifyMultiConfigRequest.class);
    }
}
