package com.akka.config.protocol;/* 
    create qiangzhiwei time 2023/2/11
 */

import java.util.List;

public class Request {
    protected String namespace;
    protected String environment;
    protected int version;


    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
}
