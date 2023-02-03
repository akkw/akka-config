package com.akka.remoting.netty;/* 
    create qiangzhiwei time 2023/2/1
 */

import com.akka.remoting.common.ServiceThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

class NettyEventExecutor extends ServiceThread {
    private static final Logger logger = LoggerFactory.getLogger(NettyEventExecutor.class);
    private final LinkedBlockingQueue<NettyEvent> eventQueue = new LinkedBlockingQueue<NettyEvent>();
    private final int maxSize = 10000;

    private final ChannelEventListener channelEventListener;


    public NettyEventExecutor(ChannelEventListener channelEventListener) {
        this.channelEventListener = channelEventListener;
    }

    public void putNettyEvent(final NettyEvent event) {
        int currentSize = this.eventQueue.size();
        if (currentSize <= maxSize) {
            this.eventQueue.add(event);
        } else {
            logger.warn("event queue size [{}] over the limit [{}], so drop this event {}", currentSize, maxSize, event.toString());
        }
    }

    @Override
    public void run() {
        logger.info(this.getServiceName() + " service started");

        final ChannelEventListener listener = this.channelEventListener;

        while (!this.isStopped()) {
            try {
                NettyEvent event = this.eventQueue.poll(3000, TimeUnit.MILLISECONDS);
                if (event != null && listener != null) {
                    switch (event.getType()) {
                        case IDLE:
                            listener.onChannelIdle(event.getRemoteAddr(), event.getChannel());
                            break;
                        case CLOSE:
                            listener.onChannelClose(event.getRemoteAddr(), event.getChannel());
                            break;
                        case CONNECT:
                            listener.onChannelConnect(event.getRemoteAddr(), event.getChannel());
                            break;
                        case EXCEPTION:
                            listener.onChannelException(event.getRemoteAddr(), event.getChannel());
                            break;
                        default:
                            break;

                    }
                }
            } catch (Exception e) {
                logger.warn(this.getServiceName() + " service has exception. ", e);
            }
        }

        logger.info(this.getServiceName() + " service end");
    }

    @Override
    public String getServiceName() {
        return NettyEventExecutor.class.getSimpleName();
    }
}