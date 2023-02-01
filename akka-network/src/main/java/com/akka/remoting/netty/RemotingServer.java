package com.akka.remoting.netty;/* 
    create qiangzhiwei time 2023/2/1
 */

import com.akka.remoting.Service;
import com.akka.remoting.protocol.Command;
import io.netty.channel.Channel;

public interface RemotingServer extends Service {

    Command invokeSync(final Channel channel, final Command command, final long timeMillis);

    void invokeAsync(final Channel channel, final Command command, final long timeoutMillis, InvokeCallback invokeCallback);

    void invokeOneway(final Channel channel, final Command command, final long timeoutMillis);
}
