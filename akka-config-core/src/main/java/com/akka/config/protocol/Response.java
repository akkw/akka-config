package com.akka.config.protocol;/* 
    create qiangzhiwei time 2023/2/11
 */

public class Response {
    protected int code = ResponseCode.SUCCESS.code();
    protected byte[] message;

    public byte[] getMessage() {
        return message;
    }

    public void setMessage(byte[] message) {
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
