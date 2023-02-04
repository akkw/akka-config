package com.akka.remoting.netty;/* 
    create qiangzhiwei time 2023/2/1
 */

import com.akka.remoting.CommandReceived;
import com.akka.remoting.protocol.Command;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
@ChannelHandler.Sharable
public class NettyServerHandler extends SimpleChannelInboundHandler<Command> {

    private Logger logger = LoggerFactory.getLogger(NettyServerHandler.class);

    private final CommandReceived commandReceived;

    public NettyServerHandler(HashMap<Integer, Pair<NettyRequestProcessor, ExecutorService>> processorTable,
                              Pair<NettyRequestProcessor, ExecutorService> defaultRequestProcessor,
                              ConcurrentMap<Integer, ResponseFuture> responseTable, CommandReceived commandReceived) {
        this.commandReceived = commandReceived;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Command msg) throws Exception {
        commandReceived.processMessageReceived(ctx, msg);
    }




}
