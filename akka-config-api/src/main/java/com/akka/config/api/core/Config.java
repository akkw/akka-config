package com.akka.config.api.core;/*
    create qiangzhiwei time 2023/2/12
 */


public class Config {
    private final String body;
    private final String namespace;
    private final String environment;
    private final int version;

    public Config(String body, String namespace, String environment, int version) {
        this.body = body;
        this.namespace = namespace;
        this.environment = environment;
        this.version = version;
    }

    public String getBody() {
        return body;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getEnvironment() {
        return environment;
    }

    public int getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return "Config{" +
                "body=" + body +
                ", namespace='" + namespace + '\'' +
                ", environment='" + environment + '\'' +
                ", version=" + version +
                '}';
    }
}
