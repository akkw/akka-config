package com.akka.config.store;
/*
    create qiangzhiwei time 2023/2/1
 */

import com.akka.tools.api.LifeCycle;

public interface Store extends LifeCycle {

    void write(String namespace,String env, int version, byte[] content);

    void delete(String configId, int version);

    void rollback();

}
