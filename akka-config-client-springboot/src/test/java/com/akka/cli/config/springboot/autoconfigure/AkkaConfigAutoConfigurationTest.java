package com.akka.cli.config.springboot.autoconfigure;


import com.akka.cli.config.springboot.annotation.AkkaConfig;
import com.akka.cli.config.springboot.core.AkkaConfigListener;
import com.akka.config.api.core.Config;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Objects;
import java.util.concurrent.CountDownLatch;

import static org.assertj.core.api.Assertions.assertThat;

/*
    create qiangzhiwei time 2023/4/1
 */
public class AkkaConfigAutoConfigurationTest {
    private ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AkkaConfigAutoConfiguration.class));
    @Test
    public void testAkkaConfigAnnotation() throws InterruptedException {
        runner.withPropertyValues("akka.config.address=127.0.0.1:9707")
                .withUserConfiguration(TestAutoConfig.class)
                .run((context -> {
                    assertThat(context).getBean(TestAkkaConfigListener.class);
                }));
        new CountDownLatch(1).await();
    }

    @Configuration
    static class TestAutoConfig {
        @Bean
        public Object configListener() {
            return new TestAkkaConfigListener();
        }
    }

    @AkkaConfig(namespace = {"akka-name"}, environment = {"dev"})
    static class TestAkkaConfigListener implements AkkaConfigListener {

        @Override
        public void active(Config config) {
            System.out.println(config);
        }

        @Override
        public void verify(Config config) {
            System.out.println(config);
        }
    }
}