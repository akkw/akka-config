package com.akka.config.store.mysql.utils;/*
    create qiangzhiwei time 2023/2/18
 */

import java.util.List;
import java.util.Map;

public interface ResultFunction extends FillStatement {
    void result(List<Map<String, Object>> result);
}
