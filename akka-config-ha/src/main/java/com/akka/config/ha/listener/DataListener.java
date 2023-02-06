package com.akka.config.ha.listener;/* 
    create qiangzhiwei time 2023/2/5
 */

public interface DataListener {

    void onEvent();

    void onException(Throwable throwable);
}
