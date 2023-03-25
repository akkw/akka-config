package com.akka.config.client.core;/* 
    create qiangzhiwei time 2023/2/12
 */

import com.akka.config.protocol.CommandCode;
import com.akka.config.protocol.MetadataRequest;
import com.akka.config.protocol.MetadataResponse;
import com.akka.config.protocol.ReadConfigRequest;
import com.akka.config.protocol.ReadConfigResponse;
import com.akka.config.protocol.Request;
import com.akka.remoting.exception.RemotingConnectException;
import com.akka.remoting.exception.RemotingSendRequestException;
import com.akka.remoting.exception.RemotingTimeoutException;
import com.akka.remoting.netty.AkkaNettySocketClient;
import com.akka.remoting.protocol.Command;
import com.akka.tools.api.LifeCycle;
import com.alibaba.fastjson.JSON;


public class ConfigNetworkClient implements LifeCycle {

    protected final AkkaNettySocketClient socketClient;

    protected final ClientConfig clientConfig;

    public ConfigNetworkClient(ClientConfig clientConfig) {
        this.clientConfig = clientConfig;
        this.socketClient = new AkkaNettySocketClient(clientConfig.getNettyClientConfig());
    }


    public MetadataResponse metadata(String namespace, String environment, String clientIp) throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, InterruptedException {
        MetadataRequest request = new MetadataRequest();
        request.setNamespace(namespace);
        request.setEnvironment(environment);
        request.setClientIp(clientIp);
        final Command respCommand = socketClient.invokeSync(clientConfig.getRemoteAddress(), buildRequestCommand(request, CommandCode.METADATA), 3000);
        return JSON.parseObject(respCommand.getBody(), MetadataResponse.class);
    }

    public ReadConfigResponse readConfig(String namespace, String environment, Integer version) throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, InterruptedException {
        final ReadConfigRequest request = new ReadConfigRequest(namespace, environment,version);
        final Command respCommand = socketClient.invokeSync(clientConfig.getRemoteAddress(), buildRequestCommand(request, CommandCode.READ), 3000);
        return com.alibaba.fastjson2.JSON.parseObject(respCommand.getBody(), ReadConfigResponse.class);
    }

    public Command buildRequestCommand(Request request, CommandCode code) {
        final Command command = Command.createRequestCommand(code.code(), null);
        command.setBody(JSON.toJSONBytes(request));
        return command;
    }

    @Override
    public void start() {
        socketClient.start();
    }

    @Override
    public void stop() {
        socketClient.shutdown();
    }
}
