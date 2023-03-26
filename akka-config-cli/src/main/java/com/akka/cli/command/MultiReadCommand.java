package com.akka.cli.command;/* 
    create qiangzhiwei time 2023/3/26
 */

import com.akka.config.protocol.MutliReadConfigResponse;
import com.akka.config.protocol.ReadConfigResponse;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.beust.jcommander.Parameter;

import java.util.Arrays;
import java.util.Iterator;

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
