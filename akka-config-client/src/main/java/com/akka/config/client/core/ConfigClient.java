package com.akka.config.client.core;/* 
    create qiangzhiwei time 2023/2/12
 */

import com.akka.config.api.Client;
import com.akka.config.api.ConfigWatch;
import com.akka.config.protocol.MetadataResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ConfigClient implements Client {

    private final static Logger logger = LoggerFactory.getLogger(ConfigClient.class);

    private final ClientConfig clientConfig;

    private final Timer timer = new Timer("AkkaConfigClient", true);

    private volatile boolean isRun;

    private final ConfigNetworkClient networkClient;


    private final Map<String, Map<String, ConfigWatch>> watchMap = new ConcurrentHashMap<>();

    private final Map<String, Map<String, ConfigMetadataSnapshot>> configMetadataSnapshotMap = new ConcurrentHashMap<>();

    public ConfigClient(ClientConfig clientConfig) {
        this.clientConfig = clientConfig;
        this.networkClient = new ConfigNetworkClient(clientConfig);
    }


    @Override
    public void start() {
        this.networkClient.start();
        this.timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                remoteUpdateVersion();

                localUpdateVersion();
            }
        }, 0, 3000);
        this.isRun = true;
    }

    private void localUpdateVersion() {

    }

    private void remoteUpdateVersion() {
        final Iterator<Map.Entry<String, Map<String, ConfigWatch>>> namespaceIterator = watchMap.entrySet().iterator();
        while (namespaceIterator.hasNext()) {
            final Map.Entry<String, Map<String, ConfigWatch>> namespaceEntry = namespaceIterator.next();
            final String namespace = namespaceEntry.getKey();
            final Iterator<String> environmentIterator = namespaceEntry.getValue().keySet().iterator();
            while (environmentIterator.hasNext()) {
                final String environment = environmentIterator.next();
                try {
                    final MetadataResponse metadata = networkClient.metadata(namespace, environment, "127.0.0.1");
                    final ConfigMetadataSnapshot configMetadataSnapshot = configMetadataSnapshotMap.get(namespace).get(environment);
                    configMetadataSnapshot.newMetadata = metadata;
                } catch (Exception e) {
                    logger.error("Failed to obtain metadata. namespace: {} environment: {}", namespace, environment, e);
                }
            }
        }
    }

    private static class ConfigMetadataSnapshot {
        private MetadataResponse prevMetadata;

        private MetadataResponse newMetadata;


        private boolean isVerifyVersionUpdate() {
            if (newMetadata != null && prevMetadata == null) {
                return true;
            }

            if (newMetadata != null) {
                return newMetadata.getVerifyVersion().equals(prevMetadata.getVerifyVersion());
            }

            return false;
        }



        private boolean isActivateVersionUpdate() {
            if (newMetadata != null && prevMetadata == null) {
                return true;
            }

            if (newMetadata != null) {
                return newMetadata.getActivateVersion().equals(prevMetadata.getActivateVersion());
            }

            return false;
        }

        private void updateVersion() {
            this.prevMetadata = newMetadata;
            this.newMetadata = null;
        }
    }

    @Override
    public void stop() {
        this.timer.purge();
        this.networkClient.stop();
    }

    @Override
    public void watch(String namespace, String environment, ConfigWatch configWatch) {
        if (!isRun) {
            throw new RuntimeException("configClient isRun: " + isRun);
        }
        putConfigWatchIfAbsent(namespace, environment, configWatch);
    }

    private void putConfigWatchIfAbsent(String namespace, String environment, ConfigWatch configWatch) {
        Map<String, ConfigWatch> steadyWatchMap = watchMap.get(namespace);
        if (steadyWatchMap == null) {
            Map<String, ConfigWatch> transitoryWatchMap = new ConcurrentHashMap<>();
            steadyWatchMap = watchMap.putIfAbsent(namespace, transitoryWatchMap);
            if (steadyWatchMap == null) {
                steadyWatchMap = transitoryWatchMap;
            }
        }
        steadyWatchMap.putIfAbsent(environment, configWatch);

        Map<String, ConfigMetadataSnapshot> steadyConfigMetadataSnapshotMap = configMetadataSnapshotMap.get(namespace);
        if (steadyConfigMetadataSnapshotMap == null) {
            Map<String, ConfigMetadataSnapshot> transitoryMetadataSnapshotMap = new ConcurrentHashMap<>();
            steadyConfigMetadataSnapshotMap = configMetadataSnapshotMap.putIfAbsent(namespace, transitoryMetadataSnapshotMap);
            if (steadyConfigMetadataSnapshotMap == null) {
                steadyConfigMetadataSnapshotMap = transitoryMetadataSnapshotMap;
            }
        }

        if (steadyConfigMetadataSnapshotMap.containsKey(environment)) {
            steadyConfigMetadataSnapshotMap.put(environment, new ConfigMetadataSnapshot());
        }

    }
}
