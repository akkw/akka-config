package com.akka.cli.config.command;/*
    create qiangzhiwei time 2023/3/26
 */

import com.akka.config.protocol.ActivateConfigResponse;
import com.akka.config.protocol.Metadata;
import com.alibaba.fastjson.JSON;
import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.Parameter;

import java.util.Arrays;
import java.util.List;

public class ActiveVersionCommand extends BaseCommand{

    @Parameter(names = {"--version", "-v"}, required = true)
    private Integer version;
    @Parameter(names = {"--list", "-l"}, listConverter = ListConverter.class)
    private List<Metadata.ClientVersion> activateVersionList;
    @Override
    public void doCommand() {
        try {
            final ActivateConfigResponse response = admin.activateConfig(namespace, environment, version, "", activateVersionList);
            System.out.println(JSON.toJSONString(response));
        } catch (Exception e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
        }
    }

    public static class ListConverter implements IStringConverter<List<Metadata.ClientVersion>> {
        @Override
        public List<Metadata.ClientVersion> convert(String value) {
            return JSON.parseArray(value, Metadata.ClientVersion.class);
        }
    }
}
