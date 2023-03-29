package com.akka.cli.config.springboot.annotation;

import com.akka.cli.config.springboot.autoconfigure.AkkaConfigListenerRegister;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;


public class AkkaConfigListenerBeanPostProcessor implements ApplicationContextAware, BeanPostProcessor, InitializingBean {
    private ApplicationContext applicationContext;


    private AkkaConfigListenerRegister akkaConfigListenerRegister;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }


    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> targetClass = AopUtils.getTargetClass(bean);
        AkkaConfig annotation = targetClass.getAnnotation(AkkaConfig.class);

        if (annotation != null && akkaConfigListenerRegister != null) {
            akkaConfigListenerRegister.register(beanName, bean, annotation);
        }
        return bean;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.akkaConfigListenerRegister = applicationContext.getBean(AkkaConfigListenerRegister.class);
    }
}
