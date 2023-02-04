package com.akka.remoting.netty;/* 
    create qiangzhiwei time 2023/2/4
 */

import com.akka.remoting.Service;
import com.akka.remoting.exception.RemotingConnectException;
import com.akka.remoting.exception.RemotingSendRequestException;
import com.akka.remoting.exception.RemotingTimeoutException;
import com.akka.remoting.exception.RemotingTooMuchRequestException;
import com.akka.remoting.protocol.Command;

import java.util.List;
import java.util.concurrent.ExecutorService;

public interface RemotingClient extends Service {
    void updateNameServerAddressList(final List<String> addrs);

    List<String> getNameServerAddressList();

    Command invokeSync(final String addr, final Command request,
                       final long timeoutMillis) throws InterruptedException, RemotingConnectException,
            RemotingSendRequestException, RemotingTimeoutException;

    void invokeAsync(final String addr, final Command request, final long timeoutMillis,
                     final InvokeCallback invokeCallback) throws InterruptedException, RemotingConnectException,
            RemotingTooMuchRequestException, RemotingTimeoutException, RemotingSendRequestException;

    void invokeOneway(final String addr, final Command request, final long timeoutMillis)
            throws InterruptedException, RemotingConnectException, RemotingTooMuchRequestException,
            RemotingTimeoutException, RemotingSendRequestException;

    void registerProcessor(final int requestCode, final NettyRequestProcessor processor,
                           final ExecutorService executor);

    void setCallbackExecutor(final ExecutorService callbackExecutor);

    ExecutorService getCallbackExecutor();

    boolean isChannelWritable(final String addr);
}
