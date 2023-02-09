package com.akka.config.ha.controller;/* 
    create qiangzhiwei time 2023/2/8
 */

import com.akka.config.ha.common.PathUtils;
import com.akka.config.ha.etcd.EtcdClient;
import com.akka.config.ha.listener.DataListener;
import com.akka.config.ha.protocol.EtcdEvent;
import com.akka.config.ha.protocol.LeaderElectionRequest;
import com.akka.tools.bus.AsyncEventBus;
import com.akka.tools.bus.Event;
import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class HaController {

    private static final Logger logger = LoggerFactory.getLogger(HaController.class);

    private final EtcdClient etcdClient;

    private final PathConfig haConfig;

    private final String localhost = "";

    private final AsyncEventBus<EtcdEvent> watchEventBus;

    public HaController(EtcdClient etcdClient, PathConfig haConfig) {
        this.etcdClient = etcdClient;
        this.haConfig = haConfig;
        this.watchEventBus = new AsyncEventBus<>();
    }


    public boolean election(String namespace) {

        if (namespace == null || "".equals(namespace)) {
            return false;
        }

        boolean leaderPathResult = false;

        final String leaderPath = PathUtils.createLeaderPatch(haConfig, namespace);
        final LeaderElectionRequest leaderElectionRequest = new LeaderElectionRequest(namespace, localhost);

        try {
            final String electionResult = etcdClient.leaderTimeout(leaderPath,
                    JSON.toJSONString(leaderElectionRequest),
                    haConfig.getElectionTimeout());

            if (electionResult != null) {
                leaderPathResult = true;
            }
        } catch (ExecutionException e) {
        } catch (InterruptedException e) {
            logger.error("[{}] election interrupted, possibility future failed", namespace, e);
        } catch (TimeoutException e) {
            logger.error("[{}] election timeout, time: {}", namespace, haConfig.getElectionTimeout());
        }
        return leaderPathResult;
    }


    public void watch(String key, DataListener listener) {
        DataListener thisDataListener;
        if (listener != null) {
            thisDataListener = listener;
        } else {
            thisDataListener = new DataListener() {

                @Override
                public void onEvent(EtcdEvent etcdEvent) {
                    watchEventBus.addEvent(new Event<>(etcdEvent));
                }

                @Override
                public void onException(Throwable throwable) {
                    logger.error("default etcdEvent listener exception, key: {}", key, throwable);
                }
            };
        }
        etcdClient.watch(key, thisDataListener);
    }

    public AsyncEventBus<EtcdEvent> getWatchEventBus() {
        return watchEventBus;
    }
}
