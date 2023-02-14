package com.akka.config.protocol;/* 
    create qiangzhiwei time 2023/2/11
 */

public class MetadataRequest extends Request {
    private String clientIp;

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }
}
