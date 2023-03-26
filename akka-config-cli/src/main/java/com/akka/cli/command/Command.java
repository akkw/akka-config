package com.akka.cli.command;/* 
    create qiangzhiwei time 2023/3/26
 */

import com.beust.jcommander.JCommander;

import java.util.HashMap;
import java.util.Map;

public class Command {
    public static void main(String[] args) {
        Map<String, BaseCommand> commandMap = new HashMap<>();
        commandMap.put("create", new CreateCommand());
        commandMap.put("active", new ActiveVersionCommand());
        commandMap.put("namespace", new CreateNamespaceCommand());
        commandMap.put("metadata", new MetadataCommand());
        commandMap.put("multiRead", new MultiReadCommand());
        commandMap.put("read",new ReadCommand());
        commandMap.put("verify", new VerifyVersionCommand());

        JCommander.Builder builder = JCommander.newBuilder();
        for (String cmd : commandMap.keySet()) {
            builder.addCommand(cmd, commandMap.get(cmd));
        }

        JCommander jc = builder.build();
        jc.parse(args);
        if (jc.getParsedCommand() == null) {
            jc.usage();
        } else {
            BaseCommand command = commandMap.get(jc.getParsedCommand());
            if (null != command) {
                command.executor();
            } else {
                jc.usage();
            }
        }
    }
}
