package com.akka.config.protocol;/* 
    create qiangzhiwei time 2023/2/11
 */

public class CreateConfigResponse extends Response {
    private int serverVersion;
    private int serverAddress;
    private int configVersion;

    public CreateConfigResponse() {
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public int getServerVersion() {
        return serverVersion;
    }

    public void setServerVersion(int serverVersion) {
        this.serverVersion = serverVersion;
    }

    public int getServerAddress() {
        return serverAddress;
    }

    public void setServerAddress(int serverAddress) {
        this.serverAddress = serverAddress;
    }
}
