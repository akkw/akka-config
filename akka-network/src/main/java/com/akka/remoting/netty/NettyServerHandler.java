package com.akka.remoting.netty;/* 
    create qiangzhiwei time 2023/2/1
 */

import com.akka.remoting.protocol.Command;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class NettyServerHandler extends SimpleChannelInboundHandler<Command> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Command msg) throws Exception {

    }
}
