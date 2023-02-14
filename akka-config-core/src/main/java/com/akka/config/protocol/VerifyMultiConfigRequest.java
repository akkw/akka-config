package com.akka.config.protocol;/* 
    create qiangzhiwei time 2023/2/14
 */

import java.util.Map;

public class VerifyMultiConfigRequest extends  Request{
    private Map<String, Metadata.ClientVersion> verifyVersionList;
    public VerifyMultiConfigRequest(String namespace, String environment, Integer version, Map<String, Metadata.ClientVersion> verifyVersionList) {
        this.namespace = namespace;
        this.environment = environment;
        this.version = version;
        this.verifyVersionList = verifyVersionList;
    }

    public Map<String, Metadata.ClientVersion> getVerifyVersionList() {
        return verifyVersionList;
    }

    public void setVerifyVersionList(Map<String, Metadata.ClientVersion> verifyVersionList) {
        this.verifyVersionList = verifyVersionList;
    }
}
