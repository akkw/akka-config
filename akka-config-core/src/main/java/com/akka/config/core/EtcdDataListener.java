package com.akka.config.core;/* 
    create qiangzhiwei time 2023/2/8
 */

import com.akka.config.ha.common.PathUtils;
import com.akka.config.ha.etcd.EtcdClient;
import com.akka.config.ha.etcd.EtcdConfig;
import com.akka.config.ha.listener.DataListener;
import com.akka.config.ha.protocol.EtcdEvent;
import com.akka.config.protocol.MetadataEvent;
import com.akka.tools.api.LifeCycle;
import com.akka.tools.bus.AsyncEventBus;
import com.akka.tools.bus.Event;
import com.alibaba.fastjson2.JSON;

public class EtcdDataListener implements LifeCycle {

    private final EtcdClient etcdClient;

    private final AsyncEventBus<MetadataEvent> metadataBus;

    private final EtcdConfig etcdConfig;
    public EtcdDataListener(EtcdClient etcdClient, AsyncEventBus<MetadataEvent> metadataBus) {
        this.etcdClient = etcdClient;
        this.metadataBus = metadataBus;
        this.etcdConfig = etcdClient.getConfig();
    }

    @Override
    public void start() {
        etcdClient.watch(etcdConfig.getPathConfig().getMetadataPath(), new MetadataListener());
    }

    @Override
    public void stop() {

    }

    public class MetadataListener implements DataListener {

        @Override
        public void onEvent(EtcdEvent etcdEvent) {
            final String key = etcdEvent.getKey();
            final String metadata = etcdEvent.getValue();
            if (key.contains("metadata")) {
                final MetadataEvent metadataEvent = JSON.parseObject(metadata, MetadataEvent.class);
                EtcdDataListener.this.metadataBus.addEvent(new Event<>(metadataEvent));
            }

        }

        @Override
        public void onException(Throwable throwable) {

        }
    }
}
