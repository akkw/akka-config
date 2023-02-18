package com.akka.config.ha.etcd;/* 
    create qiangzhiwei time 2023/2/5
 */

import com.akka.config.ha.controller.PathConfig;

public class EtcdConfig {
    private String endpoints = "http://127.0.0.1:2379";

    private long createLeaseIdTimeout = 5000;

    private long leaseLiveTimeout = 5;

    private String username = "root";
    private String password = "root";

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    private PathConfig pathConfig = new PathConfig();

    public PathConfig getPathConfig() {
        return pathConfig;
    }

    public void setPathConfig(PathConfig pathConfig) {
        this.pathConfig = pathConfig;
    }

    public long getLeaseLiveTimeout() {
        return leaseLiveTimeout;
    }

    public void setLeaseLiveTimeout(long leaseLiveTimeout) {
        this.leaseLiveTimeout = leaseLiveTimeout;
    }

    public String getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(String endpoints) {
        this.endpoints = endpoints;
    }

    public void setCreateLeaseIdTimeout(long createLeaseIdTimeout) {
        this.createLeaseIdTimeout = createLeaseIdTimeout;
    }

    public long getCreateLeaseIdTimeout() {
        return createLeaseIdTimeout;
    }

}
