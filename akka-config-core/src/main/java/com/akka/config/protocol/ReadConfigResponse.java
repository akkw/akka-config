package com.akka.config.protocol;/* 
    create qiangzhiwei time 2023/2/11
 */


public class ReadConfigResponse extends Response {
    private byte[] body;
    private String namespace;
    private String environment;
    private Integer version;



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

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return "ReadConfigResponse{" +
                "body=" + new String(body) +
                ", namespace='" + namespace + '\'' +
                ", environment='" + environment + '\'' +
                ", version=" + version +
                '}';
    }
}
