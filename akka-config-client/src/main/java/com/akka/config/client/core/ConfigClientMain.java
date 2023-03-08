package com.akka.config.client.core;/* 
    create qiangzhiwei time 2023/3/8
 */

import com.akka.config.api.ConfigWatch;
import com.akka.config.api.core.Config;

import java.util.concurrent.CountDownLatch;

public class ConfigClientMain {
    public static void main(String[] args) throws InterruptedException {
        ConfigClient client = new ConfigClient(new ClientConfig());
        client.start();
        client.watch("akka-name", "dev", new ConfigWatch() {
            @Override
            public boolean verify(Config config) {
                System.out.println("verify" + config.toString());
                return false;
            }

            @Override
            public void activate(Config config) {
                System.out.println("activate" + config.toString());
            }
        });
        new CountDownLatch(1).await();
    }
}
