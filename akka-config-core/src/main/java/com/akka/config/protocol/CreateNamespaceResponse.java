package com.akka.config.protocol;/* 
    create qiangzhiwei time 2023/2/13
 */


public class CreateNamespaceResponse extends Response {
    public CreateNamespaceResponse() {
    }

    public CreateNamespaceResponse(int code) {
        super(code);
    }


    public CreateNamespaceResponse(int code, byte[] message) {
        super(code, message);
    }

    @Override
    public String toString() {
        return "CreateNamespaceResponse{" +
                "code=" + code +
                ", message=" + new String(message == null ? new byte[0] : message) +
                '}';
    }
}
