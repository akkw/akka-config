package com.akka.config.protocol;/* 
    create qiangzhiwei time 2023/2/11
 */

public class CreateConfigResponse extends Response {
    private Integer version;

    public CreateConfigResponse() {
    }

    public CreateConfigResponse(int code) {
        super(code);
    }

    public CreateConfigResponse(int code, byte[] error) {
        super(code, error);
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }
}
