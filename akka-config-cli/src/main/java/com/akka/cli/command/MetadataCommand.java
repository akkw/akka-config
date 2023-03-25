package com.akka.cli.command;/* 
    create qiangzhiwei time 2023/3/26
 */

import com.akka.config.protocol.MetadataResponse;
import com.akka.config.protocol.MutliReadConfigResponse;
import com.beust.jcommander.Parameter;

import java.util.Arrays;

public class MetadataCommand extends BaseCommand {
    @Parameter(names = {"--version", "-v"}, required = true)
    private int version;
    @Parameter(names = {"--address", "-a"}, required = true)
    private String clientIp;
    @Override
    public void doCommand() {
        try {
            final MetadataResponse response = admin.metadata(namespace, environment, clientIp);
            if (response.getCode() != 200) {
                System.out.println(new String(response.getMessage()));
            }
        } catch (Exception e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
        }
    }
}
