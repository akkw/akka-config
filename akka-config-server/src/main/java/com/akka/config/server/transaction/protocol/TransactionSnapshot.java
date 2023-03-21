package com.akka.config.server.transaction.protocol;

import com.akka.config.protocol.ActivateConfigRequest;
import com.akka.config.protocol.Metadata;
import com.akka.config.protocol.Response;

import java.util.List;

public class TransactionSnapshot {
    private final String namespace;

    private final String environment;


    public TransactionSnapshot(String namespace, String environment) {
        this.namespace = namespace;
        this.environment = environment;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getEnvironment() {
        return environment;
    }
}
