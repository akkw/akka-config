package com.akka.config.client.core;/* 
    create qiangzhiwei time 2023/2/12
 */

import com.akka.config.api.Client;
import com.akka.config.api.ConfigWatch;
import com.akka.config.client.core.protocol.ClientMetadata;
import com.akka.config.client.core.protocol.WaitUpdateConfig;
import com.akka.config.protocol.MetadataResponse;
import com.akka.config.protocol.ResponseCode;
import com.akka.remoting.exception.RemotingConnectException;
import com.akka.remoting.exception.RemotingSendRequestException;
import com.akka.remoting.exception.RemotingTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ConfigClient implements Client {

    private final static Logger logger = LoggerFactory.getLogger(ConfigClient.class);

    private final ClientConfig clientConfig;

    private final Timer timer = new Timer("AkkaConfigClient", true);

    private volatile boolean isRun;

    private final Map<String, Map<String, ClientMetadata>> metadataMap = new ConcurrentHashMap<>();

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
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateAndCompareMetadataAll();

                updateDataConfig();

            }
        }, 0, 3000);
    }

    private void updateDataConfig() {
        if (!waitUpdateConfigList.isEmpty()) {

            for (final WaitUpdateConfig waitUpdateConfig : waitUpdateConfigList) {
                final String namespace = waitUpdateConfig.getNamespace();
                final String environment = waitUpdateConfig.getEnvironment();


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

        final Map<String, ConfigWatch> watchMap = watchs.computeIfAbsent(namespace, (k) -> {
            return new ConcurrentHashMap<>();
        });
        watchMap.put(namespace, configWatch);
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
        final MetadataResponse metadataResp = networkClient.metadata(namespace, environment);
        if (metadataResp.getCode() == ResponseCode.SUCCESS.code()) {
            Map<String, ClientMetadata> namespaceMap = metadataMap.get(namespace);
            if (namespaceMap == null) {
                namespaceMap = new ConcurrentHashMap<>();
            } else {
                final ClientMetadata clientMetadata = namespaceMap.get(environment);
                if (clientMetadata.getVerifyVersion() != metadataResp.getVerifyVersion() ||
                        clientMetadata.getActivateVersion() != metadataResp.getActivateVersion()) {
                    waitUpdateConfigList.add(new WaitUpdateConfig(metadataResp.getVerifyVersion(), metadataResp.getActivateVersion(), namespace, environment));
                }
            }
            final ClientMetadata clientMetadata = new ClientMetadata();
            clientMetadata.setNamespace(metadataResp.getNamespace());
            clientMetadata.setEnvironment(metadataResp.getEnvironment());
            clientMetadata.setVerifyVersion(metadataResp.getVerifyVersion());
            clientMetadata.setActivateVersion(metadataResp.getActivateVersion());
            namespaceMap.put(environment, clientMetadata);

        } else {
            throw new RuntimeException(new String(metadataResp.getMessage()));
        }
    }

}
