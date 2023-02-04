package com.akka.remoting;/* 
    create qiangzhiwei time 2023/2/4
 */

import com.akka.remoting.protocol.Command;
import io.netty.channel.ChannelHandlerContext;

public interface CommandReceived {
    void processMessageReceived(ChannelHandlerContext ctx, Command msg);
}
