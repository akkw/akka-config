package com.akka.remoting.netty;/*
    create qiangzhiwei time 2023/2/1
 */

import com.akka.remoting.common.NettyNetworkHelper;
import com.akka.remoting.common.RemotingUtil;
import com.akka.remoting.exception.RemotingEncodeException;
import com.akka.remoting.protocol.Command;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@ChannelHandler.Sharable
public class NettyEncoder extends MessageToByteEncoder<Command> {

    private static final Logger logger = LoggerFactory.getLogger(NettyEncoder.class);

    @Override
    protected void encode(ChannelHandlerContext ctx, Command msg, ByteBuf out) throws Exception {
        try {
            msg.fastEncodeHeader(out);
            final byte[] body = msg.getBody();
            if (body != null) {
                out.writeBytes(body);
            }
        } catch (Exception e) {
            logger.error("encode exception, " + NettyNetworkHelper.parseChannelRemoteAddr(ctx.channel()), e);
            if (msg != null) {
                logger.error(msg.toString());
            }
            RemotingUtil.closeChannel(ctx.channel());
        }
    }
}
