package com.akka.config.store.mysql;/* 
    create qiangzhiwei time 2023/2/10
 */

import com.akka.config.store.Store;

public class MysqlStore implements Store {
    @Override
    public void write(String configId, int version, byte[] content) {

    }

    @Override
    public void delete(String configId, int version) {

    }

    @Override
    public void rollback() {

    }
}
