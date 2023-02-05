package com.akka.remoting.netty;/* 
    create qiangzhiwei time 2023/2/4
 */

import com.akka.remoting.CommandReceived;
import com.akka.remoting.protocol.Command;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class NettyClientHandler extends SimpleChannelInboundHandler<Command> {
    private final CommandReceived commandReceived;

    public NettyClientHandler(CommandReceived commandReceived) {
        this.commandReceived = commandReceived;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Command msg) throws Exception {
        commandReceived.processMessageReceived(ctx, msg);
    }
}
