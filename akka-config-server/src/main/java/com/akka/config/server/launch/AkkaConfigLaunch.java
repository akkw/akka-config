package com.akka.config.server.launch;/* 
    create qiangzhiwei time 2023/2/14
 */

import com.akka.config.ha.etcd.EtcdClient;
import com.akka.config.ha.etcd.EtcdConfig;
import com.akka.config.server.core.ServerController;

public class AkkaConfigLaunch {
    public static void main(String[] args) {
        ServerController serverController = new ServerController(new EtcdClient(new EtcdConfig()));
        serverController.start();
    }
}
