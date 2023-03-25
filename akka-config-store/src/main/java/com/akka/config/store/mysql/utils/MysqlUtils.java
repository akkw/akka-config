package com.akka.config.store.mysql.utils;/*
    create qiangzhiwei time 2023/2/10
 */

import com.akka.tools.api.LifeCycle;
import com.alibaba.druid.pool.DruidDataSourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

public class MysqlUtils implements LifeCycle {
    private static final Logger logger = LoggerFactory.getLogger(MysqlUtils.class);
    private static DataSource dataSource;
    private static Connection connection;

    private final Properties properties;
    private final AtomicBoolean connected = new AtomicBoolean(false);

    private final AtomicBoolean started = new AtomicBoolean(false);
    public MysqlUtils() {
        this.properties = new Properties();
    }

    @Override
    public void start() {
        if (!started.get()) {
            try {
                properties.load(MysqlUtils.class.getClassLoader().getResourceAsStream("druid.properties"));
            } catch (IOException e) {
                logger.error("Failed to read the database configuration file", e);
            }
            initContent();
        }
    }


    public void initContent() {
        if (connected.get()) {
            return;
        }
        try {
            dataSource = DruidDataSourceFactory.createDataSource(properties);
            connection = dataSource.getConnection();
        } catch (Exception e) {
            logger.error("Failed to initialize the database connection", e);
            connected.compareAndSet(true, false);
        }

        connected.compareAndSet(false, true);
    }
    @Override
    public void stop() {

    }


    public int mysqlDml(FillStatement function) throws SQLException {

        try (PreparedStatement statement = dmlPrepared(function)) {
            function.fillStatement(statement);
            return statement.executeUpdate();
        }
    }

    public int mysqlDml(Map<Integer, Object> data, SqlFunction function) throws SQLException {
        try (PreparedStatement statement = dmlPrepared(function)) {
            for (int i = 0; i < data.size(); i++) {
                statement.setObject(i + 1, data.get(i + 1));
            }
            return statement.executeUpdate();
        }
    }

    private PreparedStatement dmlPrepared(SqlFunction function) throws SQLException {
        if (function == null) {
            throw new NullPointerException("insert function is null!!!");
        }
        final String sql = function.sql();
        if (sql == null || sql.trim().length() == 0) {
            throw new IllegalArgumentException("sql verification fails");
        }
        initContent();
        return connection.prepareStatement(sql);
    }


    public void mysqlSelect(ResultFunction function) throws SQLException {
        PreparedStatement statement = null;
        try {
            final String sql = function.sql();
            if (sql == null || sql.trim().length() == 0) {
                throw new IllegalArgumentException("sql verification fails");
            }
            statement = connection.prepareStatement(sql);
            function.fillStatement(statement);
            select(function, statement);
        } finally {
            if (statement != null) {
                statement.close();
            }
        }
    }


    public <T> List<T> mysqlSelect(Map<Integer, Object> data, SqlFunction function, Class<T> tClass, List<String> excludeColumns) throws SQLException, InstantiationException, IllegalAccessException, NoSuchFieldException {
        PreparedStatement statement = null;
        try {
            final String sql = function.sql();
            if (sql == null || sql.trim().length() == 0) {
                throw new IllegalArgumentException("sql verification fails");
            }
            statement = connection.prepareStatement(sql);
            for (int i = 0; i < data.size(); i++) {
                final Object o = data.get(i + 1);
                statement.setObject(i + 1, o);
            }
            final ResultSet resultSet = statement.executeQuery();
            final ResultSetMetaData resMetadata = resultSet.getMetaData();
            final int columnCount = resMetadata.getColumnCount();
            final List<T> resultList = new ArrayList<>();
            while (resultSet.next()) {
                final T tInstance = tClass.newInstance();
                for (int i = 0; i < columnCount; i++) {
                    final String columnName = resMetadata.getColumnName(i + 1);
                    if (excludeColumns.contains(columnName)) {
                        continue;
                    }
                    final Object columnValue = resultSet.getObject(i + 1);
                    Field field = tClass.getDeclaredField(toCamelCase(columnName));
                    field.setAccessible(true);
                    field.set(tInstance, columnValue);
                }
                resultList.add(tInstance);
            }
            return resultList;
        } finally {
            if (statement != null) {
                statement.close();
            }
        }
    }

    private void select(ResultFunction function, PreparedStatement statement) throws SQLException {
        final ResultSet resultSet = statement.executeQuery();
        final ResultSetMetaData resMetadata = resultSet.getMetaData();
        final int columnCount = resMetadata.getColumnCount();
        final List<Map<String, Object>> resultList = new ArrayList<>();
        while (resultSet.next()) {
            Map<String, Object> resultMap = new HashMap<>();
            for (int i = 0; i < columnCount; i++) {
                final Object columnValue = resultSet.getObject(i + 1);
                final String columnName = resMetadata.getColumnName(i + 1);
                resultMap.put(columnName, columnValue);
            }
            resultList.add(resultMap);
        }
        function.result(resultList);
    }

    private String toCamelCase(String s) {
        if (s == null) {
            return null;
        }
        s = s.toLowerCase();
        StringBuilder sb = new StringBuilder(s.length());
        boolean upperCase = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);

            if (c == '_') {
                upperCase = true;
            } else if (upperCase) {
                sb.append(Character.toUpperCase(c));
                upperCase = false;
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
