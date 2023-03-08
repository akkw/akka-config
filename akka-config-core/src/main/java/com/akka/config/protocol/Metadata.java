package com.akka.config.protocol;/*
    create qiangzhiwei time 2023/2/8
 */

import com.google.common.base.Objects;

import java.util.Set;

public class Metadata {


    private Set<Metadata.ClientVersion> activateVersions;

    private Set<Metadata.ClientVersion> verifyVersions;

    private int globalVersion;

    private int verifyVersion;

    private String namespace;

    private String environment;

    private int maxVersion;

    public int getVerifyVersion() {
        return verifyVersion;
    }

    public void setVerifyVersion(int verifyVersion) {
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

    public int getGlobalVersion() {
        return globalVersion;
    }

    public void setGlobalVersion(int globalVersion) {
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

    public static class ConfigRemotePath {
        private String dBRemoteAddress;
        private String cacheRemoteAddress;

        public String getDBRemoteAddress() {
            return dBRemoteAddress;
        }

        public void setDBRemoteAddress(String dBRemoteAddress) {
            this.dBRemoteAddress = dBRemoteAddress;
        }

        public String getCacheRemoteAddress() {
            return cacheRemoteAddress;
        }

        public void setCacheRemoteAddress(String cacheRemoteAddress) {
            this.cacheRemoteAddress = cacheRemoteAddress;
        }
    }
}
