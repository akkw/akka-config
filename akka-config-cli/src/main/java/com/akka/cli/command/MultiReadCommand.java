package com.akka.cli.command;/* 
    create qiangzhiwei time 2023/3/26
 */

import com.akka.config.protocol.MutliReadConfigResponse;
import com.akka.config.protocol.ReadConfigResponse;
import com.beust.jcommander.Parameter;

import java.util.Arrays;

public class MultiReadCommand extends BaseCommand {

    @Parameter(names = {"--version", "-v"}, required = true)
    private int version;
    @Parameter(names = {"--max", "-h"}, required = true)
    private int max;
    @Parameter(names = {"--min", "-l"}, required = true)
    private int min;
    @Override
    public void doCommand() {
        try {
            final MutliReadConfigResponse response = admin.readAllConfig(namespace, environment, min, max);
            if (response.getCode() != 200) {
                System.out.println(new String(response.getMessage()));
            }
        } catch (Exception e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
        }
    }
}
