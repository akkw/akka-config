package com.akka.config.protocol;/* 
    create qiangzhiwei time 2023/2/11
 */

import java.util.Map;

public class ActivateConfigRequest extends Request {

    private Map<String, Metadata.ClientVersion> activateVersionList;
    private String clientIp;

    public ActivateConfigRequest(String namespace, String environment, Integer version, String clientIp) {
        this.namespace = namespace;
        this.environment = environment;
        this.version = version;
        this.clientIp = clientIp;
    }

    public Map<String, Metadata.ClientVersion> getActivateVersionList() {
        return activateVersionList;
    }

    public void setActivateVersionList(Map<String, Metadata.ClientVersion> activateVersionList) {
        this.activateVersionList = activateVersionList;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }
}
