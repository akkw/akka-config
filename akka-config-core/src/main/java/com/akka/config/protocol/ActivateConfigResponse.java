package com.akka.config.protocol;/* 
    create qiangzhiwei time 2023/2/11
 */

public class ActivateConfigResponse extends Response {

    public ActivateConfigResponse(int code, byte[] message) {
        super(code, message);
    }

    public ActivateConfigResponse() {
    }
}
