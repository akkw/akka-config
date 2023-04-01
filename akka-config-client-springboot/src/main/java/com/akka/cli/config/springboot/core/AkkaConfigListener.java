package com.akka.cli.config.springboot.core;/* 
    create qiangzhiwei time 2023/3/29
 */

import com.akka.config.api.core.Config;

public interface AkkaConfigListener {

    void active(Config config);


    void verify(Config config);
}
