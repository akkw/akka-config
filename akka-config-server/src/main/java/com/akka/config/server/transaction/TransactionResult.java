package com.akka.config.server.transaction;/* 
    create qiangzhiwei time 2023/3/18
 */

public class TransactionResult {
    private boolean success;

    private Exception exception;

    private String message;

    public TransactionResult() {
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    public String getMessage() {
        return exception.getMessage();
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
