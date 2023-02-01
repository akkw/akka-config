package com.akka.remoting.netty;/* 
    create qiangzhiwei time 2023/2/1
 */

import io.netty.channel.ChannelDuplexHandler;

public class NettyConnectManageHandler extends ChannelDuplexHandler {

    private final ChannelEventListener channelEventListener;

    public NettyConnectManageHandler(ChannelEventListener channelEventListener) {
        this.channelEventListener = channelEventListener;
    }
}
