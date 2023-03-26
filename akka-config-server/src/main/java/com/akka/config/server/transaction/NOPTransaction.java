package com.akka.config.server.transaction;/* 
    create qiangzhiwei time 2023/3/18
 */

import com.akka.config.ha.etcd.EtcdClient;

import java.sql.SQLException;
import java.util.concurrent.ExecutionException;

public class NOPTransaction extends Transaction {

    public NOPTransaction(long transactionId, EtcdClient etcdClient, Exception exception) {
        super(null, transactionId, null, null, null);
        this.exception = exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    @Override
    void rollback(TransactionUndoLog transactionUndoLog, boolean prev)  {

    }

    @Override
    void undoLog() throws ExecutionException, InterruptedException {

    }

    @Override
    public void executor() {
    }

    @Override
    public Exception exception() {
        return exception;
    }

    @Override
    void transaction() throws Exception {

    }

    @Override
    Object getResult() {
        return null;
    }

    @Override
    void await() throws InterruptedException {

    }
}
