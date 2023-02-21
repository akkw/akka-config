package com.akka.config.protocol;/* 
    create qiangzhiwei time 2023/2/11
 */

public class DeleteConfigResponse extends Response {
    public DeleteConfigResponse(int code) {
        super(code);
    }

    public DeleteConfigResponse(int code, byte[] message) {
        super(code, message);
    }
}
