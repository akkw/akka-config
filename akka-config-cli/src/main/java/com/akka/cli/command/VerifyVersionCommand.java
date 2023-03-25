package com.akka.cli.command;/* 
    create qiangzhiwei time 2023/3/26
 */

import com.akka.config.protocol.ActivateConfigResponse;
import com.akka.config.protocol.Metadata;
import com.akka.config.protocol.VerifyConfigResponse;
import com.beust.jcommander.Parameter;

import java.util.Arrays;
import java.util.List;

public class VerifyVersionCommand extends BaseCommand{
    @Parameter(names = {"--version", "-v"} , required = true)
    private Integer version;
    @Parameter(names = {"--list", "-l"})
    private List<Metadata.ClientVersion> verifyVersionList;
    @Override
    public void doCommand() {
        try {
            final VerifyConfigResponse response = admin.verifyConfig(namespace, environment, version, "", verifyVersionList);
            if (response.getCode() != 200) {
                System.out.println(new String(response.getMessage()));
            }
        } catch (Exception e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
        }
    }
}
