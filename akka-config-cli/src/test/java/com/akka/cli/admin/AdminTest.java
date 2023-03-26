package com.akka.cli.admin;

import com.akka.config.client.core.ClientConfig;
import com.akka.config.protocol.*;
import com.akka.remoting.exception.RemotingConnectException;
import com.akka.remoting.exception.RemotingSendRequestException;
import com.akka.remoting.exception.RemotingTimeoutException;
import org.junit.Assert;
import org.junit.Before;

import java.util.ArrayList;
import java.util.List;

/*
    create qiangzhiwei time 2023/2/14
 */public class AdminTest {
    private final Admin client = new Admin(new ClientConfig());

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
        final CreateConfigResponse response = client.createConfig("akka-name", "dev", "createConfig");
    }

    @org.junit.Test
    public void deleteConfig() throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, InterruptedException {
        client.deleteConfig("akka-name", "dev", 5);
    }

    @org.junit.Test
    public void readConfig() throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, InterruptedException {
        final ReadConfigResponse readResponse = client.readConfig("akka-name", "dev", 6);
        System.out.println(readResponse);
    }


    @org.junit.Test
    public void readAllConfig() throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, InterruptedException {
        final MutliReadConfigResponse mutliReadConfigResponse = client.readAllConfig("akka-name", "dev", 1, 5);
        System.out.println(new String(mutliReadConfigResponse.getBodyBytes()));
    }

    @org.junit.Test
    public void activateConfig() throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, InterruptedException {
        List<Metadata.ClientVersion> clientVersionList = new ArrayList<>();
        final Metadata.ClientVersion clientVersion = new Metadata.ClientVersion();
        clientVersion.setClient("127.0.0.1");
        clientVersion.setVersion(6);
        clientVersionList.add(clientVersion);


        final Metadata.ClientVersion clientVersion1 = new Metadata.ClientVersion();
        clientVersion1.setClient("127.0.0.2");
        clientVersion1.setVersion(6);
        clientVersionList.add(clientVersion1);

        client.activateConfig("akka-name", "dev", 6, "127.0.0.1", clientVersionList);
    }

    @org.junit.Test
    public void verifyConfig() throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, InterruptedException {
        List<Metadata.ClientVersion> clientVersionList = new ArrayList<>();
        final Metadata.ClientVersion clientVersion = new Metadata.ClientVersion();
        clientVersion.setClient("127.0.0.1");
        clientVersion.setVersion(3);
        clientVersionList.add(clientVersion);
        final Metadata.ClientVersion clientVersion1 = new Metadata.ClientVersion();
        clientVersion1.setClient("127.0.0.2");
        clientVersion1.setVersion(5);
        clientVersionList.add(clientVersion1);
        client.verifyConfig("akka-name", "dev", 3, "", clientVersionList);
    }

    @org.junit.Test
    public void metadata() throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, InterruptedException {
        final MetadataResponse metadata = client.metadata("akka-name", "dev", "127.0.0.2");
        System.out.println(metadata);
    }
}