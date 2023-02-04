package com.akka.remoting.netty;

import com.akka.remoting.exception.RemotingConnectException;
import com.akka.remoting.exception.RemotingSendRequestException;
import com.akka.remoting.exception.RemotingTimeoutException;
import com.akka.remoting.protocol.Command;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

/*
    create qiangzhiwei time 2023/2/5
 */
public class AkkaNettySocketClientTest {
    private AkkaNettySocketClient client;
    private final String address = "127.0.0.1:9707";
    @Before

    public void before() {
        NettyClientConfig nettyClientConfig = new NettyClientConfig();

        client = new AkkaNettySocketClient(nettyClientConfig);
        client.start();
    }


    @Test
    public void write () throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, InterruptedException {
        Command command =  Command.createResponseCommand(10,"");
        command.setBody("test message".getBytes(StandardCharsets.UTF_8));

        Command response = client.invokeSync(address, command, 1000000);
        Assert.assertNotNull(response);
    }
}