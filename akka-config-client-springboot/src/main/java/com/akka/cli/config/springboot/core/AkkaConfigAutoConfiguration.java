package com.akka.cli.config.springboot.core;

import com.akka.cli.config.springboot.autoconfigure.AkkaConfigListenerConfiguration;
import com.akka.cli.config.springboot.autoconfigure.AkkaConfigListenerRegister;
import com.akka.cli.config.springboot.support.AkkaConfigProperties;
import com.akka.config.client.core.ConfigClient;
import org.springframework.beans.BeansException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@EnableConfigurationProperties(AkkaConfigProperties.class)
@ConditionalOnClass(ConfigClient.class)
@ConditionalOnProperty(prefix = "akka.config", value = "address")
@Import({AkkaConfigListenerConfiguration.class, AkkaConfigListenerRegister.class})
public class AkkaConfigAutoConfiguration implements ApplicationContextAware {

    private ApplicationContext applicationContext;
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
