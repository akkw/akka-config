package com.akka.config.ha.etcd;



/*
    create qiangzhiwei time 2023/2/9
 */

import org.junit.Before;

import java.util.concurrent.ExecutionException;

public class EtcdClientTest {


    private EtcdClient etcdClient;

    @Before
    public void before() {
        EtcdConfig config = new EtcdConfig();
        this.etcdClient = new EtcdClient(config);
        this.etcdClient.start();
    }

    @org.junit.Test
    public void checkLeader() throws ExecutionException, InterruptedException {
        System.out.println(etcdClient.checkLeader("root"));
    }
}