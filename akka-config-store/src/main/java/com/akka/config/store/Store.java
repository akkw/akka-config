package com.akka.config.store;
/*
    create qiangzhiwei time 2023/2/1
 */

public interface Store {

    void write(String configId, int version, byte[] content);

    void delete(String configId, int version);

    void rollback();

}
