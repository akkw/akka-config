package com.akka.remoting.netty;


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
}