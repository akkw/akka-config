package com.akka.config.protocol;/* 
    create qiangzhiwei time 2023/2/13
 */

public class CreateNamespaceRequest extends Request {

    public CreateNamespaceRequest(String namespace, String environment) {
        this.namespace = namespace;
        this.environment = environment;
    }
}
