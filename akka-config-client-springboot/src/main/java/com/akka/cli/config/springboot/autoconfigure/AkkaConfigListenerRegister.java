package com.akka.cli.config.springboot.autoconfigure;

import com.akka.cli.config.springboot.annotation.AkkaConfig;
import com.akka.cli.config.springboot.core.AkkaConfigListener;
import com.akka.cli.config.springboot.core.DefaultAkkaConfigClient;
import com.akka.cli.config.springboot.support.AkkaConfigProperties;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.GenericApplicationContext;
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
        final Class<?> aClass = AopProxyUtils.ultimateTargetClass(bean);
        if (!AkkaConfigListener.class.isAssignableFrom(bean.getClass())) {
            throw new IllegalStateException(aClass + " not be  instance of " + AkkaConfigListener.class.getName());
        }
        final String[] namespace = annotation.namespace();
        final String[] environment = annotation.environment();
        if (namespace.length != environment.length) {
            throw new IllegalArgumentException(beanName + " the namespace cannot be paired with the environment");
        }

        GenericApplicationContext genericApplicationContext = (GenericApplicationContext) applicationContext;
        final String name = DefaultAkkaConfigClient.class.getName();


        if (!genericApplicationContext.containsBeanDefinition(name)) {
            genericApplicationContext.registerBean(name, DefaultAkkaConfigClient.class,
                    () -> createDefaultAkkaConfigClient(name, bean, annotation));
        }
        final DefaultAkkaConfigClient defaultAkkaConfigClient = genericApplicationContext.getBean(name, DefaultAkkaConfigClient.class);
        if (!defaultAkkaConfigClient.isStarted()) {
            defaultAkkaConfigClient.start();
        }
        for (int i = 0; i < namespace.length; i++) {
            defaultAkkaConfigClient.watch(namespace[i], environment[i], (AkkaConfigListener) bean);
        }
    }

    private DefaultAkkaConfigClient createDefaultAkkaConfigClient(String name, Object bean, AkkaConfig annotation) {
        DefaultAkkaConfigClient defaultAkkaConfigClient = new DefaultAkkaConfigClient();
        defaultAkkaConfigClient.setAddress(akkaConfigProperties.getAddress());
        return defaultAkkaConfigClient;
    }


}
