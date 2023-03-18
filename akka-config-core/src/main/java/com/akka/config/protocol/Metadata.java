package com.akka.config.protocol;/*
    create qiangzhiwei time 2023/2/8
 */

import com.google.common.base.Objects;

import java.util.Set;

public class Metadata {


    private Set<Metadata.ClientVersion> activateVersions;

    private Set<Metadata.ClientVersion> verifyVersions;

    private Integer globalVersion;

    private Integer verifyVersion;

    private String namespace;

    private String environment;

    private int maxVersion;

    public Integer getVerifyVersion() {
        return verifyVersion;
    }

    public void setVerifyVersion(Integer verifyVersion) {
        this.verifyVersion = verifyVersion;
    }

    public Set<ClientVersion> getActivateVersions() {
        return activateVersions;
    }

    public void setActivateVersions(Set<Metadata.ClientVersion> activateVersions) {
        this.activateVersions = activateVersions;
    }

    public Set<Metadata.ClientVersion> getVerifyVersions() {
        return verifyVersions;
    }

    public void setVerifyVersions(Set<Metadata.ClientVersion> verifyVersions) {
        this.verifyVersions = verifyVersions;
    }

    public Integer getGlobalVersion() {
        return globalVersion;
    }

    public void setGlobalVersion(Integer globalVersion) {
        this.globalVersion = globalVersion;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public int getMaxVersion() {
        return maxVersion;
    }

    public void setMaxVersion(int maxVersion) {
        this.maxVersion = maxVersion;
    }

    public static class ClientVersion {
        private String client;
        private Integer version;

        public String getClient() {
            return client;
        }

        public void setClient(String client) {
            this.client = client;
        }

        public Integer getVersion() {
            return version;
        }

        public void setVersion(Integer version) {
            this.version = version;
        }


        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ClientVersion that = (ClientVersion) o;
            return Objects.equal(client, that.client);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(client);
        }
    }

    public Metadata copy() {
        Metadata metadata = new Metadata();
        metadata.namespace = this.namespace;
        metadata.environment = this.environment;
        metadata.globalVersion = this.globalVersion;
        metadata.verifyVersion = this.verifyVersion;
        metadata.activateVersions = this.activateVersions;
        metadata.verifyVersions = this.verifyVersions;
        metadata.maxVersion = this.maxVersion;
        return metadata;
    }
}
