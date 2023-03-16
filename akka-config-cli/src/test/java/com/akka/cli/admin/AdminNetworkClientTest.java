package com.akka.cli.admin;

import com.akka.config.client.core.ClientConfig;
import com.akka.config.protocol.*;
import com.akka.remoting.exception.RemotingConnectException;
import com.akka.remoting.exception.RemotingSendRequestException;
import com.akka.remoting.exception.RemotingTimeoutException;
import org.junit.Assert;
import org.junit.Before;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/*
    create qiangzhiwei time 2023/2/14
 */public class AdminNetworkClientTest {
    private final AdminNetworkClient client = new AdminNetworkClient(new ClientConfig());

    @Before
    public void before() {
        client.start();
    }

    @org.junit.Test
    public void createNamespace() throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, InterruptedException {
        final CreateNamespaceResponse response = client.createNamespace("akka-name", "dev");
        Assert.assertEquals(response.getCode(), 200);
    }

    @org.junit.Test
    public void createConfig() throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, InterruptedException {
        final CreateConfigResponse response = client.createConfig("akka-name", "dev", "body");
    }

    @org.junit.Test
    public void deleteConfig() throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, InterruptedException {
        client.deleteConfig("akka-name", "dev", 5);
    }

    @org.junit.Test
    public void readConfig() throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, InterruptedException {
        final ReadConfigResponse readResponse = client.readConfig("mysqlUtils-test", "dev", 6);
        System.out.println(readResponse);
    }


    @org.junit.Test
    public void readAllConfig() throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, InterruptedException {
        final MutliReadConfigResponse mutliReadConfigResponse = client.readAllConfig("akka-name", "dev", 1, 5);
        System.out.println(new String(mutliReadConfigResponse.getBody()));
    }

    @org.junit.Test
    public void activateConfig() throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, InterruptedException {
        List<Metadata.ClientVersion> clientVersionList = new ArrayList<>();
        final Metadata.ClientVersion clientVersion = new Metadata.ClientVersion();
        clientVersion.setClient("127.0.0.1");
        clientVersion.setVersion(3);
        clientVersionList.add(clientVersion);
        client.activateConfig("akka-name", "dev", 3, "127.0.0.1", clientVersionList);
    }

    @org.junit.Test
    public void verifyConfig() throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, InterruptedException {
        List<Metadata.ClientVersion> clientVersionList = new ArrayList<>();
        final Metadata.ClientVersion clientVersion = new Metadata.ClientVersion();
        clientVersion.setClient("127.0.0.1");
        clientVersion.setVersion(3);

        clientVersionList.add(clientVersion);
        client.verifyConfig("akka-name", "dev", 2, "", clientVersionList);
    }

    @org.junit.Test
    public void activateMultiConfig() throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, InterruptedException {
        client.activateMultiConfig("akka-name", "dev", 1, new HashMap<>());
    }

    @org.junit.Test
    public void verifyMultiConfig() throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, InterruptedException {
        client.verifyMultiConfig("akka-name", "dev", 1, new HashMap<>());
    }

    @org.junit.Test
    public void metadata() throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, InterruptedException {
        client.metadata("akka-name", "dev", "127.0.0.1");
    }
}