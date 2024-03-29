package com.akka.remoting.netty;
/*
    create qiangzhiwei time 2023/2/4
 */

import com.akka.remoting.common.NettyNetworkHelper;
import com.akka.remoting.common.RemotingUtil;
import com.akka.remoting.exception.RemotingConnectException;
import com.akka.remoting.exception.RemotingSendRequestException;
import com.akka.remoting.exception.RemotingTimeoutException;
import com.akka.remoting.exception.RemotingTooMuchRequestException;
import com.akka.remoting.protocol.Command;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class AkkaNettySocketClient extends AkkaNettySocketAbstract implements RemotingClient {


    private static final Logger logger = LoggerFactory.getLogger(AkkaNettySocketClient.class);

    private static final long LOCK_TIMEOUT_MILLIS = 3000;

    private final NettyClientConfig config;

    private final Bootstrap bootstrap = new Bootstrap();

    private final EventLoopGroup eventLoopGroupWorker;

    private final Lock lockChannelTables = new ReentrantLock();

    private final ConcurrentMap<String, ChannelWrapper> channelTables = new ConcurrentHashMap<>();

    private final Timer timer = new Timer("ClientHouseKeepingService", true);

    private AtomicReference<List<String>> nameSrvAddrList = new AtomicReference<>();

    private final AtomicReference<String> nameservAddrChoosed = new AtomicReference<>();

    private final AtomicInteger namesrvIndex = new AtomicInteger(initValueIndex());

    private final Lock namesrvChannelLock = new ReentrantLock();

    private final ExecutorService publicExecutor;

    private ExecutorService callbackExecutor;

    private final ChannelEventListener channelEventListener;

    private DefaultEventExecutorGroup defaultEventExecutorGroup;

    private final NettyClientHandler nettyClientHandler;

    private static int initValueIndex() {
        Random r = new Random();

        return Math.abs(r.nextInt() % 999) % 999;
    }

    public AkkaNettySocketClient(final NettyClientConfig config) {
        this(config, null);
    }


    public AkkaNettySocketClient(NettyClientConfig config, ChannelEventListener listener) {
        super(config.getClientOnewaySemaphoreValue(), config.getClientAsyncSemaphoreValue(), listener);

        this.config = config;
        this.channelEventListener = listener;
        this.nettyClientHandler = new NettyClientHandler(this::processMessageReceived);
        int publicThreadNums = this.config.getClientCallbackExecutorThreads();

        if (publicThreadNums <= 0) {
            publicThreadNums = 4;
        }

        this.publicExecutor = Executors.newFixedThreadPool(publicThreadNums, new ThreadFactory() {
            private AtomicInteger threadIndex = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "NettyClientPublicExecutor_" + this.threadIndex.incrementAndGet());
            }
        });

        this.eventLoopGroupWorker = new NioEventLoopGroup(1, new ThreadFactory() {
            private AtomicInteger threadIndex = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, String.format("NettyClientSelector_%d", this.threadIndex.incrementAndGet()));
            }
        });
    }

    @Override
    public void updateNameServerAddressList(List<String> addrs) {
        List<String> old = this.nameSrvAddrList.get();

        boolean update = false;

        if (!addrs.isEmpty()) {
            if (null == old) {
                update = true;
            } else if (addrs.size() != old.size()) {
                update = true;
            } else {
                for (int i = 0; i < addrs.size() && !update; i++) {
                    if (!old.contains(addrs.get(i))) {
                        update = true;
                    }
                }
            }

            if (update) {
                Collections.shuffle(addrs);
                logger.info("name server address updated. NEW : {} , OLD: {}", addrs, old);
                this.nameSrvAddrList.set(addrs);

                if (!addrs.contains(this.nameservAddrChoosed.get())) {
                    this.nameservAddrChoosed.set(null);
                }
            }
        }
    }

    @Override
    public List<String> getNameServerAddressList() {
        return this.nameSrvAddrList.get();
    }

    @Override
    public Command invokeSync(String addr, Command request, long timeoutMillis) throws InterruptedException, RemotingConnectException, RemotingSendRequestException, RemotingTimeoutException {
        long beginStartTime = System.currentTimeMillis();
        final Channel channel = this.getAndCreateChannel(addr);

        if (channel != null && channel.isActive()) {
            try {
                long costTime = System.currentTimeMillis() - beginStartTime;
                if (timeoutMillis < costTime) {
                    throw new RemotingTimeoutException("invokeSync call the addr[" + addr + "] timeout");
                }

                return this.invokeSyncImpl(channel, request, timeoutMillis - costTime);
            } catch (RemotingSendRequestException e) {
                logger.warn("invokeSync: send request exception, so close the channel[{}]", addr);
                this.closeChannel(addr, channel);
                throw e;
            } catch (RemotingTimeoutException e) {
                if (config.isClientCloseSocketIfTimeout()) {
                    this.closeChannel(channel);
                    logger.warn("invokeSync: close socket because of timeout, {}ms, {}", timeoutMillis, addr);
                }
                logger.warn("invokeSync: wait response timeout exception, the channel[{}]", addr);
                throw e;
            }
        } else {
            this.closeChannel(addr, channel);
            throw new RemotingConnectException(addr);
        }
    }

    private Channel getAndCreateChannel(String addr) throws InterruptedException, RemotingConnectException {
        if (null == addr) {
            return getAndCreateNameserverChannel();
        }

        final ChannelWrapper cw = this.channelTables.get(addr);
        if (cw != null) {
            return cw.getChannel();
        }
        return this.createChannel(addr);
    }


    private Channel getAndCreateNameserverChannel() throws InterruptedException, RemotingConnectException {
        String addr = this.nameservAddrChoosed.get();

        if (addr != null) {
            final ChannelWrapper cw = this.channelTables.get(addr);
            if (cw != null && cw.isOK()) {
                return cw.getChannel();
            }
        }

        final List<String> addrList = this.nameSrvAddrList.get();
        if (this.namesrvChannelLock.tryLock(LOCK_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)) {
            try {
                addr = this.nameservAddrChoosed.get();
                if (addr != null) {
                    ChannelWrapper cw = this.channelTables.get(addr);
                    if (cw != null && cw.isOK()) {
                        return cw.getChannel();
                    }
                }

                if (addrList != null && !addrList.isEmpty()) {
                    for (int i = 0; i < addrList.size(); i++) {
                        int index = this.namesrvIndex.getAndIncrement();
                        index = index % addrList.size();
                        final String newAddr = addrList.get(index);

                        this.nameservAddrChoosed.set(newAddr);
                        logger.info("new name server is chosen. OLD: {} , NEW: {}. namesrvIndex = {}", addr, newAddr, namesrvIndex);

                        final Channel channelNew = this.createChannel(newAddr);
                        if (channelNew != null) {
                            return channelNew;
                        }
                    }
                    throw new RemotingConnectException(addrList.toString());
                }
            } finally {
                this.namesrvChannelLock.unlock();
            }
        } else {
            logger.warn("getAndCreateNameserverChannel: try to lock name server, but timeout, {}ms", LOCK_TIMEOUT_MILLIS);
        }
        return null;
    }

    private Channel createChannel(String addr) throws InterruptedException {
        ChannelWrapper cw = this.channelTables.get(addr);
        if (cw != null && cw.isOK()) {
            return cw.getChannel();
        }

        if (this.lockChannelTables.tryLock(LOCK_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)) {
            try {
                boolean createNewConnection;

                cw = this.channelTables.get(addr);
                if (cw != null) {
                    if (cw.isOK()) {
                        return cw.getChannel();
                    } else if (!cw.getChannelFuture().isDone()) {
                        createNewConnection = false;
                    } else {
                        this.channelTables.remove(addr);
                        createNewConnection = true;
                    }
                } else {
                    createNewConnection = true;
                }

                if (createNewConnection) {
                    final ChannelFuture channelFuture = this.bootstrap.connect(NettyNetworkHelper.string2SocketAddress(addr));
                    logger.info("createChannel: begin to connect remote host[{}] asynchronously", addr);
                    cw = new ChannelWrapper(channelFuture);
                    this.channelTables.put(addr, cw);
                }
            } catch (Exception e) {
                logger.error("createChannel: create channel exception", e);
            } finally {
                this.lockChannelTables.unlock();
            }
        } else {
            logger.warn("createChannel: try to lock channel table, but timeout, {}ms", LOCK_TIMEOUT_MILLIS);
        }

        if (cw != null) {
            final ChannelFuture channelFuture = cw.getChannelFuture();
            if (channelFuture.awaitUninterruptibly(this.config.getConnectTimeoutMillis())) {
                if (cw.isOK()) {
                    logger.info("createChannel: connect remote host[{}] success, {}", addr, channelFuture);
                    return cw.getChannel();
                } else {
                    logger.warn("createChannel: connect remote host[" + addr + "] failed, " + channelFuture, channelFuture.cause());
                }
            } else {
                logger.warn("createChannel: connect remote host[{}] timeout {}ms, {}", addr, this.config.getConnectTimeoutMillis(),
                        channelFuture.toString());
            }
        }
        return null;
    }

    @Override
    public void invokeAsync(String addr, Command request, long timeoutMillis, InvokeCallback invokeCallback) throws InterruptedException, RemotingConnectException, RemotingTooMuchRequestException, RemotingTimeoutException, RemotingSendRequestException {
        final long beginStartTime = System.currentTimeMillis();
        final Channel channel = this.getAndCreateChannel(addr);

        if (channel != null && channel.isActive()) {
            try {
                final long costTime = System.currentTimeMillis() - beginStartTime;
                if (timeoutMillis < costTime) {
                    throw new RemotingTooMuchRequestException("invokeAsync call the addr[" + addr + "] timeout");
                }
                this.invokeAsyncImpl(channel, request, timeoutMillis, invokeCallback);
            } catch (RemotingSendRequestException e) {
                logger.warn("invokeAsync: send request exception, so close the channel[{}]", addr);
                this.closeChannel(addr, channel);
                throw e;
            }
        } else {
            this.closeChannel(addr, channel);
            throw new RemotingConnectException(addr);
        }
    }

    @Override
    public void invokeOneway(String addr, Command request, long timeoutMillis) throws InterruptedException, RemotingConnectException, RemotingTooMuchRequestException, RemotingTimeoutException, RemotingSendRequestException {
        final Channel channel = this.getAndCreateChannel(addr);
        if (channel != null && channel.isActive()) {
            try {
                this.invokeOnewayImpl(channel, request, timeoutMillis);
            } catch (RemotingSendRequestException e) {
                logger.warn("invokeOneway: send request exception, so close the channel[{}]", addr);
                this.closeChannel(addr, channel);
                throw e;
            }
        } else {
            this.closeChannel(addr, channel);
            throw new RemotingConnectException(addr);
        }
    }

    @Override
    public void registerProcessor(int requestCode, NettyRequestProcessor processor, ExecutorService executor) {
        ExecutorService executorThis = executor;
        if (null == executor) {
            executorThis = this.publicExecutor;
        }
        final Pair<NettyRequestProcessor, ExecutorService> pair = new Pair<>(processor, executorThis);
        this.processorTable.put(requestCode, pair);
    }

    @Override
    public void setCallbackExecutor(ExecutorService callbackExecutor) {
        this.callbackExecutor = callbackExecutor;
    }

    @Override
    public ExecutorService getCallbackExecutor() {
        return null == callbackExecutor ? publicExecutor : callbackExecutor;
    }

    @Override
    public boolean isChannelWritable(String addr) {
        final ChannelWrapper cw = this.channelTables.get(addr);
        if (cw != null && cw.isOK()) {
            return cw.isWritable();
        }
        return false;
    }

    @Override
    public void start() {
        this.defaultEventExecutorGroup = new DefaultEventExecutorGroup(config.getClientWorkerThreads(),
                new ThreadFactory() {
                    private AtomicInteger threadCounter = new AtomicInteger();

                    @Override
                    public Thread newThread(Runnable r) {
                        return new Thread(r, "NettyClientWorkerThread_" + threadCounter.getAndIncrement());
                    }
                });

        Bootstrap handler = this.bootstrap.group(this.eventLoopGroupWorker).channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, false)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, config.getConnectTimeoutMillis())
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        final ChannelPipeline pipeline = ch.pipeline();

                        pipeline.addLast(defaultEventExecutorGroup,
                                new NettyEncoder(),
                                new NettyDecoder(),
                                new IdleStateHandler(0, 0, config.getClientChannelMaxIdleTimeSeconds()),
                                new NettyConnectManageHandler(channelEventListener),
                                nettyClientHandler);

                    }
                });
        if (config.getClientSocketSndBufSize() > 0) {
            logger.info("client set SO_SNDBUF to {}", config.getClientSocketSndBufSize());
            handler.option(ChannelOption.SO_SNDBUF, config.getClientSocketSndBufSize());
        }

        if (config.getClientSocketRcvBufSize() > 0) {
            logger.info("client set SO_RCVBUF to {}", config.getClientSocketRcvBufSize());
            handler.option(ChannelOption.SO_RCVBUF, config.getClientSocketRcvBufSize());
        }

        if (config.getWriteBufferLowWaterMark() > 0 && config.getWriteBufferHighWaterMark() > 0) {
            logger.info("client set netty WRITE_BUFFER_WATER_MARK to {},{}",
                    config.getWriteBufferLowWaterMark(), config.getWriteBufferHighWaterMark());
            handler.option(ChannelOption.WRITE_BUFFER_WATER_MARK,
                    new WriteBufferWaterMark(config.getWriteBufferLowWaterMark(), config.getWriteBufferHighWaterMark()));
        }

        this.timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                AkkaNettySocketClient.this.scanResponseTable();
            }
        }, 1000 * 3, 1000);


        if (this.channelEventListener != null) {
            this.nettyEventExecutor.start();
        }
    }

    @Override
    public void shutdown() {
        try {
            this.timer.cancel();

            for (ChannelWrapper cw : this.channelTables.values()) {
                this.closeChannel(cw.getChannel());
            }

            this.channelTables.clear();

            this.eventLoopGroupWorker.shutdownGracefully();

            if (this.nettyEventExecutor != null) {
                this.nettyEventExecutor.shutdown();
            }

            if (this.defaultEventExecutorGroup != null) {
                this.defaultEventExecutorGroup.shutdownGracefully();
            }
        } catch (Exception e) {
            logger.error("NettyRemotingClient shutdown exception, ", e);
        }

        if (this.publicExecutor != null) {
            try {
                this.publicExecutor.shutdown();
            } catch (Exception e) {
                logger.error("NettyRemotingServer shutdown exception, ", e);
            }
        }
    }


    public void closeChannel(final String addr, final Channel channel) {
        if (null == channel) {
            return;
        }


        final String addrRemote = null == addr ? NettyNetworkHelper.parseChannelRemoteAddr(channel) : addr;

        try {
            if (this.lockChannelTables.tryLock(LOCK_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)) {
                try {
                    boolean removeItemFromTable = true;
                    final ChannelWrapper channelWrapper = this.channelTables.get(addrRemote);

                    logger.info("closeChannel: begin close the channel[{}] Found: {}", addrRemote, channelWrapper != null);

                    if (null == channelWrapper) {
                        logger.info("closeChannel: the channel[{}] has been removed from the channel table before", addrRemote);
                        removeItemFromTable = false;
                    } else if (channelWrapper.getChannel() != channel) {
                        logger.info("closeChannel: the channel[{}] has been closed before, and has been created again, nothing to do.",
                                addrRemote);
                        removeItemFromTable = false;
                    }


                    if (removeItemFromTable) {
                        this.channelTables.remove(addrRemote);
                        logger.info("closeChannel: the channel[{}] was removed from channel table", addrRemote);
                    }

                    RemotingUtil.closeChannel(channel);
                } catch (Exception e) {
                    logger.error("closeChannel: close the channel exception", e);
                } finally {
                    this.lockChannelTables.unlock();
                }
            } else {
                logger.warn("closeChannel: try to lock channel table, but timeout, {}ms", LOCK_TIMEOUT_MILLIS);
            }
        } catch (InterruptedException e) {
            logger.error("closeChannel exception", e);
        }
    }

    public void closeChannel(final Channel channel) {
        if (null == channel) {
            return;
        }

        try {
            if (this.lockChannelTables.tryLock(LOCK_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)) {
                try {
                    boolean removeItemFromTable = true;

                    ChannelWrapper prevCW = null;
                    String addrRemote = null;
                    for (Map.Entry<String, ChannelWrapper> entry : channelTables.entrySet()) {
                        String key = entry.getKey();
                        ChannelWrapper prev = entry.getValue();
                        if (prev.getChannel() != null) {
                            if (prev.getChannel() == channel) {
                                prevCW = prev;
                                addrRemote = key;
                                break;
                            }
                        }
                    }

                    if (null == prevCW) {
                        logger.info("eventCloseChannel: the channel[{}] has been removed from the channel table before", addrRemote);
                        removeItemFromTable = false;
                    }

                    if (removeItemFromTable) {
                        this.channelTables.remove(addrRemote);
                        logger.info("closeChannel: the channel[{}] was removed from channel table", addrRemote);
                        RemotingUtil.closeChannel(channel);
                    }
                } catch (Exception e) {
                    logger.error("closeChannel: close the channel exception", e);
                } finally {
                    this.lockChannelTables.unlock();
                }
            } else {
                logger.warn("closeChannel: try to lock channel table, but timeout, {}ms", LOCK_TIMEOUT_MILLIS);
            }
        } catch (InterruptedException e) {
            logger.error("closeChannel exception", e);
        }
    }


}
