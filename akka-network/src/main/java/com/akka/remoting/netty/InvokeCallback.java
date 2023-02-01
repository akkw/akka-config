package com.akka.remoting.netty;/* 
    create qiangzhiwei time 2023/2/1
 */

import java.util.concurrent.CompletableFuture;

public interface InvokeCallback {

    void complete(ResponseFuture future);
}
