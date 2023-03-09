package com.akka.config.protocol;/* 
    create qiangzhiwei time 2023/2/11
 */public enum ResponseCode {

    UNKNOWN(-1, ""),
    SUCCESS(200, ""),
    TIMEOUT(300, ""),
    METADATA_ERROR(301, ""),
    NETWORK_ERROR(302, ""),
    NAMESPACE_EXIST(303,""),
    CONFIG_CREATE_ERROR(304,""),
    CONFIG_DELETE_ERROR(305,""),
    CONFIG_READ_ERROR(306,""),
    CONFIG_NOT_EXIST(307,""),
    METADATA_NOT_EXIST(308,"");


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
