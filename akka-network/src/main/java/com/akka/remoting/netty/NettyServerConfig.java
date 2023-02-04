package com.akka.remoting.netty;/* 
    create qiangzhiwei time 2023/2/1
 */

public class NettyServerConfig {

    private int serverCallbackThreads = 0;

    private int serverSelectorThreads = 3;

    private int serverWorkerThreads = 8;

    private int serverSocketBacklog = 1024;

    private int listenPort = 9707;

    private int serverOnewaySemaphoreValue = 256;

    private int serverAsyncSemaphoreValue = 64;

    public int getServerOnewaySemaphoreValue() {
        return serverOnewaySemaphoreValue;
    }

    public void setServerOnewaySemaphoreValue(int serverOnewaySemaphoreValue) {
        this.serverOnewaySemaphoreValue = serverOnewaySemaphoreValue;
    }

    public int getServerAsyncSemaphoreValue() {
        return serverAsyncSemaphoreValue;
    }

    public void setServerAsyncSemaphoreValue(int serverAsyncSemaphoreValue) {
        this.serverAsyncSemaphoreValue = serverAsyncSemaphoreValue;
    }

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
