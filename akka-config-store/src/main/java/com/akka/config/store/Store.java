package com.akka.config.store;
/*
    create qiangzhiwei time 2023/2/1
 */

import com.akka.config.store.mysql.model.MysqlConfigModel;
import com.akka.tools.api.LifeCycle;

import java.sql.SQLException;

public interface Store extends LifeCycle {

    void write(String namespace,String environment, int version, byte[] content) throws SQLException;

    void delete(String namespace,String environment, int version) throws SQLException;

    void rollback();

    MysqlConfigModel read(String namespace, String environment, int version) throws SQLException, NoSuchFieldException, InstantiationException, IllegalAccessException;
}
