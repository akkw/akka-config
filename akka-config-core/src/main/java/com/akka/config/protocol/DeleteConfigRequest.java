package com.akka.config.protocol;/* 
    create qiangzhiwei time 2023/2/11
 */

public class DeleteConfigRequest extends Request {


    public DeleteConfigRequest(String namespace, String environment, Integer version) {
        this.namespace = namespace;
        this.environment = environment;
        this.version = version;
    }


}
