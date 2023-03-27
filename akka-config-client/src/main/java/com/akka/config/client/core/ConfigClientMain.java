package com.akka.config.client.core;/* 
    create qiangzhiwei time 2023/3/8
 */

import com.akka.config.api.ConfigWatch;
import com.akka.config.api.core.Config;
import com.alibaba.fastjson2.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

public class ConfigClientMain {
    private final static Logger logger = LoggerFactory.getLogger(ConfigClientMain.class);
    public static void main(String[] args) throws InterruptedException {

        ConfigClient client = new ConfigClient(new ClientConfig());
        client.start();
        client.watch("akka-name", "dev", new ConfigWatch() {
            @Override
            public boolean verify(Config config) {
                return false;
            }

            @Override
            public void activate(Config config) {
                logger.info("config: {}", JSON.toJSONString(config));
            }
        });
        new CountDownLatch(1).await();
    }
}
