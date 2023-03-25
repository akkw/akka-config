package com.akka.config.protocol;/* 
    create qiangzhiwei time 2023/2/11
 */

import java.util.Arrays;

public class MetadataResponse extends Response {
    private String namespace;
    private String environment;
    private Integer verifyVersion;
    private Integer activateVersion;


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


    @Override
    public String toString() {
        return "MetadataResponse{" +
                "namespace='" + namespace + '\'' +
                ", environment='" + environment + '\'' +
                ", verifyVersion=" + verifyVersion +
                ", activateVersion=" + activateVersion +
                ", code=" + code +
                ", message=" + Arrays.toString(message) +
                '}';
    }
}
