package com.akka.remoting.netty;


import com.akka.remoting.protocol.Command;
import io.netty.channel.ChannelHandlerContext;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

/*
    create qiangzhiwei time 2023/2/5
 */
public class AkkaSocketServerTest {
    private AkkaNettySocketServer socketServer;


    @Before
    public void before() {
        NettyServerConfig nettyServerConfig = new NettyServerConfig();
        socketServer = new AkkaNettySocketServer(nettyServerConfig);
        socketServer.start();
    }

    @Test
    public void start() throws InterruptedException {
        new CountDownLatch(1).await();
    }


    @Test
    public void testProcessor() throws InterruptedException {
        socketServer.registerProcessor(10, new NettyRequestProcessor() {
            @Override
            public Command processRequest(ChannelHandlerContext ctx, Command request) throws Exception {
                System.out.println(request);
                return Command.createResponseCommand(10, "server handler success");
            }

            @Override
            public boolean rejectRequest() {
                return false;
            }
        }, null);
        new CountDownLatch(1).await();
    }
}