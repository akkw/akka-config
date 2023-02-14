package com.akka.config.protocol;/* 
    create qiangzhiwei time 2023/2/11
 */public enum ResponseCode {

    UNKNOWN(-1, ""),
    SUCCESS(200, ""),
    TIMEOUT(300, ""),
    METADATA_ERROR(301, ""),
    NETWORK_ERROR(302, "");


    private final int code;
    private final String desc;

    ResponseCode(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }


    public int code() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
