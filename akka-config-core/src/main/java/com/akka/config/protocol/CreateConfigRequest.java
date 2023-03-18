package com.akka.config.protocol;/* 
    create qiangzhiwei time 2023/2/11
 */

public class CreateConfigRequest extends Request {
    private byte[] contents;


    public CreateConfigRequest(String namespace, String environment, byte[] contents) {
        this.namespace = namespace;
        this.environment = environment;
        this.contents = contents;
    }

    public byte[] getContents() {
        return contents;
    }

    public void setContents(byte[] contents) {
        this.contents = contents;
    }


}
