package com.akka.remoting.netty;

import com.akka.remoting.protocol.Command;
import io.netty.channel.ChannelHandlerContext;

public abstract class AsyncNettyRequestProcessor implements NettyRequestProcessor {

    public void asyncProcessRequest(ChannelHandlerContext ctx, Command request, RemotingResponseCallback responseCallback) throws Exception {
        Command response = processRequest(ctx, request);
        responseCallback.callback(response);
    }
}