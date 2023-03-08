package com.akka.config.protocol;/* 
    create qiangzhiwei time 2023/2/13
 */

public class MutliReadConfigResponse extends Response {
    private byte[] body;

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

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }
}
