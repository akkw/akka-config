package com.akka.remoting.netty;/* 
    create qiangzhiwei time 2023/2/3
 */

import com.akka.remoting.common.NettyNetworkHelper;
import com.akka.remoting.common.SemaphoreReleaseOnlyOnce;
import com.akka.remoting.exception.RemotingSendRequestException;
import com.akka.remoting.exception.RemotingTimeoutException;
import com.akka.remoting.exception.RemotingTooMuchRequestException;
import com.akka.remoting.protocol.Command;
import com.akka.remoting.protocol.RemotingSysResponseCode;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

public abstract class AkkaNettySocketAbstract {

    private static final Logger logger = LoggerFactory.getLogger(AkkaNettySocketAbstract.class);
    protected final ConcurrentMap<Integer /* opaque */, ResponseFuture> responseTable =
            new ConcurrentHashMap<Integer, ResponseFuture>(256);

    protected final HashMap<Integer/* request code */, Pair<NettyRequestProcessor, ExecutorService>> processorTable =
            new HashMap<Integer, Pair<NettyRequestProcessor, ExecutorService>>(64);


    protected final NettyEventExecutor nettyEventExecutor;


    protected Pair<NettyRequestProcessor, ExecutorService> defaultRequestProcessor;

    protected final Semaphore semaphoreAsync;

    protected final Semaphore semaphoreOneway;

    public AkkaNettySocketAbstract(final int permitsOneway, final int permitsAsync,ChannelEventListener listener) {
        this.semaphoreOneway = new Semaphore(permitsOneway, true);
        this.semaphoreAsync = new Semaphore(permitsAsync, true);
        this.nettyEventExecutor = new NettyEventExecutor(listener);
    }

    protected void processMessageReceived(ChannelHandlerContext ctx, Command msg) {
        if (msg != null) {
            switch (msg.getType()) {
                case REQUEST_COMMAND:
                    processRequestCommand(ctx, msg);
                    break;
                case RESPONSE_COMMAND:
                    processResponseCommand(ctx, msg);
                default:
                    break;
            }
        }
    }


    private void processRequestCommand(ChannelHandlerContext ctx, Command command) {
        final Pair<NettyRequestProcessor, ExecutorService> matched = this.processorTable.get(command.getCode());
        final Pair<NettyRequestProcessor, ExecutorService> pair = null == matched ? this.defaultRequestProcessor : matched;
        final int opaque = command.getOpaque();
        if (pair != null) {
            Runnable run = new Runnable() {
                @Override
                public void run() {
                    try {
                        final RemotingResponseCallback callback = new RemotingResponseCallback() {
                            @Override
                            public void callback(Command response) {
                                if (!command.isOnewayRPC()) {
                                    if (response != null) {
                                        response.setOpaque(opaque);
                                        response.markResponseType();
                                        response.setSerializeTypeCurrentRPC(command.getSerializeTypeCurrentRPC());
                                        try {
                                            ctx.writeAndFlush(response);
                                        } catch (Throwable e) {
                                            logger.error("process request over, but response failed", e);
                                            logger.error(command.toString());
                                            logger.error(response.toString());
                                        }
                                    }
                                }
                            }
                        };
                        if (pair.getKey() instanceof AsyncNettyRequestProcessor) {
                            final AsyncNettyRequestProcessor processor = (AsyncNettyRequestProcessor) pair.getKey();
                            processor.asyncProcessRequest(ctx, command, callback);
                        } else {
                            NettyRequestProcessor processor = pair.getKey();
                            final Command response = processor.processRequest(ctx, command);
                            callback.callback(response);
                        }
                    } catch (Throwable e) {
                        logger.error("process request exception", e);
                        logger.error(command.toString());

                        if (!command.isOnewayRPC()) {
                            Command response = Command.createResponseCommand(RemotingSysResponseCode.SYSTEM_BUSY,
                                    "[REJECTREQUEST]system busy, start flow control for a while");
                            response.setOpaque(opaque);
                            ctx.writeAndFlush(response);
                        }

                    }
                }
            };

            if (pair.getKey().rejectRequest()) {
                final Command response = Command.createResponseCommand(RemotingSysResponseCode.SYSTEM_BUSY,
                        "[REJECTREQUEST]system busy, start flow control for a while");
                response.setOpaque(opaque);
                ctx.writeAndFlush(response);
                return;
            }

            try {
                final RequestTask requestTask = new RequestTask(run, ctx.channel(), command);
                pair.getValue().submit(requestTask);
            } catch (RejectedExecutionException e) {
                if ((System.currentTimeMillis() % 10000) == 0) {
                    logger.warn(NettyNetworkHelper.parseChannelRemoteAddr(ctx.channel())
                            + ", too many requests and system thread pool busy, RejectedExecutionException "
                            + pair.getValue().toString()
                            + " request code: " + command.getCode());
                }

                if (!command.isOnewayRPC()) {
                    final Command response = Command.createResponseCommand(RemotingSysResponseCode.SYSTEM_BUSY,
                            "[OVERLOAD]system busy, start flow control for a while");
                    response.setOpaque(opaque);
                    ctx.writeAndFlush(response);
                }
            }

        } else {
            String error = "request type [" + command.getCode() + "] not supported";
            final Command response = Command.createResponseCommand(RemotingSysResponseCode.REQUEST_CODE_NOT_SUPPORTED, error);
            response.setOpaque(opaque);
            ctx.writeAndFlush(response);
            logger.error(NettyNetworkHelper.parseChannelRemoteAddr(ctx.channel()) + error);
        }
    }



    private void processResponseCommand(ChannelHandlerContext ctx, Command command) {
        final int opaque = command.getOpaque();
        final ResponseFuture responseFuture = responseTable.get(opaque);
        if (responseFuture != null) {
            responseFuture.setResponseCommand(command);

            responseTable.remove(opaque);
            if (responseFuture.getInvokeCallback() != null) {
                executeInvokeCallback(responseFuture);
            } else {
                responseFuture.putResponse(command);
                responseFuture.release();
            }
        }
    }

    private void executeInvokeCallback(final ResponseFuture responseFuture) {
        boolean runInThisThread = false;
        ExecutorService executor = getCallbackExecutor();
        if (executor != null) {
            try {
                executor.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            responseFuture.executeInvokeCallback();
                        } catch (Throwable e) {
                            logger.warn("execute callback in executor exception, and callback throw", e);
                        } finally {
                            responseFuture.release();
                        }
                    }
                });
            } catch (Exception e) {
                runInThisThread = true;
                logger.warn("execute callback in executor exception, maybe executor busy", e);
            }
        } else {
            runInThisThread = true;
        }

        if (runInThisThread) {
            try {
                responseFuture.executeInvokeCallback();
            } catch (Throwable e) {
                logger.warn("executeInvokeCallback Exception", e);
            } finally {
                responseFuture.release();
            }
        }
    }

    public void scanResponseTable() {
        final List<ResponseFuture> rfList = new LinkedList<ResponseFuture>();
        Iterator<Map.Entry<Integer, ResponseFuture>> it = this.responseTable.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, ResponseFuture> next = it.next();
            ResponseFuture rep = next.getValue();

            if ((rep.getBeginTimestamp() + rep.getTimeoutMillis() + 1000) <= System.currentTimeMillis()) {
                rep.release();
                it.remove();
                rfList.add(rep);
                logger.warn("remove timeout request, " + rep);
            }
        }

        for (ResponseFuture rf : rfList) {
            try {
                executeInvokeCallback(rf);
            } catch (Throwable e) {
                logger.warn("scanResponseTable, operationComplete Exception", e);
            }
        }
    }

    public abstract ExecutorService getCallbackExecutor();

    public Command invokeSyncImpl(final Channel channel, final Command request, final long timeoutMillis) throws InterruptedException, RemotingTimeoutException, RemotingSendRequestException {
        final int opaque = request.getOpaque();
        try {
            final ResponseFuture responseFuture = new ResponseFuture(channel, opaque, timeoutMillis, null, null);
            this.responseTable.put(opaque, responseFuture);
            final SocketAddress addr = channel.remoteAddress();
            channel.writeAndFlush(request).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (future.isSuccess()) {
                        responseFuture.setSendRequestOK(true);
                        return;
                    } else {
                        responseFuture.setSendRequestOK(false);
                    }

                    responseTable.remove(opaque);
                    responseFuture.setCause(future.cause());
                    responseFuture.putResponse(null);
                    logger.warn("send a request command to channel <" + addr + "> failed.");
                }
            });

            final Command responseCommand = responseFuture.waitResponse(timeoutMillis);
            if (null == responseCommand) {
                if (responseFuture.isSendRequestOK()) {
                    throw new RemotingTimeoutException(NettyNetworkHelper.parseSocketAddressAddr(addr), timeoutMillis, responseFuture.getCause());
                } else {
                    throw new RemotingSendRequestException(NettyNetworkHelper.parseSocketAddressAddr(addr), responseFuture.getCause());
                }
            }
            return responseCommand;
        } finally {
            responseTable.remove(opaque);
        }
    }


    public void invokeAsyncImpl(final Channel channel, final Command request, final long timeoutMillis, InvokeCallback callback) throws InterruptedException, RemotingTimeoutException, RemotingSendRequestException, RemotingTooMuchRequestException {
        long beginStartTime = System.currentTimeMillis();
        final int opaque = request.getOpaque();
        final boolean acquire = this.semaphoreAsync.tryAcquire(timeoutMillis, TimeUnit.MILLISECONDS);

        if (acquire) {
            final SemaphoreReleaseOnlyOnce once = new SemaphoreReleaseOnlyOnce(semaphoreAsync);
            final long costTime = System.currentTimeMillis() - beginStartTime;
            if (timeoutMillis < costTime) {
                once.release();
                throw new RemotingTimeoutException("invokeAsyncImpl call timeout");
            }

            final ResponseFuture responseFuture = new ResponseFuture(channel, opaque, timeoutMillis, callback, once);
            this.responseTable.put(opaque, responseFuture);


            try {
                channel.writeAndFlush(request).addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        if (future.isSuccess()) {
                            responseFuture.setSendRequestOK(true);
                            return;
                        }
                        requestFail(opaque);
                        logger.warn("send a request command to channel <{}> failed.", NettyNetworkHelper.parseChannelRemoteAddr(channel));
                    }
                });
            } catch (Exception e) {
                responseFuture.release();
                logger.warn("send a request command to channel <" + NettyNetworkHelper.parseChannelRemoteAddr(channel) + "> Exception", e);
                throw new RemotingSendRequestException(NettyNetworkHelper.parseChannelRemoteAddr(channel), e);
            }
        } else {
            if (timeoutMillis <= 0) {
                throw new RemotingTooMuchRequestException("invokeAsyncImpl invoke too fast");
            } else {
                String info =
                        String.format("invokeAsyncImpl tryAcquire semaphore timeout, %dms, waiting thread nums: %d semaphoreAsyncValue: %d",
                                timeoutMillis,
                                this.semaphoreAsync.getQueueLength(),
                                this.semaphoreAsync.availablePermits()
                        );
                logger.warn(info);
                throw new RemotingTimeoutException(info);
            }
        }
    }

    private void requestFail(int opaque) {
        final ResponseFuture responseFuture = responseTable.remove(opaque);
        if (responseFuture != null) {
            responseFuture.setSendRequestOK(false);
            responseFuture.putResponse(null);
            try {
                executeInvokeCallback(responseFuture);
            } catch (Throwable t) {
                logger.warn("execute callback in requestFail, and callback throw", t);
            } finally {
                responseFuture.release();
            }
        }
    }


    public void invokeOnewayImpl(final Channel channel, final Command request, final long timeoutMillis)
            throws InterruptedException, RemotingSendRequestException,
            RemotingTooMuchRequestException, RemotingTimeoutException {
        request.markOnewayRPC();
        final boolean acquire = semaphoreOneway.tryAcquire(timeoutMillis, TimeUnit.MILLISECONDS);

        if (acquire) {
            final SemaphoreReleaseOnlyOnce once = new SemaphoreReleaseOnlyOnce(semaphoreOneway);
            try {
                channel.writeAndFlush(request).addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        once.release();
                        if (future.isSuccess()) {
                            logger.warn("send a request command to channel <" + channel.remoteAddress() + "> failed.");
                        }
                    }
                });
            } catch (Exception e) {
                once.release();
                logger.warn("write send a request command to channel <" + channel.remoteAddress() + "> failed.");
                throw new RemotingSendRequestException(NettyNetworkHelper.parseChannelRemoteAddr(channel), e);
            }
        } else {
            if (timeoutMillis <= 0) {
                throw new RemotingTooMuchRequestException("invokeOnewayImpl invoke too fast");
            } else {
                String info = String.format(
                        "invokeOnewayImpl tryAcquire semaphore timeout, %dms, waiting thread nums: %d semaphoreOnewayValue: %d",
                        timeoutMillis,
                        this.semaphoreOneway.getQueueLength(),
                        this.semaphoreOneway.availablePermits()
                );
                logger.warn(info);
                throw new RemotingTimeoutException(info);
            }
        }
    }
}
