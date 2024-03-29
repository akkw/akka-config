package com.akka.config.server.transaction;/* 
    create qiangzhiwei time 2023/3/18
 */

import java.util.Arrays;

public class TransactionResult<T> {
    private boolean success;

    private Exception exception;

    private T result;
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
        return exception.getMessage() != null ? exception.getMessage() : Arrays.toString(exception.getStackTrace());
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }
}
