package com.akka.config.protocol;/* 
    create qiangzhiwei time 2023/2/11
 */

import com.alibaba.fastjson.annotation.JSONField;

public class Response {
    protected int code = ResponseCode.SUCCESS.code();
    protected byte[] message;

    public Response() {
    }

    public Response(int code) {
        this.code = code;
    }


    @JSONField(serialize = false)
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

    public Response(int code, byte[] message) {
        this.code = code;
        this.message = message;
    }
}
