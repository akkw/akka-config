package com.akka.cli.command;/* 
    create qiangzhiwei time 2023/3/26
 */

import com.akka.config.protocol.ReadConfigResponse;
import com.akka.config.protocol.VerifyConfigResponse;
import com.beust.jcommander.Parameter;

import java.util.Arrays;

public class ReadCommand extends BaseCommand {

    @Parameter(names = {"--version", "-v"}, required = true)
    private int version;
    @Override
    public void doCommand() {
        try {
            final ReadConfigResponse response = admin.readConfig(namespace, environment, version);
            if (response.getCode() != 200) {
                System.out.println(new String(response.getMessage()));
            }
        } catch (Exception e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
        }
    }
}
