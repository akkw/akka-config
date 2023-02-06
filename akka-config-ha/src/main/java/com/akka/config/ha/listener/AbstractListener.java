package com.akka.config.ha.listener;/* 
    create qiangzhiwei time 2023/2/5
 */

import com.akka.config.ha.etcd.EtcdClient;
import com.akka.config.ha.protocol.EtcdData;
import com.akka.tools.bus.AsyncEventBus;
import com.akka.tools.bus.Event;

public abstract class AbstractListener {

    private final EtcdClient etcdClient;

    private final String path;

    private final AsyncEventBus<Event<EtcdData>> asyncEventBus;

    public AbstractListener(EtcdClient etcdClient, String path, AsyncEventBus<Event<EtcdData>> asyncEventBus) {
        this.etcdClient = etcdClient;
        this.path = path;
        this.asyncEventBus = asyncEventBus;
    }


    abstract void check();
}
