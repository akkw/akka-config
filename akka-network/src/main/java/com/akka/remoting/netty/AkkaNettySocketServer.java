package com.akka.remoting.netty;/* 
    create qiangzhiwei time 2023/2/1
 */

import com.akka.remoting.exception.RemotingSendRequestException;
import com.akka.remoting.exception.RemotingTimeoutException;
import com.akka.remoting.exception.RemotingTooMuchRequestException;
import com.akka.remoting.protocol.Command;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Timer;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class AkkaNettySocketServer extends AkkaNettySocketAbstract implements RemotingServer {

    private static final Logger logger = LoggerFactory.getLogger(AkkaNettySocketServer.class);

    private final ServerBootstrap serverBootstrap;

    private final EventLoopGroup eventLoopGroupSelector;

    private final EventLoopGroup eventLoopGroupBoss;


    private final ExecutorService publicExecutor;

    private final ChannelEventListener channelEventListener;

    private final Timer timer = new Timer("netty-keeping-timer", true);

    private final NettyServerConfig nettyServerConfig;

    private DefaultEventExecutorGroup defaultEventExecutorGroup;

    private NettyServerHandler serverHandler;

    private NettyConnectManageHandler connectionManageHandler;

    private final NettyEncoder encoder = new NettyEncoder();;

    private int port = 0;



    public AkkaNettySocketServer(NettyServerConfig config) {
        this(config, null);
    }

    public AkkaNettySocketServer(NettyServerConfig config, ChannelEventListener listener) {
        super(config.getServerOnewaySemaphoreValue(), config.getServerAsyncSemaphoreValue(), listener);
        this.serverBootstrap = new ServerBootstrap();
        this.channelEventListener = listener;
        this.nettyServerConfig = config;
        this.connectionManageHandler = new NettyConnectManageHandler(listener);
        this.serverHandler = new NettyServerHandler(processorTable, defaultRequestProcessor, responseTable, this::processMessageReceived);


        int serverCallbackThreads = config.getServerCallbackThreads();
        if (serverCallbackThreads <= 0) {
            serverCallbackThreads = 4;
        }


        this.publicExecutor = new ThreadPoolExecutor(serverCallbackThreads, serverCallbackThreads,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(),
                new ThreadFactory() {
                    private final AtomicLong threadCounter = new AtomicLong();

                    @Override
                    public Thread newThread(Runnable r) {
                        return new Thread(r, "NettyServerPublishExecutor_" + threadCounter.getAndIncrement());
                    }
                });

        if (useEpoll()) {
            this.eventLoopGroupBoss = new EpollEventLoopGroup(1, new ThreadFactory() {
                private final AtomicLong threadCounter = new AtomicLong();

                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, "NettyEpollBoss_" + threadCounter.getAndIncrement());
                }
            });

            this.eventLoopGroupSelector = new EpollEventLoopGroup(config.getServerSelectorThreads(), new ThreadFactory() {
                private final AtomicLong threadCounter = new AtomicLong();

                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, "NettyServerEpollSelector_" + threadCounter.getAndIncrement());
                }
            });
        } else {
            this.eventLoopGroupBoss = new NioEventLoopGroup(1, new ThreadFactory() {
                private final AtomicLong threadCounter = new AtomicLong();

                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, "NettyEpollBoss_" + threadCounter.getAndIncrement());
                }
            });

            this.eventLoopGroupSelector = new NioEventLoopGroup(config.getServerSelectorThreads(), new ThreadFactory() {
                private final AtomicLong threadCounter = new AtomicLong();

                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, "NettyEpollBoss_" + threadCounter.getAndIncrement());
                }
            });
        }


    }


    @Override
    public void registerDefaultProcessor(NettyRequestProcessor processor, ExecutorService executor) {
        this.defaultRequestProcessor = new Pair<>(processor, executor);
    }

    @Override
    public void registerProcessor(int requestCode, NettyRequestProcessor processor, ExecutorService processorExecutor) {
        ExecutorService executor = processorExecutor;
        if (executor == null) {
            executor = this.publicExecutor;
        }
        this.processorTable.put(requestCode, new Pair<>(processor, executor));
    }

    @Override
    public Pair<NettyRequestProcessor, ExecutorService> getProcessorPair(int requestCode) {
        return this.processorTable.get(requestCode);
    }

    @Override
    public Command invokeSync(Channel channel, Command command, long timeMillis) throws RemotingSendRequestException, RemotingTimeoutException, InterruptedException {
        return invokeSyncImpl(channel, command, timeMillis);
    }

    @Override
    public void invokeAsync(Channel channel, Command command, long timeoutMillis, InvokeCallback invokeCallback) throws RemotingSendRequestException, RemotingTimeoutException, InterruptedException, RemotingTooMuchRequestException {
        invokeAsyncImpl(channel, command, timeoutMillis, invokeCallback);
    }

    @Override
    public void invokeOneway(Channel channel, Command command, long timeoutMillis) throws RemotingSendRequestException, RemotingTimeoutException, InterruptedException, RemotingTooMuchRequestException {
        invokeOnewayImpl(channel, command, timeoutMillis);
    }

    @Override
    public void start() {
        this.defaultEventExecutorGroup = new DefaultEventExecutorGroup(
                nettyServerConfig.getServerWorkerThreads(),
                new ThreadFactory() {

                    private AtomicInteger threadIndex = new AtomicInteger(0);

                    @Override
                    public Thread newThread(Runnable r) {
                        return new Thread(r, "NettyServerCodecThread_" + this.threadIndex.incrementAndGet());
                    }
                });

        this.serverBootstrap.group(this.eventLoopGroupBoss, this.eventLoopGroupSelector)
                .channel(useEpoll()? EpollServerSocketChannel.class : NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, nettyServerConfig.getServerSocketBacklog())
                .option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.SO_KEEPALIVE, false)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .localAddress(nettyServerConfig.getListenPort())
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(defaultEventExecutorGroup,
                                encoder,
                                new NettyDecoder(),
                                connectionManageHandler,
                                serverHandler);
                    }
                });

        try {
            ChannelFuture sync = this.serverBootstrap.bind().sync();
            InetSocketAddress addr = (InetSocketAddress) sync.channel().localAddress();
            this.port = addr.getPort();
        } catch (InterruptedException e1) {
            throw new RuntimeException("this.serverBootstrap.bind().sync() InterruptedException", e1);
        }

        if (this.channelEventListener != null) {
            this.nettyEventExecutor.start();
        }

    }
    @Override
    public void shutdown() {
        try {
            this.timer.cancel();

            this.eventLoopGroupBoss.shutdownGracefully();

            this.eventLoopGroupSelector.shutdownGracefully();

            this.nettyEventExecutor.shutdown();

            if (this.defaultEventExecutorGroup != null) {
                this.defaultEventExecutorGroup.shutdownGracefully();
            }
        } catch (Exception e) {
            logger.error("NettyRemotingServer shutdown exception, ", e);
        }

        if (this.publicExecutor != null) {
            try {
                this.publicExecutor.shutdown();
            } catch (Exception e) {
                logger.error("NettyRemotingServer shutdown exception, ", e);
            }
        }
    }

    private boolean useEpoll() {
        return Epoll.isAvailable();
    }

    @Override
    public ExecutorService getCallbackExecutor() {
        return publicExecutor;
    }
}
