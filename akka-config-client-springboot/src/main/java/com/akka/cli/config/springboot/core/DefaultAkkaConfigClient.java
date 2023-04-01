package com.akka.cli.config.springboot.core;/* 
    create qiangzhiwei time 2023/3/30
 */

import com.akka.config.api.ConfigWatch;
import com.akka.config.api.core.Config;
import com.akka.config.client.core.ClientConfig;
import com.akka.config.client.core.ConfigClient;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class DefaultAkkaConfigClient {

    private String address;

    private ConfigClient client;

    private AtomicBoolean started = new AtomicBoolean();

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }


    public boolean isStarted() {
        return started.get();
    }

    public void start() {
        if (started.compareAndSet(false, true)) {
            final ClientConfig clientConfig = initClientConfig();
            client = new ConfigClient(clientConfig);
            client.start();
        }
    }

    private ClientConfig initClientConfig() {
        final ClientConfig clientConfig = new ClientConfig();
        clientConfig.setRemoteAddress(address);
        return clientConfig;
    }


    public void watch(String namespace, String environment, AkkaConfigListener listener) {
        client.watch(namespace, environment, new ConfigWatch() {
            @Override
            public void verify(Config config) {
                listener.verify(config);
            }

            @Override
            public void activate(Config config) {
                listener.active(config);
            }
        });
    }
}
