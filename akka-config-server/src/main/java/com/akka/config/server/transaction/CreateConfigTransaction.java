package com.akka.config.server.transaction;/* 
    create qiangzhiwei time 2023/3/16
 */

import com.akka.config.ha.common.PathUtils;
import com.akka.config.ha.etcd.EtcdClient;
import com.akka.config.protocol.Metadata;
import com.akka.config.server.exception.TimeoutException;
import com.akka.config.store.Store;
import com.akka.tools.atomic.PaddedAtomicInteger;
import com.akka.tools.retry.Retry;
import com.alibaba.fastjson2.JSON;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class CreateConfigTransaction extends Transaction {

    private final static Logger logger = LoggerFactory.getLogger(CreateConfigTransaction.class);

    private final byte[] contents;

    private final long timeoutMs = 200;


    private final TransactionThread worker;

    private int maxVersion = -1;

    private int newVersion = -1;

    private ThreadPoolExecutor transactionExecutor;

    private final Store store;

    private Metadata etcdMetadata;


    private final CountDownLatch countDownLatch;

    public CreateConfigTransaction(String namespace, String environment, byte[] contents, EtcdClient etcdClient, Store store, String transactionId, String lockKey) {
        super(etcdClient, transactionId, lockKey, namespace, environment);
        this.worker = this.new TransactionThread(namespace + "_" + environment);
        this.contents = contents;
        this.store = store;
        this.countDownLatch = new CountDownLatch(1);
        this.transactionExecutor = new ThreadPoolExecutor(4, 8, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<>(128), new ThreadFactory() {
            final PaddedAtomicInteger counter = new PaddedAtomicInteger();

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "transaction_executor[" + counter.getAndIncrement() + "]");
            }
        });
    }


    public String getNamespace() {
        return namespace;
    }

    public String getEnvironment() {
        return environment;
    }

    public class TransactionThread extends Thread {

        public TransactionThread(String name) {
            super(name);
        }

        @Override
        public void run() {

            try {

                final long beginTimeMs = System.currentTimeMillis();
                prepare();

                Retry.retry(CreateConfigTransaction.this::undoLog);
                undoLogWriteSuccess = true;


                final Future<?> configFuture = transactionExecutor.submit(() -> {
                    try {
                        Retry.retry(CreateConfigTransaction.this::writeConfig);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });

                final Future<?> metadataFuture = transactionExecutor.submit(() -> {
                    try {
                        Retry.retry(CreateConfigTransaction.this::writeMetadata);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });

                while (!configFuture.isDone() && !metadataFuture.isDone()) {
                    final long currentTimeMillis = System.currentTimeMillis();
                    if (currentTimeMillis - beginTimeMs > timeoutMs) {
                        CreateConfigTransaction.this.exception = new TimeoutException("Transaction execution timeout, namespace: "
                                + namespace + "environment: " + environment);
                        configFuture.cancel(true);
                        metadataFuture.cancel(true);
                        rollback(false);
                        break;
                    }
                    TimeUnit.MILLISECONDS.sleep(200);
                }
            } catch (Exception e) {
                try {
                    rollback(false);
                } catch (Exception exc) {
                    logger.error("Transaction rollback failed, namespace: {}, environment: {}", namespace, environment, exc);
                }
                CreateConfigTransaction.this.exception = e;
            } finally {
                down();
            }
        }


    }

    private void prepare() throws ExecutionException, InterruptedException, SQLException {
        checkNoPaas();

        etcdMetadata = metadata(namespace, environment);
        if (etcdMetadata == null) {
            throw new IllegalArgumentException("etcd metadata does not exist");
        }
        final String undoLogPath = PathUtils.createUndoLogPath(etcdConfig.getPathConfig(), namespace, environment, TransactionKey.METADATA.name());
        final Pair<String, String> etcdMetadataPair = etcdClient.get(undoLogPath);
        if (etcdMetadataPair != null) {
            rollback(true);
        }
    }

    private void checkNoPaas() {
        if (namespace == null || "".equals(namespace)) {
            throw new IllegalArgumentException("namespace is null");
        }

        if (environment == null || "".equals(environment)) {
            throw new IllegalArgumentException("environment is null"));
        }

        if (contents == null) {
            throw new IllegalArgumentException("contents is null"));
        }

        if (etcdClient == null) {
            throw new IllegalArgumentException("internal error");
        }
    }

    private void rollback(boolean prev) throws ExecutionException, InterruptedException, SQLException {
        if (undoLogWriteSuccess || prev) {
            etcdClient.put(PathUtils.createEnvironmentPath(etcdConfig.getPathConfig(), namespace, environment), JSON.toJSONString(etcdMetadata));
            store.delete(namespace, environment, newVersion);
        }
    }

    private void writeMetadata() throws ExecutionException, InterruptedException {
        final Metadata copyMetadata = etcdMetadata.copy();
        copyMetadata.setMaxVersion(newVersion);
        etcdClient.put(PathUtils.createEnvironmentPath(etcdConfig.getPathConfig(), namespace, environment), JSON.toJSONString(copyMetadata));
    }

    private void writeConfig() throws SQLException {
        store.write(namespace, environment, newVersion, contents);
    }

    private void undoLog() throws ExecutionException, InterruptedException {
        writeUndoLog();
    }


    private void writeUndoLog() throws ExecutionException, InterruptedException {
        maxVersion = etcdMetadata.getMaxVersion();
        newVersion = maxVersion + 1;
        final long transactionBeginTime = System.currentTimeMillis();
        final TransactionUndoLog transactionMetadata = new TransactionUndoLog(newVersion, maxVersion, namespace, environment, transactionBeginTime);
        final String undoLogPath = PathUtils.createUndoLogPath(etcdConfig.getPathConfig(), namespace, environment, TransactionKey.METADATA.name());
        etcdClient.put(undoLogPath, JSON.toJSONString(transactionMetadata));
    }

    private void deleteUndoLog() throws ExecutionException, InterruptedException {
        final String undoLogPath = PathUtils.createUndoLogPath(etcdConfig.getPathConfig(), namespace, environment, TransactionKey.METADATA.name());
        etcdClient.del(undoLogPath);
    }


    public void executor() {
        if (once.isDown()) {
            throw new RuntimeException(String.format("The transaction has been executed, namespace: %s, environment: %s", namespace, environment));
        }
        this.worker.start();
    }

    private void down() {
        if (once.isDown()) {
            return;
        }
        try {
            deleteUndoLog();
        } catch (Exception ignored) {
        }
        once.down();
        countDownLatch.countDown();
    }

    @Override
    Exception exception() {
        return exception;
    }

    @Override
    void await() throws InterruptedException {
        countDownLatch.await();
    }

}
