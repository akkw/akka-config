package com.akka.config.protocol;/* 
    create qiangzhiwei time 2023/2/8
 */

import java.util.List;

public class Metadata {

    private List<AssignVersion> assignVersionList;

    private String useVersion;

    private String namespace;

    private String leaderIp;

    private ConfigRemotePath configRemotePath;

    public List<AssignVersion> getAssignVersionList() {
        return assignVersionList;
    }

    public void setAssignVersionList(List<AssignVersion> assignVersionList) {
        this.assignVersionList = assignVersionList;
    }

    public String getUseVersion() {
        return useVersion;
    }

    public void setUseVersion(String useVersion) {
        this.useVersion = useVersion;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getLeaderIp() {
        return leaderIp;
    }

    public void setLeaderIp(String leaderIp) {
        this.leaderIp = leaderIp;
    }

    public ConfigRemotePath getConfigRemotePath() {
        return configRemotePath;
    }

    public void setConfigRemotePath(ConfigRemotePath configRemotePath) {
        this.configRemotePath = configRemotePath;
    }

    public static class AssignVersion {
        private String client;
        private String version;

        public String getClient() {
            return client;
        }

        public void setClient(String client) {
            this.client = client;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
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
