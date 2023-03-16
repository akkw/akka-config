package com.akka.config.server.transaction;/* 
    create qiangzhiwei time 2023/3/16
 */

import com.akka.config.ha.common.PathUtils;
import com.akka.config.ha.etcd.EtcdClient;
import com.akka.config.protocol.Metadata;
import com.alibaba.fastjson2.JSON;
import javafx.util.Pair;

import java.util.concurrent.ExecutionException;

public class CreateConfigTransactionSnapshot extends TransactionSnapshot {
    private final String namespace;
    private final String environment;

    private final byte[] contents;
    private final TransactionThread worker;
    private final Once once = Once.create();


    public CreateConfigTransactionSnapshot(String namespace, String environment, byte[] contents, EtcdClient etcdClient) {
        super(etcdClient);
        this.namespace = namespace;
        this.environment = environment;
        this.worker = this.new TransactionThread(namespace + "_" + environment);
        this.contents = contents;
    }

    public void down() {
        once.down();
    }




    public class TransactionThread extends Thread {

        public TransactionThread(String name) {
            super(name);
        }

        @Override
        public void run() {
            try {
                final Pair<String, String> etcdMetadataPair = etcdClient.get(
                        PathUtils.createEnvironmentPath(etcdConfig.getPathConfig(), namespace, environment));
                final Metadata metadata = JSON.parseObject(etcdMetadataPair.getValue(), Metadata.class);
                final int maxVersion = metadata.getMaxVersion();
                final int newVersion = maxVersion + 1;
                final long transactionBeginTime = System.currentTimeMillis();

                final TransactionUndoLog transactionMetadata = new TransactionUndoLog(newVersion, maxVersion, namespace, environment, transactionBeginTime);
                final String undoLogPath = PathUtils.createUndoLogPath(etcdConfig.getPathConfig(), namespace, environment, TransactionKind.CREATE_CONFIG.name());
                etcdClient.put(undoLogPath, JSON.toJSONString(transactionMetadata));



            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }


    public void start() {
        if (once.isDown()) {
            throw new RuntimeException(String.format("The transaction has been executed, namespace: %s, environment: %s", namespace, environment));
        }
        this.worker.start();
    }
}
