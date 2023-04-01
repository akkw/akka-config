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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class CreateConfigTransaction extends Transaction {

    private final static Logger logger = LoggerFactory.getLogger(CreateConfigTransaction.class);

    private final byte[] contents;

    private final long timeoutMs = 200;

    private Integer maxVersion = -1;

    private Integer newVersion;

    private final ThreadPoolExecutor transactionExecutor;

    private final Store store;





    public CreateConfigTransaction(String namespace, String environment, byte[] contents, EtcdClient etcdClient,
                                   Store store, long transactionId, String lockKey) {
        super(etcdClient, transactionId, lockKey, namespace, environment);
        this.contents = contents;
        this.store = store;

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


    void rollback(TransactionUndoLog transactionUndoLog, boolean prev) {
        if (undoLogWriteSuccess || prev) {
            final String etcdMetadata = JSON.toJSONString(transactionUndoLog.getMetadata());
            try {
                logger.debug("rollback etcdMetadata: {}", etcdMetadata);
                etcdClient.put(PathUtils.createEnvironmentPath(etcdConfig.getPathConfig(), namespace, environment), etcdMetadata);
            } catch (Exception ignored) {

            }
            try {

                store.delete(namespace, environment, transactionUndoLog.getNewVersion());
            } catch (Exception ignored) {

            }
        }
    }

    @Override
    void transaction() throws Exception {
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
                break;
            }
            TimeUnit.MILLISECONDS.sleep(200);
        }
        // In order to throw an exception
        configFuture.get();
        metadataFuture.get();
    }

    @Override
    Object getResult() {
        return newVersion;
    }

    private void writeMetadata() throws ExecutionException, InterruptedException, java.util.concurrent.TimeoutException {
        final Metadata copyMetadata = etcdMetadata.copy();
        copyMetadata.setMaxVersion(newVersion);
        writeEtcdMetadata(copyMetadata);
    }



    private void writeConfig() throws SQLException {
        store.write(namespace, environment, newVersion, contents);
    }

    protected void undoLog() throws ExecutionException, InterruptedException, java.util.concurrent.TimeoutException {
        writeUndoLog();
        undoLogWriteSuccess = true;
    }


    private void writeUndoLog() throws ExecutionException, InterruptedException, java.util.concurrent.TimeoutException {
        maxVersion = etcdMetadata.getMaxVersion();
        newVersion = maxVersion + 1;
        transactionUndoLog = new TransactionUndoLog(newVersion, etcdMetadata, beginTimeMs);
        final String undoLogPath = PathUtils.createUndoLogPath(etcdConfig.getPathConfig(), namespace, environment, TransactionKey.METADATA.name());
        etcdClient.put(undoLogPath, JSON.toJSONString(transactionUndoLog));
    }
}
