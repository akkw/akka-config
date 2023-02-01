package com.akka.remoting.netty;/* 
    create qiangzhiwei time 2023/2/1
 */

public class NettyServerConfig {

    private int serverCallbackThreads = 0;

    private int serverSelectorThreads = 3;

    private int serverWorkerThreads = 8;

    private int serverSocketBacklog = 1024;

    private int listenPort = 9707;

    public int getListenPort() {
        return listenPort;
    }

    public void setListenPort(int listenPort) {
        this.listenPort = listenPort;
    }

    public int getServerSocketBacklog() {
        return serverSocketBacklog;
    }

    public void setServerSocketBacklog(int serverSocketBacklog) {
        this.serverSocketBacklog = serverSocketBacklog;
    }

    public int getServerWorkerThreads() {
        return serverWorkerThreads;
    }

    public void setServerWorkerThreads(int serverWorkerThreads) {
        this.serverWorkerThreads = serverWorkerThreads;
    }

    public int getServerCallbackThreads() {
        return serverCallbackThreads;
    }

    public void setServerCallbackThreads(int serverCallbackThreads) {
        this.serverCallbackThreads = serverCallbackThreads;
    }

    public int getServerSelectorThreads() {
        return serverSelectorThreads;
    }

    public void setServerSelectorThreads(int serverSelectorThreads) {
        this.serverSelectorThreads = serverSelectorThreads;
    }
}
