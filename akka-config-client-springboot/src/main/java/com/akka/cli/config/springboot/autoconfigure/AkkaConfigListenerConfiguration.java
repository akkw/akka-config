package com.akka.cli.config.springboot.autoconfigure;

import com.akka.cli.config.springboot.annotation.AkkaConfigListenerBeanPostProcessor;
import com.akka.cli.config.springboot.core.AkkaConfigAutoConfiguration;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

@Configuration
@AutoConfigureAfter(AkkaConfigAutoConfiguration.class)
public class AkkaConfigListenerConfiguration implements ImportBeanDefinitionRegistrar {
    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        if (!registry.containsBeanDefinition(AkkaConfigListenerBeanPostProcessor.class.getName())) {
            registry.registerBeanDefinition(AkkaConfigListenerBeanPostProcessor.class.getName(),
                    new RootBeanDefinition(AkkaConfigAutoConfiguration.class));
        }
    }
}
