package com.akka.config.api;/* 
    create qiangzhiwei time 2023/2/12
 */

import com.akka.tools.api.LifeCycle;

public interface Client extends LifeCycle {
    void watch(String namespace, String environment, ConfigWatch configWatch);
}
