package com.akka.config.protocol;/* 
    create qiangzhiwei time 2023/2/13
 */


import com.alibaba.fastjson.annotation.JSONField;

public class CreateNamespaceResponse extends Response {
    public CreateNamespaceResponse() {
    }

    public CreateNamespaceResponse(int code) {
        super(code);
    }


    public CreateNamespaceResponse(int code, byte[] message) {
        super(code, message);
    }

    @JSONField
    public String message() {
        return new String(message);
    }
    @Override
    public String toString() {
        return "CreateNamespaceResponse{" +
                "code=" + code +
                ", message=" + new String(message == null ? new byte[0] : message) +
                '}';
    }
}
