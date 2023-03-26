package com.akka.cli.command;/* 
    create qiangzhiwei time 2023/3/26
 */

import com.akka.config.protocol.CreateNamespaceResponse;
import com.akka.config.protocol.MetadataResponse;
import com.alibaba.fastjson.JSON;
import com.beust.jcommander.Parameter;

import java.util.Arrays;

public class CreateNamespaceCommand extends BaseCommand {

    @Override
    public void doCommand() {
        try {
            final CreateNamespaceResponse response = admin.createNamespace(namespace, environment);
            System.out.println(JSON.toJSONString(response));
        } catch (Exception e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
        }
    }
}
