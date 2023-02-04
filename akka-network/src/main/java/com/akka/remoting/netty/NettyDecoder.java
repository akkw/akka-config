package com.akka.remoting.netty;

import com.akka.remoting.common.NettyNetworkHelper;
import com.akka.remoting.common.RemotingUtil;
import com.akka.remoting.protocol.Command;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.util.internal.logging.InternalLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NettyDecoder extends LengthFieldBasedFrameDecoder {
    private static final Logger logger = LoggerFactory.getLogger(NettyDecoder.class);


    public NettyDecoder() {
        super(16777216, 0, 4, 0, 4);
    }


    @Override
    public Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        ByteBuf frame = null;

        try {
            frame = (ByteBuf) super.decode(ctx, in);
            if (null == frame) {
                System.out.println("NettyDecoder null");
                return null;
            }
            return Command.decode(frame);
        } catch (Exception e) {
            logger.error("decode exception, " + NettyNetworkHelper.parseChannelRemoteAddr(ctx.channel()), e);
            RemotingUtil.closeChannel(ctx.channel());
        }
        finally {
            if (null != frame) {
                frame.release();
            }
        }
        return null;
    }
}