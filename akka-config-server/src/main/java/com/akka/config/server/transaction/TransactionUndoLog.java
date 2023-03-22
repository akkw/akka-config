package com.akka.config.server.transaction;/* 
    create qiangzhiwei time 2023/3/17
 */

import com.akka.config.protocol.Metadata;

public class TransactionUndoLog {
    private Metadata metadata;
    private final long beginTimeMs;

    private final Integer newVersion;

    public TransactionUndoLog(Metadata metadata, long beginTimeMs) {
        this(null, metadata, beginTimeMs);
    }

    public TransactionUndoLog(Integer newVersion, Metadata metadata, long beginTimeMs) {
        this.newVersion = newVersion;
        this.beginTimeMs = beginTimeMs;
        this.metadata = metadata;
    }

    public Integer getNewVersion() {
        return newVersion;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public long getBeginTimeMs() {
        return beginTimeMs;
    }
}
