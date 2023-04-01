package com.akka.config.protocol;/* 
    create qiangzhiwei time 2023/2/11
 */


public class ReadConfigResponse extends Response {
    private String body;
    private String namespace;
    private String environment;
    private Integer version;


    public ReadConfigResponse() {
    }

    public ReadConfigResponse(int code) {
        super(code);
    }

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
    public String getBody() {
        return body;
    }

    public void setBody(String body) {
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
