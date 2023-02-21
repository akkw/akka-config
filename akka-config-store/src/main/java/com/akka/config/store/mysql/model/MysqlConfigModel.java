package com.akka.config.store.mysql.model;/*
    create qiangzhiwei time 2023/2/21
 */

import java.util.Arrays;

public class MysqlConfigModel {
    private String namespace;
    private String environment;
    private Integer version;
    private byte[] content;

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

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "MysqlConfigModel{" +
                "namespace='" + namespace + '\'' +
                ", environment='" + environment + '\'' +
                ", version=" + version +
                ", content=" + new String(content) +
                '}';
    }
}
