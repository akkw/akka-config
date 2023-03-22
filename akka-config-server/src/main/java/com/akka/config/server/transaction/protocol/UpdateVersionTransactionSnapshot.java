package com.akka.config.server.transaction.protocol;

import com.akka.config.protocol.Metadata;

import java.util.List;

public class UpdateVersionTransactionSnapshot extends TransactionSnapshot {
    private final Integer version;

    private final String clientIp;

    private final List<Metadata.ClientVersion> clientVersionList;


    public UpdateVersionTransactionSnapshot(String namespace, String environment, Integer version, String clientIp, List<Metadata.ClientVersion> clientVersionList) {
        super(namespace, environment);
        this.version = version;
        this.clientIp = clientIp;
        this.clientVersionList = clientVersionList;
    }


    public Integer getVersion() {
        return version;
    }

    public String getClientIp() {
        return clientIp;
    }

    public List<Metadata.ClientVersion> getClientVersionList() {
        return clientVersionList;
    }
}
