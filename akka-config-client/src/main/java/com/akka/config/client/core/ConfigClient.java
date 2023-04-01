package com.akka.config.client.core;/* 
    create qiangzhiwei time 2023/2/12
 */

import com.akka.config.api.Client;
import com.akka.config.api.ConfigWatch;
import com.akka.config.api.core.Config;
import com.akka.config.protocol.MetadataResponse;
import com.akka.config.protocol.ReadConfigResponse;
import com.akka.config.protocol.ResponseCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
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
        this.timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {

                    remoteUpdateVersion();

                    localUpdateVersion();
                } catch (Exception ignored) {
                }
            }
        }, 0, clientConfig.getConfigPullIntervalMs());
        this.isRun = true;
    }

    private void localUpdateVersion() {
        final Iterator<Map.Entry<String, Map<String, ConfigMetadataSnapshot>>> namespaceIterator = configMetadataSnapshotMap.entrySet().iterator();
        while (namespaceIterator.hasNext()) {
            final Iterator<ConfigMetadataSnapshot> configMetadataSnapshotIterator = namespaceIterator.next()
                    .getValue()
                    .values()
                    .iterator();

            while (configMetadataSnapshotIterator.hasNext()) {
                final ConfigMetadataSnapshot configMetadataSnapshot = configMetadataSnapshotIterator.next();
                final MetadataResponse newMetadata = configMetadataSnapshot.newMetadata;
                final String namespace = newMetadata.getNamespace();
                final String environment = newMetadata.getEnvironment();

                if (configMetadataSnapshot.isVerifyVersionUpdate()) {
                    try {
                        final ReadConfigResponse response = networkClient.readConfig(namespace, environment, newMetadata.getVerifyVersion());
                        if (response.getCode() == ResponseCode.SUCCESS.code()) {
                            final ConfigWatch configWatch = watchMap.get(namespace).get(environment);
                            final Config config = new Config(response.getBody(), response.getNamespace(), response.getEnvironment(), response.getVersion());
                            configWatch.verify(config);
                        } else {
                            logger.error("failed to read the configuration, please check. namespace: {}, environment: {} code: {}, message: {}",
                                    namespace, environment, response.getCode(), new String(response.getMessage() != null ? response.getMessage() : new byte[0]));
                        }
                    } catch (Exception e) {
                        logger.error("The client failed to verify the configuration. namespace: {}, environment: {}", namespace, environment, e);
                    }
                }

                if (configMetadataSnapshot.isActivateVersionUpdate()) {
                    try {
                        final ReadConfigResponse response = networkClient.readConfig(namespace, environment, newMetadata.getActivateVersion());
                        if (response.getCode() == ResponseCode.SUCCESS.code()) {
                            final ConfigWatch configWatch = watchMap.get(namespace).get(environment);
                            final Config config = new Config(response.getBody(), response.getNamespace(), response.getEnvironment(), response.getVersion());
                            configWatch.activate(config);
                        } else {
                            logger.error("failed to read the configuration, please check. namespace: {}, environment: {} code: {}, message: {}",
                                    namespace, environment, response.getCode(), new String(response.getMessage() != null ? response.getMessage() : new byte[0]));
                        }
                    } catch (Exception e) {
                        logger.error("The client failed to activate the configuration. namespace: {}, environment: {}", namespace, environment, e);
                    }
                }
                configMetadataSnapshot.updateVersion();

            }

        }
    }

    private void remoteUpdateVersion() throws Exception {
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
                    throw e;
                }
            }
        }
    }

    private static class ConfigMetadataSnapshot {
        private MetadataResponse prevMetadata;

        private MetadataResponse newMetadata;


        private boolean isVerifyVersionUpdate() {

            if (prevMetadata == null) {
                return true;
            }

            if (newMetadata == null) {
                return false;
            }

            return !newMetadata.getVerifyVersion().equals(prevMetadata.getVerifyVersion());

        }


        private boolean isActivateVersionUpdate() {
            if (prevMetadata == null) {
                return true;
            }

            if (newMetadata == null) {
                return false;
            }

            return !newMetadata.getActivateVersion().equals(prevMetadata.getActivateVersion());

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
        Map<String, ConfigMetadataSnapshot> steadyConfigMetadataSnapshotMap = configMetadataSnapshotMap.get(namespace);
        if (steadyWatchMap == null) {
            Map<String, ConfigWatch> transitoryWatchMap = new ConcurrentHashMap<>();
            Map<String, ConfigMetadataSnapshot> transitoryMetadataSnapshotMap = new ConcurrentHashMap<>();

            steadyConfigMetadataSnapshotMap = configMetadataSnapshotMap.putIfAbsent(namespace, transitoryMetadataSnapshotMap);
            steadyWatchMap = watchMap.putIfAbsent(namespace, transitoryWatchMap);
            if (steadyConfigMetadataSnapshotMap == null) {
                steadyConfigMetadataSnapshotMap = transitoryMetadataSnapshotMap;
            }
            if (steadyWatchMap == null) {
                steadyWatchMap = transitoryWatchMap;
            }
        }
        if (!steadyConfigMetadataSnapshotMap.containsKey(environment)) {
            steadyConfigMetadataSnapshotMap.put(environment, new ConfigMetadataSnapshot());
        }
        steadyWatchMap.putIfAbsent(environment, configWatch);
    }
}
