package com.akka.config.ha.listener;/* 
    create qiangzhiwei time 2023/2/5
 */

import com.akka.config.ha.protocol.EtcdEvent;

public interface DataListener {

    void onEvent(EtcdEvent etcdEvent);

    void onException(Throwable throwable);
}
