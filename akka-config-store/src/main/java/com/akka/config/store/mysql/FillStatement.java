package com.akka.config.store.mysql;/* 
    create qiangzhiwei time 2023/2/18
 */

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface FillStatement extends SqlFunction {
    void fillStatement(PreparedStatement statement) throws SQLException;
}
