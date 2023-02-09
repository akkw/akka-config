package com.akka.config.ha.protocol;/* 
    create qiangzhiwei time 2023/2/8
 */

public class LeaderElectionRequest {

    private String namespace;
    private String leaderIp;

    public LeaderElectionRequest(String namespace, String leaderIp) {
        this.namespace = namespace;
        this.leaderIp = leaderIp;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getLeaderIp() {
        return leaderIp;
    }

    public void setLeaderIp(String leaderIp) {
        this.leaderIp = leaderIp;
    }
}
