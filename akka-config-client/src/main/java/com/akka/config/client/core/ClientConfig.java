package com.akka.config.client.core;/* 
    create qiangzhiwei time 2023/2/12
 */

import com.akka.remoting.netty.NettyClientConfig;

public class ClientConfig {
    private String metadataRemoteAddress = "127.0.0.1:9707";
    private NettyClientConfig nettyClientConfig = new NettyClientConfig();


    public String getMetadataRemoteAddress() {
        return metadataRemoteAddress;
    }

    public void setMetadataRemoteAddress(String metadataRemoteAddress) {
        this.metadataRemoteAddress = metadataRemoteAddress;
    }

    public NettyClientConfig getNettyClientConfig() {
        return nettyClientConfig;
    }

    public void setNettyClientConfig(NettyClientConfig nettyClientConfig) {
        this.nettyClientConfig = nettyClientConfig;
    }
}
