package com.akka.remoting.netty;/* 
    create qiangzhiwei time 2023/2/1
 */

import com.akka.remoting.common.NettyNetworkHelper;
import com.akka.remoting.protocol.Command;
import com.akka.remoting.protocol.RemotingSysResponseCode;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;

public class NettyServerHandler extends SimpleChannelInboundHandler<Command> {

    private Logger logger = LoggerFactory.getLogger(NettyServerHandler.class);

    private final HashMap<Integer/* request code */, Pair<NettyRequestProcessor, ExecutorService>> processorTable;

    private final Pair<NettyRequestProcessor, ExecutorService> defaultRequestProcessor;

    protected final ConcurrentMap<Integer /* opaque */, ResponseFuture> responseTable;

    private ExecutorService publicExecutor;

    public NettyServerHandler(HashMap<Integer, Pair<NettyRequestProcessor, ExecutorService>> processorTable,
                              Pair<NettyRequestProcessor, ExecutorService> defaultRequestProcessor,
                              ConcurrentMap<Integer, ResponseFuture> responseTable) {
        this.processorTable = processorTable;
        this.defaultRequestProcessor = defaultRequestProcessor;
        this.responseTable = responseTable;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Command msg) throws Exception {
        processMessageReceived(ctx, msg);
    }


    private void processMessageReceived(ChannelHandlerContext ctx, Command msg){
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
                        final String remoteAddr = NettyNetworkHelper.parseChannelRemoteAddr(ctx.channel());

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
            String error = "request type " + command + "not supported";
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
        ExecutorService executor = publicExecutor;
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

}
