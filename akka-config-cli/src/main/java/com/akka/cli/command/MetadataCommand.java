package com.akka.cli.command;/* 
    create qiangzhiwei time 2023/3/26
 */

import com.akka.config.protocol.MetadataResponse;
import com.akka.config.protocol.MutliReadConfigResponse;
import com.alibaba.fastjson.JSON;
import com.beust.jcommander.Parameter;

import java.util.Arrays;

public class MetadataCommand extends BaseCommand {
    @Parameter(names = {"--version", "-v"}, required = true)
    private int version;
    @Parameter(names = {"--clientIp", "-ip"}, required = true)
    private String clientIp;
    @Override
    public void doCommand() {
        try {
            final MetadataResponse response = admin.metadata(namespace, environment, clientIp);
            System.out.println(JSON.toJSONString(response));
        } catch (Exception e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
        }
    }
}
