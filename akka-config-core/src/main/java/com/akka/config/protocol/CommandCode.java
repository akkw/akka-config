package com.akka.config.protocol;/* 
    create qiangzhiwei time 2023/2/10
 */

public enum CommandCode {
    METADATA(300),
    READ(301),
    CREATE(302),
    DELETE(303),
    ACTIVATE(304),
    VERIFY(305),
    CREATE_NAMESPACE(306),
    READ_ALL_CONFIG(307),
    ACTIVATE_MULTI(308),
    VERIFY_MULTI_CONFIG(309),
    UNKNOWN(400);


    private final int code;

    CommandCode(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }


    public static CommandCode valueOf(int code) {
        if (code == 300) {
            return METADATA;
        } else if (code == 301) {
            return READ;
        } else if (code == 302) {
            return CREATE;
        } else if (code == 303) {
            return DELETE;
        } else if (code == 304) {
            return ACTIVATE;
        } else if (code == 305) {
            return VERIFY;
        } else if (code == 306) {
            return CREATE_NAMESPACE;
        } else if (code == 307) {
            return READ_ALL_CONFIG;
        } else if (code == 308) {
            return ACTIVATE_MULTI;
        } else if (code == 309) {
            return VERIFY_MULTI_CONFIG;
        } else {
            return UNKNOWN;
        }
    }
}
