package com.akka.config.protocol;/* 
    create qiangzhiwei time 2023/2/11
 */

import com.alibaba.fastjson.annotation.JSONField;

public class ActivateConfigResponse extends Response {

    public ActivateConfigResponse(int code, byte[] message) {
        super(code, message);
    }

    public ActivateConfigResponse() {
    }
}
