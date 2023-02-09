package com.akka.config.protocol;/* 
    create qiangzhiwei time 2023/2/8
 */

public class MetadataEvent {

    private String namespace;

    private Metadata metadata;


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
