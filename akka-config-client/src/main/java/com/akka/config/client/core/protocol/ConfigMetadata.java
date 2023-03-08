package com.akka.config.client.core.protocol;/* 
    create qiangzhiwei time 2023/2/12
 */

public class ConfigMetadata {
    private String serverIp;
    private String namespace;
    private String environment;
    private  Integer verifyVersion;
    private  Integer activateVersion;

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

    public String getServerIp() {
        return serverIp;
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }

    public Integer getVerifyVersion() {
        return verifyVersion;
    }

    public void setVerifyVersion(Integer verifyVersion) {
        this.verifyVersion = verifyVersion;
    }

    public Integer getActivateVersion() {
        return activateVersion;
    }

    public void setActivateVersion(Integer activateVersion) {
        this.activateVersion = activateVersion;
    }
}
