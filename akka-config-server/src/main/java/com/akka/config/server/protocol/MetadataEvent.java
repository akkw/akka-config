package com.akka.config.server.protocol;/*
    create qiangzhiwei time 2023/2/8
 */

import com.akka.config.protocol.Metadata;

public class MetadataEvent {

    private String environment;

    private String namespace;

    private Metadata metadata;


    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }
}
