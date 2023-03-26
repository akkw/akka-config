package com.akka.cli.command;/* 
    create qiangzhiwei time 2023/3/26
 */

import com.akka.cli.admin.Admin;
import com.akka.config.client.core.ClientConfig;
import com.akka.remoting.exception.RemotingConnectException;
import com.akka.remoting.exception.RemotingSendRequestException;
import com.akka.remoting.exception.RemotingTimeoutException;
import com.beust.jcommander.Parameter;

public abstract class BaseCommand {

    private final ClientConfig clientConfig = new ClientConfig();
    protected final Admin admin = new Admin(clientConfig);

    @Parameter(names = {"--address", "-a"}, description = "server address")
    protected void address(String address) {
        clientConfig.setRemoteAddress(address);
    }

    @Parameter(names = {"--namespace", "-n"}, description = "namespace")
    protected String namespace;

    @Parameter(names = {"--environment", "-e"}, description = "environment")
    protected String environment;


    public void executor() {
       try {
           admin.start();
           doCommand();
       } finally {
           admin.stop();
       }
    }
    public abstract void doCommand();
}
