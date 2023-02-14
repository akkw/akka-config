package com.akka.config.protocol;/* 
    create qiangzhiwei time 2023/2/11
 */

public class ActivateConfigResponse extends Response {
    boolean deleted;

    public ActivateConfigResponse() {
    }

    public ActivateConfigResponse(boolean deleted) {
        this.deleted = deleted;
    }
}
