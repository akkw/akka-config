package com.akka.config.server.core;/*
    create qiangzhiwei time 2023/2/10
 */

import com.akka.config.protocol.CommandCode;
import com.akka.remoting.netty.AkkaNettySocketServer;
import com.akka.remoting.netty.NettyRequestProcessor;
import com.akka.remoting.netty.NettyServerConfig;
import com.akka.tools.api.LifeCycle;

public class ServerNetwork implements LifeCycle {

    private final AkkaNettySocketServer server;

    public ServerNetwork() {
        this(null);
    }

    public ServerNetwork(NettyServerConfig nettyServerConfig) {
        this.server = new AkkaNettySocketServer(nettyServerConfig);
    }

    @Override
    public void start() {
        this.server.start();
    }

    public void registerProcessor(CommandCode code, NettyRequestProcessor processor) {
        this.server.registerProcessor(code.code(), processor, null);
    }

    @Override
    public void stop() {
        this.server.shutdown();
    }
}
