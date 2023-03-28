package com.akka.config.api;/* 
    create qiangzhiwei time 2023/2/12
 */

import com.akka.config.api.core.Config;

public interface ConfigWatch {
    void verify(Config config);

    void activate(Config config);
}
