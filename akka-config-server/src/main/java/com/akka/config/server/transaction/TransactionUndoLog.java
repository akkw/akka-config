package com.akka.config.server.transaction;/* 
    create qiangzhiwei time 2023/3/17
 */

public class TransactionUndoLog {
    private final int newVersion;

    private final int maxVersion;

    private final String namespace;

    private final String environment;

    private final long beginTime;

    public TransactionUndoLog(int newVersion, int maxVersion, String namespace, String environment, long beginTime) {
        this.namespace = namespace;
        this.beginTime = beginTime;
        this.newVersion = newVersion;
        this.maxVersion = maxVersion;
        this.environment = environment;
    }

    public int getNewVersion() {
        return newVersion;
    }

    public int getMaxVersion() {
        return maxVersion;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getEnvironment() {
        return environment;
    }

    public long getBeginTime() {
        return beginTime;
    }
}
