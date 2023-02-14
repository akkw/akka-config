package com.akka.config.protocol;/* 
    create qiangzhiwei time 2023/2/11
 */

public class CreateConfigRequest extends Request {
    private byte[] body;


    public CreateConfigRequest(String namespace, String environment, byte[] body) {
        this.namespace = namespace;
        this.environment = environment;
        this.body = body;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }


}
