package com.akka.config.ha.controller;/* 
    create qiangzhiwei time 2023/2/8
 */

public class PathConfig {

    private String rootPath = "/root/akka";

    private String leader = "/leader/";

    private String metadata = "/metadata/";

    private long electionTimeout = 5000;


    public long getElectionTimeout() {
        return electionTimeout;
    }

    public void setElectionTimeout(long electionTimeout) {
        this.electionTimeout = electionTimeout;
    }

    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }

    public void setLeader(String leader) {
        this.leader = leader;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public String getRootPath() {
        return rootPath;
    }

    public String getLeader() {
        return leader;
    }

    public String getLeaderPath() {
        return rootPath + leader;
    }

    public String getMetadata() {
        return metadata;
    }

    public String getMetadataPath() {
        return rootPath + metadata;
    }
}
