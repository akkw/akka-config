package com.akka.cli.command;/* 
    create qiangzhiwei time 2023/3/26
 */

import com.akka.config.protocol.ReadConfigResponse;
import com.alibaba.fastjson.JSON;
import com.beust.jcommander.Parameter;

import java.util.Arrays;

public class ReadCommand extends BaseCommand {

    @Parameter(names = {"--version", "-v"}, required = true)
    private int version;
    @Override
    public void doCommand() {
        try {
            final ReadConfigResponse response = admin.readConfig(namespace, environment, version);
            System.out.println(JSON.toJSONString(response));
        } catch (Exception e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
        }
    }
}
