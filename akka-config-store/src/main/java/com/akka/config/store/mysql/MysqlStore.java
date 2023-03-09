package com.akka.config.store.mysql;/* 
    create qiangzhiwei time 2023/2/18
 */

import com.akka.config.store.Store;
import com.akka.config.store.mysql.model.MysqlConfigModel;
import com.akka.config.store.mysql.utils.MysqlUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MysqlStore implements Store {

    @Override
    public void write(String namespace, String env, int version, byte[] content) throws SQLException {
        Map<Integer, Object> data = new HashMap<>();
        data.put(1, namespace);
        data.put(2, env);
        data.put(3, version);
        data.put(4, content);
        // check version >= 0
        MysqlUtils.mysqlDml(data, () -> "insert into config(namespace, environment, version, content) values (?,?,?,?)");
    }

    @Override
    public void delete(String namespace, String environment, int version) throws SQLException {
        Map<Integer, Object> data = new HashMap<>();
        data.put(1, namespace);
        data.put(2, environment);
        data.put(3, version);
        MysqlUtils.mysqlDml(data, () -> "delete from config where namespace=? and environment=? and version = ?");
    }


    @Override
    public void rollback() {

    }

    @Override
    public MysqlConfigModel read(String namespace, String environment, int version) throws SQLException, NoSuchFieldException, InstantiationException, IllegalAccessException {
        Map<Integer, Object> data = new HashMap<>();
        data.put(1, namespace);
        data.put(2, environment);
        data.put(3, version);
        final ArrayList<String> excludeColumnList = new ArrayList<>();
        excludeColumnList.add("id");
        final List<MysqlConfigModel> mysqlConfigModels = MysqlUtils.mysqlSelect(data,
                () -> "select namespace,environment,version,content from config where namespace=? and environment=? and version = ?",
                MysqlConfigModel.class, excludeColumnList);

        return mysqlConfigModels.size() == 0 ? null : mysqlConfigModels.get(0);
    }

    @Override
    public List<MysqlConfigModel> multiRead(String namespace, String environment, int minVersion, int maxVersion) throws SQLException, NoSuchFieldException, InstantiationException, IllegalAccessException {
        Map<Integer, Object> data = new HashMap<>();
        data.put(1, namespace);
        data.put(2, environment);
        data.put(3, minVersion);
        data.put(4, maxVersion);
        final ArrayList<String> excludeColumnList = new ArrayList<>();
        excludeColumnList.add("id");
        final List<MysqlConfigModel> mysqlConfigModelList = MysqlUtils.mysqlSelect(data,
                () -> "select namespace,environment,version,content from config where namespace=? and environment=? and version >= ? and version <= ?",
                MysqlConfigModel.class, excludeColumnList);

        return mysqlConfigModelList;
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }
}
