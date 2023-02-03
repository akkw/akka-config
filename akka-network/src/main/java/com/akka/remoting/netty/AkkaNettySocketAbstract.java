package com.akka.remoting.netty;/* 
    create qiangzhiwei time 2023/2/3
 */

import javafx.util.Pair;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;

public class AkkaNettySocketAbstract {
    protected final ConcurrentMap<Integer /* opaque */, ResponseFuture> responseTable =
            new ConcurrentHashMap<Integer, ResponseFuture>(256);

    protected final HashMap<Integer/* request code */, Pair<NettyRequestProcessor, ExecutorService>> processorTable =
            new HashMap<Integer, Pair<NettyRequestProcessor, ExecutorService>>(64);


    protected final NettyEventExecutor nettyEventExecutor;


    protected Pair<NettyRequestProcessor, ExecutorService> defaultRequestProcessor;


    public AkkaNettySocketAbstract(ChannelEventListener listener) {
        this.nettyEventExecutor = new NettyEventExecutor(listener);
    }
}
