package com.akka.config.protocol;/* 
    create qiangzhiwei time 2023/2/11
 */

import java.util.List;
import java.util.Map;

public class VerifyConfigRequest extends Request {
    private List<Metadata.ClientVersion> verifyVersionList;
    private String clientIp;

    public VerifyConfigRequest() {
    }

    public VerifyConfigRequest(String namespace, String environment, Integer version, String clientIp, List<Metadata.ClientVersion> verifyVersionList) {
        this.namespace = namespace;
        this.environment = environment;
        this.version = version;
        this.clientIp = clientIp;
        this.verifyVersionList = verifyVersionList;
    }

    public List<Metadata.ClientVersion> getVerifyVersionList() {
        return verifyVersionList;
    }

    public void setVerifyVersionList(List<Metadata.ClientVersion> verifyVersionList) {
        this.verifyVersionList = verifyVersionList;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }
}
