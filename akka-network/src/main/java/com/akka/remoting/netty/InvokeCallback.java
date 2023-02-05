package com.akka.remoting.netty;/* 
    create qiangzhiwei time 2023/2/1
 */


public interface InvokeCallback {

    void complete(ResponseFuture future);
}
