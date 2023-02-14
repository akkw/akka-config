package com.akka.config.protocol;/* 
    create qiangzhiwei time 2023/2/11
 */

public class MetadataResponse extends Response {
    private String namespace;
    private String environment;
    private Integer verifyVersion;
    private Integer activateVersion;
    private int globalVersion;


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

    public int getGlobalVersion() {
        return globalVersion;
    }

    public void setGlobalVersion(int globalVersion) {
        this.globalVersion = globalVersion;
    }


}
