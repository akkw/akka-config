package com.akka.cli.command;/* 
    create qiangzhiwei time 2023/3/26
 */

import com.akka.config.protocol.CreateConfigResponse;
import com.akka.remoting.exception.RemotingConnectException;
import com.akka.remoting.exception.RemotingSendRequestException;
import com.akka.remoting.exception.RemotingTimeoutException;
import com.beust.jcommander.Parameter;

import java.util.Arrays;

public class CreateCommand extends BaseCommand {


    @Parameter(names = {"--contents", "-c"})
    private String contents;

    @Override
    public void doCommand() {
        try {
            admin.start();
            final CreateConfigResponse config = admin.createConfig(namespace, environment, contents);
            System.out.println("namespace: " + namespace + " environment: "+environment+" version: " + config.getVersion());

        } catch (Exception e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
        }
    }
}
