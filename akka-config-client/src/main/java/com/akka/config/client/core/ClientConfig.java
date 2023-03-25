package com.akka.config.client.core;/* 
    create qiangzhiwei time 2023/2/12
 */

import com.akka.remoting.netty.NettyClientConfig;

public class ClientConfig {
    private String remoteAddress = "127.0.0.1:9707";
    private NettyClientConfig nettyClientConfig = new NettyClientConfig();


    public String getRemoteAddress() {
        return remoteAddress;
    }

    public void setRemoteAddress(String remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    public NettyClientConfig getNettyClientConfig() {
        return nettyClientConfig;
    }

    public void setNettyClientConfig(NettyClientConfig nettyClientConfig) {
        this.nettyClientConfig = nettyClientConfig;
    }
}
