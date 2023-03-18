package com.akka.config.client.core;/* 
    create qiangzhiwei time 2023/2/12
 */

import com.akka.config.api.Client;
import com.akka.config.api.ConfigWatch;
import com.akka.config.api.core.Config;
import com.akka.config.client.core.protocol.ConfigMetadata;
import com.akka.config.client.core.protocol.WaitUpdateConfig;
import com.akka.config.protocol.MetadataResponse;
import com.akka.config.protocol.ReadConfigResponse;
import com.akka.config.protocol.ResponseCode;
import com.akka.remoting.exception.RemotingConnectException;
import com.akka.remoting.exception.RemotingSendRequestException;
import com.akka.remoting.exception.RemotingTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

public class ConfigClient implements Client {

    private final static Logger logger = LoggerFactory.getLogger(ConfigClient.class);

    private final ClientConfig clientConfig;

    private final Timer timer = new Timer("AkkaConfigClient", true);

    private volatile boolean isRun;

    private final Map<String, Map<String, ConfigMetadata>> metadataMap = new ConcurrentHashMap<>();

    private final Map<String, Map<String, ConfigWatch>> watchs = new ConcurrentHashMap();

    private final Map<String, Map<String, Integer>> useVersion = new ConcurrentHashMap<>();

    private final List<WaitUpdateConfig> waitUpdateConfigList = new ArrayList<>();

    private final ConfigNetworkClient networkClient;

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
                updateAndCompareMetadataAll();

                updateDataConfig();

            }
        }, 0, 3000);
        this.isRun = true;
    }

    private void updateDataConfig() {
        if (!waitUpdateConfigList.isEmpty()) {
            final Iterator<WaitUpdateConfig> iterator = waitUpdateConfigList.iterator();
            while (iterator.hasNext()) {
                WaitUpdateConfig waitUpdateConfig = iterator.next();
                final String namespace = waitUpdateConfig.getNamespace();
                final String environment = waitUpdateConfig.getEnvironment();

                final Integer verifyVersion = waitUpdateConfig.getVerifyVersion();
                final Integer activateVersion = waitUpdateConfig.getActivateVersion();


                final Map<String, ConfigMetadata> namespaceMap = metadataMap.get(namespace);

                ConfigMetadata configMetadata = null;
                if (namespaceMap != null) {
                    configMetadata = namespaceMap.get(environment);
                }

                if (configMetadata == null) {
                    return;
                }

                try {
                    if (activateVersion != null && !Objects.equals(activateVersion, configMetadata.getActivateVersion())) {

                        final ReadConfigResponse readConfigResp = networkClient.readConfig(namespace, environment, activateVersion);

                        if (readConfigResp.getCode() == ResponseCode.SUCCESS.code()) {
                            final ConfigWatch configWatch = watchs.get(namespace).get(environment);
                            Config config = new Config(readConfigResp.getBody(), readConfigResp.getNamespace(),
                                    readConfigResp.getEnvironment(), readConfigResp.getVersion());
                            configWatch.activate(config);
                            saveVersion(namespace, environment, activateVersion, true);
                        } else {
                            logger.error("read config failed code: {}, namespace: {}, environment: {}", readConfigResp.getCode(),
                                    readConfigResp.getNamespace(), readConfigResp.getEnvironment());
                        }
                    }


                    if (verifyVersion != null && !Objects.equals(verifyVersion, configMetadata.getVerifyVersion())) {
                        final ReadConfigResponse readConfigResp = networkClient.readConfig(namespace, environment, verifyVersion);
                        if (readConfigResp.getCode() == ResponseCode.SUCCESS.code()) {
                            final ConfigWatch configWatch = watchs.get(namespace).get(environment);
                            Config config = new Config(readConfigResp.getBody(), readConfigResp.getNamespace(),
                                    readConfigResp.getEnvironment(), readConfigResp.getVersion());
                            configWatch.verify(config);
                            saveVersion(namespace, environment, verifyVersion, false);
                        } else {
                            logger.error("read config failed code: {}, namespace: {}, environment: {}", readConfigResp.getCode(),
                                    readConfigResp.getNamespace(), readConfigResp.getEnvironment());
                        }
                    }
                } catch (Exception e) {
                    logger.error("pull config thread failed", e);
                }
                iterator.remove();
            }
        }


    }

    private void saveVersion(String namespace, String environment, Integer version, boolean activate) {
        final Map<String, ConfigMetadata> namespaceMap = metadataMap.get(namespace);

        ConfigMetadata configMetadata = null;
        if (namespaceMap != null) {
            configMetadata = namespaceMap.get(environment);
            if (activate) {
                configMetadata.setActivateVersion(version);
            } else {
                configMetadata.setVerifyVersion(version);
            }
        }
    }

    @Override
    public void stop() {

    }

    @Override
    public void watch(String namespace, String environment, ConfigWatch configWatch) {
        if (!isRun) {
            throw new RuntimeException("configClient isRun: " + isRun);
        }

        final Map<String, ConfigWatch> watchMap = watchs.computeIfAbsent(namespace, (k) -> new ConcurrentHashMap<>());
        watchMap.put(environment, configWatch);
        final Map<String, ConfigMetadata> metadataMapEvn = metadataMap.computeIfAbsent(namespace, (k) -> new ConcurrentHashMap<>());
        metadataMapEvn.put(environment, new ConfigMetadata());

    }


    private void updateAndCompareMetadataAll() {
        for (Map.Entry<String, Map<String, ConfigWatch>> namespaceEntry : watchs.entrySet()) {
            final String namespace = namespaceEntry.getKey();
            final Map<String, ConfigWatch> envMap = namespaceEntry.getValue();
            for (String environment : envMap.keySet()) {
                try {
                    updateAndCompareMetadata(namespace, environment);
                } catch (Exception e) {
                    logger.error("namespace: {} environment: {} metadata update error", namespace, environment, e);
                }
            }
        }
    }

    private void updateAndCompareMetadata(String namespace, String environment) throws RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException, InterruptedException {
        final MetadataResponse metadataResp = networkClient.metadata(namespace, environment, "127.0.0.1");
        if (metadataResp.getCode() == ResponseCode.SUCCESS.code()) {

            Map<String, ConfigMetadata> namespaceMap = metadataMap.get(namespace);
            if (namespaceMap != null) {
                final ConfigMetadata clientMetadata = namespaceMap.get(environment);

                if (clientMetadata == null) {
                    return;
                }

                if (!Objects.equals(clientMetadata.getVerifyVersion(), metadataResp.getVerifyVersion())) {
                    waitUpdateConfigList.add(new WaitUpdateConfig(metadataResp.getVerifyVersion(), null, namespace, environment));
                }
                if (metadataResp.getActivateVersion() != null && !Objects.equals(clientMetadata.getActivateVersion(), metadataResp.getActivateVersion())) {
                    waitUpdateConfigList.add(new WaitUpdateConfig(null, metadataResp.getActivateVersion(), namespace, environment));
                }
            }
        } else {
            throw new RuntimeException(new String(metadataResp.getMessage()));
        }
    }

}
