package com.akka.config.server.transaction;/* 
    create qiangzhiwei time 2023/3/16
 */

public enum TransactionKind {
    CREATE_CONFIG("metadata"),
    ACTIVATE_VERSION("metadata"),
    VERIFY_VERSION("metadata");

    private final String kind;
    TransactionKind(String kind) {
        this.kind = kind;
    }

    public String kind() {
        return kind;
    }
}
