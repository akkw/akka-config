package com.akka.config.protocol;/* 
    create qiangzhiwei time 2023/2/13
 */

public class MultiReadConfigRequest extends Request {
    private int maxVersion;

    private int minVersion;

    public int getMaxVersion() {
        return maxVersion;
    }

    public void setMaxVersion(int maxVersion) {
        this.maxVersion = maxVersion;
    }

    public int getMinVersion() {
        return minVersion;
    }

    public void setMinVersion(int minVersion) {
        this.minVersion = minVersion;
    }

    public MultiReadConfigRequest(String namespace, String environment,int minVersion, int maxVersion) {
        this.namespace = namespace;
        this.environment = environment;
        this.minVersion = minVersion;
        this.maxVersion = maxVersion;
    }
}
