package com.akka.cli.config.springboot.annotation;/* 
    create qiangzhiwei time 2023/3/30
 */

public class AkkaPair {
    private String namespace;

    private String environment;

    public AkkaPair() {
    }

    public AkkaPair(String namespace, String environment) {
        this.namespace = namespace;
        this.environment = environment;
    }
}
