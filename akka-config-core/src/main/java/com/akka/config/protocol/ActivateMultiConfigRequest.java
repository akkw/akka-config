package com.akka.config.protocol;/* 
    create qiangzhiwei time 2023/2/14
 */

import java.util.Map;

public class ActivateMultiConfigRequest extends Request {
    private Map<String, Metadata.ClientVersion> activateVersionList;

    public ActivateMultiConfigRequest(String namespace, String environment, Integer version, Map<String, Metadata.ClientVersion> activateVersionList) {
        this.namespace = namespace;
        this.environment = environment;
        this.version = version;
        this.activateVersionList = activateVersionList;
    }

    public Map<String, Metadata.ClientVersion> getActivateVersionList() {
        return activateVersionList;
    }

    public void setActivateVersionList(Map<String, Metadata.ClientVersion> activateVersionList) {
        this.activateVersionList = activateVersionList;
    }
}
