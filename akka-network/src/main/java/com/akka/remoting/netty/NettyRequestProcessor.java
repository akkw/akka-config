package com.akka.remoting.netty;/* 
    create qiangzhiwei time 2023/2/2
 */

import com.akka.remoting.protocol.Command;
import io.netty.channel.ChannelHandlerContext;

public interface NettyRequestProcessor {

    Command processRequest(ChannelHandlerContext ctx, Command request)
            throws Exception;

    boolean rejectRequest();
}
