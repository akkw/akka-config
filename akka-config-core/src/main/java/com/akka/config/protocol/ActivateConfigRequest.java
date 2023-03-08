package com.akka.config.protocol;/* 
    create qiangzhiwei time 2023/2/11
 */

import java.util.List;


public class ActivateConfigRequest extends Request {

    private List<Metadata.ClientVersion> activateVersionList;
    private String clientIp;

    public ActivateConfigRequest(String namespace, String environment, Integer version, String clientIp, List<Metadata.ClientVersion> activateVersionList) {
        this.namespace = namespace;
        this.environment = environment;
        this.version = version;
        this.clientIp = clientIp;
        this.activateVersionList = activateVersionList;
    }

    public List<Metadata.ClientVersion> getActivateVersionList() {
        return activateVersionList;
    }

    public void setActivateVersionList(List<Metadata.ClientVersion> activateVersionList) {
        this.activateVersionList = activateVersionList;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }
}
