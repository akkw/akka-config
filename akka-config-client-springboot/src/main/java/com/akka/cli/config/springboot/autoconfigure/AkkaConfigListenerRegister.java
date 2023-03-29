package com.akka.cli.config.springboot.autoconfigure;

import com.akka.cli.config.springboot.annotation.AkkaConfig;
import com.akka.cli.config.springboot.support.AkkaConfigProperties;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;

@Configuration
public class AkkaConfigListenerRegister implements ApplicationContextAware {

    private ConfigurableApplicationContext applicationContext;

    private ConfigurableEnvironment environment;

    private AkkaConfigProperties akkaConfigProperties;


    public AkkaConfigListenerRegister(ConfigurableEnvironment environment,
                                      AkkaConfigProperties akkaConfigProperties) {
        this.akkaConfigProperties = akkaConfigProperties;
        this.environment = environment;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = (ConfigurableApplicationContext) applicationContext;
    }


    public void register(String beanName, Object bean, AkkaConfig annotation) {

    }
}
