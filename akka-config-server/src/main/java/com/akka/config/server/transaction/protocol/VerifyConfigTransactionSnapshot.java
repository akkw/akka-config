package com.akka.config.server.transaction.protocol;

import com.akka.config.protocol.Metadata;

import java.util.List;

public class VerifyConfigTransactionSnapshot extends TransactionSnapshot {

    private final Integer version;

    private final String clientIp;

    private final List<Metadata.ClientVersion> verifyVersionList;

    public VerifyConfigTransactionSnapshot(String namespace, String environment, Integer version,
                                         String clientIp, List<Metadata.ClientVersion> verifyVersionList) {
        super(namespace, environment);
        this.version = version;
        this.clientIp = clientIp;
        this.verifyVersionList = verifyVersionList;
    }


    public Integer getVersion() {
        return version;
    }

    public String getClientIp() {
        return clientIp;
    }

    public List<Metadata.ClientVersion> getVerifyVersionList() {
        return verifyVersionList;
    }
}
