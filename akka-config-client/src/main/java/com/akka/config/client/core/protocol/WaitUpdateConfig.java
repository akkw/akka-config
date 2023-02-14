package com.akka.config.client.core.protocol;/* 
    create qiangzhiwei time 2023/2/12
 */

public class WaitUpdateConfig {
    private final Integer verifyVersion;
    private final Integer activateVersion;
    private final String namespace;
    private final String environment;


    public WaitUpdateConfig(Integer verifyVersion, Integer activateVersion, String namespace, String environment) {
        this.verifyVersion = verifyVersion;
        this.activateVersion = activateVersion;
        this.namespace = namespace;
        this.environment = environment;
    }


    public Integer getActivateVersion() {
        return activateVersion;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getEnvironment() {
        return environment;
    }

    public Integer getVerifyVersion() {
        return verifyVersion;
    }
}
