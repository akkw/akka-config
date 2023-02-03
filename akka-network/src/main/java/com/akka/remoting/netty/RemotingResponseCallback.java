package com.akka.remoting.netty;

import com.akka.remoting.protocol.Command;

public interface RemotingResponseCallback {
    void callback(Command response);
}