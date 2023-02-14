package com.akka.config.protocol;/*
    create qiangzhiwei time 2023/2/8
 */

import java.util.Map;

public class Metadata {


    private Map<String, Metadata.ClientVersion> activateVersions;

    private Map<String, Metadata.ClientVersion> verifyVersions;

    private int globalVersion;

    private String namespace;

    private String environment;

    private int maxVersion;

    public Map<String, ClientVersion> getActivateVersions() {
        return activateVersions;
    }

    public void setActivateVersions(Map<String, ClientVersion> activateVersions) {
        this.activateVersions = activateVersions;
    }

    public Map<String, ClientVersion> getVerifyVersions() {
        return verifyVersions;
    }

    public void setVerifyVersions(Map<String, ClientVersion> verifyVersions) {
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
    }

    public static class ConfigRemotePath {
        private String DBRemoteAddress;
        private String cacheRemoteAddress;

        public String getDBRemoteAddress() {
            return DBRemoteAddress;
        }

        public void setDBRemoteAddress(String DBRemoteAddress) {
            this.DBRemoteAddress = DBRemoteAddress;
        }

        public String getCacheRemoteAddress() {
            return cacheRemoteAddress;
        }

        public void setCacheRemoteAddress(String cacheRemoteAddress) {
            this.cacheRemoteAddress = cacheRemoteAddress;
        }
    }
}
