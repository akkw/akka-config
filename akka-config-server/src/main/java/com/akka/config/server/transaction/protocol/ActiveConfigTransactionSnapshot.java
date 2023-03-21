package com.akka.config.server.transaction.protocol;

import com.akka.config.protocol.Metadata;

import java.util.List;

public class ActiveConfigTransactionSnapshot extends TransactionSnapshot {
    private final Integer version;

    private final String clientIp;

    private final List<Metadata.ClientVersion> activateVersionList;


    public ActiveConfigTransactionSnapshot(String namespace, String environment, Integer version, String clientIp, List<Metadata.ClientVersion> activateVersionList) {
        super(namespace, environment);
        this.version = version;
        this.clientIp = clientIp;
        this.activateVersionList = activateVersionList;
    }


    public Integer getVersion() {
        return version;
    }

    public String getClientIp() {
        return clientIp;
    }

    public List<Metadata.ClientVersion> getActivateVersionList() {
        return activateVersionList;
    }
}
