package com.akka.config.client.core.protocol;/* 
    create qiangzhiwei time 2023/2/12
 */

public class ClientMetadata {
    private String serverIp;
    private String namespace;
    private String environment;
    private  int verifyVersion;
    private  int activateVersion;

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

    public int getVerifyVersion() {
        return verifyVersion;
    }

    public void setVerifyVersion(int verifyVersion) {
        this.verifyVersion = verifyVersion;
    }

    public int getActivateVersion() {
        return activateVersion;
    }

    public void setActivateVersion(int activateVersion) {
        this.activateVersion = activateVersion;
    }
}
