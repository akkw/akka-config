package com.akka.config.protocol;/* 
    create qiangzhiwei time 2023/2/13
 */

public class ReadAllConfigRequest extends Request {
    public ReadAllConfigRequest(String namespace, String environment) {
        this.namespace = namespace;
        this.environment = environment;
    }
}
