package com.akka.config.server.transaction.protocol;

public class CreateConfigTransactionSnapshot extends TransactionSnapshot {
    private final byte[] contents;

    public CreateConfigTransactionSnapshot(String namespace, String environment, byte[] contents) {
        super(namespace, environment);
        this.contents = contents;
    }

    public byte[] getContents() {
        return contents;
    }
}
