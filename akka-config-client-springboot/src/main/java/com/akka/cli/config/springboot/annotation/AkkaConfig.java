package com.akka.cli.config.springboot.annotation;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AkkaConfig {
    String[] namespace() default "";

    String[] environment() default "";
}
