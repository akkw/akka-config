package com.akka.cli.command;/* 
    create qiangzhiwei time 2023/3/26
 */

import com.akka.config.protocol.CreateNamespaceResponse;
import com.akka.config.protocol.MetadataResponse;
import com.beust.jcommander.Parameter;

import java.util.Arrays;

public class CreateNamespaceCommand extends BaseCommand {

    @Override
    public void doCommand() {
        try {
            final CreateNamespaceResponse response = admin.createNamespace(namespace, environment);
            if (response.getCode() != 200) {
                System.out.println(new String(response.getMessage()));
            }
        } catch (Exception e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
        }
    }
}
