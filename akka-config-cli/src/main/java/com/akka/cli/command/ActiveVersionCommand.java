package com.akka.cli.command;/* 
    create qiangzhiwei time 2023/3/26
 */

import com.akka.config.protocol.ActivateConfigResponse;
import com.akka.config.protocol.Metadata;
import com.beust.jcommander.Parameter;

import java.util.Arrays;
import java.util.List;

public class ActiveVersionCommand extends BaseCommand{

    @Parameter(names = {"--version", "-v"}, required = true)
    private Integer version;
    @Parameter(names = {"--list", "-l"})
    private List<Metadata.ClientVersion> activateVersionList;
    @Override
    public void doCommand() {
        try {
            final ActivateConfigResponse response = admin.activateConfig(namespace, environment, version, "", activateVersionList);
            if (response.getCode() != 200) {
                System.out.println(new String(response.getMessage()));
            }
        } catch (Exception e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
        }
    }
}
