package com.akka.config.ha.etcd;



/*
    create qiangzhiwei time 2023/2/9
 */

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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

    @org.junit.Test
    public void putIfAbsent() throws ExecutionException, InterruptedException {
        etcdClient.del("/root/akka/metadata/akka-name/environment/dev2", "1");
        boolean result = etcdClient.putIfAbsent("/root/akka/metadata/akka-name/environment/dev2", "1");
        Assert.assertTrue(result);
        result = etcdClient.putIfAbsent("/root/akka/metadata/akka-name/environment/dev2", "1");
        Assert.assertFalse(result);
    }

    @Test
    public void get() throws ExecutionException, InterruptedException {
        System.out.println(etcdClient.get("/root/akka/metadata/akka-name/environment/dev").getValue());;
    }
}