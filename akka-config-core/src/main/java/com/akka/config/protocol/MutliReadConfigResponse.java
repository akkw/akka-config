package com.akka.config.protocol;/* 
    create qiangzhiwei time 2023/2/13
 */

import com.alibaba.fastjson.annotation.JSONField;

public class MutliReadConfigResponse extends Response {
    private String body;

    private String namespace;
    private String environment;

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }
    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
