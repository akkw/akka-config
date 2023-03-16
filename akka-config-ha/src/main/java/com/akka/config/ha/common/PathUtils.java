package com.akka.config.ha.common;/* 
    create qiangzhiwei time 2023/2/8
 */

import com.akka.config.ha.controller.PathConfig;

public class PathUtils {


    public static String parseNamespace(final String key) {
        if (key != null) {
            final String[] keyNode = key.split("/");
            if ("namespace".equals(keyNode[3]) && "metadata".equals(keyNode[4])) {
                return keyNode[5];
            }
        }
        return null;
    }

    public static String createLeaderPath(final PathConfig haConfig, final String namespace) {
        return  haConfig.getLeaderPath() + namespace;
    }

    public static String createEnvironmentPath(final PathConfig haConfig, final String namespace, final String environment) {
        return  haConfig.getMetadataPath() + namespace + "/environment/" + environment;
    }

    public static String createUndoLogPath(final PathConfig haConfig, final String namespace, final String environment, final String kind) {
        return  haConfig.getUndoLog() + namespace + "/environment/" + environment + "/" + kind;
    }
}
