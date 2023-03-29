package com.akka.cli.config.command;/*
    create qiangzhiwei time 2023/3/26
 */

import com.akka.config.protocol.MutliReadConfigResponse;
import com.alibaba.fastjson.JSON;
import com.beust.jcommander.Parameter;

import java.util.Arrays;

public class MultiReadCommand extends BaseCommand {

    @Parameter(names = {"--max", "-h"}, required = true)
    private int max;
    @Parameter(names = {"--min", "-l"}, required = true)
    private int min;
    @Override
    public void doCommand() {
        try {
            final MutliReadConfigResponse response = admin.readAllConfig(namespace, environment, min, max);
            System.out.println(JSON.toJSONString(response));

        } catch (Exception e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
        }
    }
}
