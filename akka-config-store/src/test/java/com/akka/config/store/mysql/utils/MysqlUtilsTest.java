package com.akka.config.store.mysql.utils;

import com.akka.config.store.mysql.utils.FillStatement;
import com.akka.config.store.mysql.utils.MysqlUtils;
import com.akka.config.store.mysql.utils.ResultFunction;
import com.akka.config.store.mysql.utils.SqlFunction;
import org.junit.Test;

import javax.sql.rowset.serial.SerialBlob;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
    create qiangzhiwei time 2023/2/18
 */public class MysqlUtilsTest {


    @Test
    public void mysqlDml() throws SQLException {
        MysqlUtils.mysqlDml(new FillStatement() {
            @Override
            public String sql() {
                return "insert into config(namespace, environment, version, content) values (?,?,?,?)";
            }

            @Override
            public void fillStatement(PreparedStatement statement) throws SQLException {
                statement.setString(1, "mysqlUtils-test");
                statement.setString(2, "dev");
                statement.setInt(3, 6);
                statement.setBlob(4, new SerialBlob("mysqlUtils-test".getBytes(StandardCharsets.UTF_8)));
            }
        });
    }

    @Test
    public void testMysqlDml1() throws SQLException {
        Map<Integer, Object> data = new HashMap<>();
        data.put(1, "mysqlUtils-test");
        data.put(2, "dev");
        data.put(3, 5);
        data.put(4, new SerialBlob("mysqlUtils-test".getBytes(StandardCharsets.UTF_8)));

        MysqlUtils.mysqlDml(data, new SqlFunction() {
            @Override
            public String sql() {
                return "insert into config(namespace, environment, version, content) values (?,?,?,?)";
            }
        });
    }

    @Test
    public void mysqlSelect() throws SQLException {
        MysqlUtils.mysqlSelect(new ResultFunction() {
            @Override
            public String sql() {
                return "select * from config where id = ?";
            }

            @Override
            public void fillStatement(PreparedStatement statement) throws SQLException {
                statement.setInt(1, 1);
            }

            @Override
            public void result(List<Map<String, Object>> resultSet) {
                System.out.println(resultSet.toString());
            }
        });
    }

    @Test
    public void testMysqlSelect1() throws SQLException, NoSuchFieldException, InstantiationException, IllegalAccessException {
        Map<Integer, Object> data = new HashMap<>();
        data.put(1, 1);
        List<String> list = new ArrayList<>();
        list.add("id");
        final List<MockConfig> mockConfigs = MysqlUtils.mysqlSelect(data, new SqlFunction() {
            @Override
            public String sql() {
                return "select * from config where id = ?";
            }
        }, MockConfig.class,list);
        System.out.println(mockConfigs.toString());
    }

    static class MockConfig {
        private String namespace;
        private String environment;
        private int version;
        private byte[] content;

        public String getNamespace() {
            return namespace;
        }

        public void setNamespace(String namespace) {
            this.namespace = namespace;
        }

        public String getEnvironment() {
            return environment;
        }

        public void setEnvironment(String environment) {
            this.environment = environment;
        }

        public int getVersion() {
            return version;
        }

        public void setVersion(int version) {
            this.version = version;
        }

        public byte[] getContent() {
            return content;
        }

        public void setContent(byte[] content) {
            this.content = content;
        }

        @Override
        public String toString() {
            return "MockConfig{" +
                    "namespace='" + namespace + '\'' +
                    ", environment='" + environment + '\'' +
                    ", version=" + version +
                    ", blob=" + content +
                    '}';
        }
    }
}