package com.akka.config.core;/* 
    create qiangzhiwei time 2023/2/9
 */

import com.akka.config.ha.controller.HaController;
import com.akka.config.ha.etcd.EtcdClient;
import com.akka.config.ha.etcd.EtcdConfig;
import com.akka.config.protocol.MetadataEvent;
import com.akka.tools.api.LifeCycle;
import com.akka.tools.bus.AsyncEventBus;
import com.akka.tools.bus.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Timer;

public class ServerController implements LifeCycle {

    private final static Logger logger = LoggerFactory.getLogger(ServerController.class);

    private final EtcdDataListener etcdDataListener;

    private final AsyncEventBus<MetadataEvent> metadataBus = new AsyncEventBus<>();

    private final EtcdClient etcdClient;

    private final HaController haController;

    private final Timer leaderTimer;

    private final EtcdConfig etcdConfig;

    public ServerController(EtcdClient etcdClient) {
        this.etcdClient = etcdClient;
        this.etcdConfig = etcdClient.getConfig();
        this.leaderTimer = new Timer("CheckNamespaceLeaderThread");
        this.etcdDataListener = new EtcdDataListener(etcdClient, metadataBus);
        this.haController = new HaController(etcdClient, etcdConfig.getPathConfig());
    }

    @Override
    public void start() {
        this.metadataBus.addStation(this::metadataHandler);
        this.etcdDataListener.start();
    }


    @Override
    public void stop() {

    }


    private void metadataHandler(Event<MetadataEvent> metadataEventEvent) {

        final MetadataEvent metadataEvent = metadataEventEvent.getLoad();
        final String namespace = metadataEvent.getNamespace();

        boolean electionResult = haController.election(namespace);

        if (electionResult) {
            logger.info("the [{}] successfully elected the leader.", namespace);
        } else {
            logger.warn("the [{}] failed to elect the leader.", namespace);
        }
    }
}
