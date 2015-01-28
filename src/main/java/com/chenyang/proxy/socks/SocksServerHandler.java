
package com.chenyang.proxy.socks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.socks.SocksAuthResponse;
import io.netty.handler.codec.socks.SocksAuthScheme;
import io.netty.handler.codec.socks.SocksAuthStatus;
import io.netty.handler.codec.socks.SocksCmdRequest;
import io.netty.handler.codec.socks.SocksCmdRequestDecoder;
import io.netty.handler.codec.socks.SocksCmdType;
import io.netty.handler.codec.socks.SocksInitResponse;
import io.netty.handler.codec.socks.SocksRequest;


@ChannelHandler.Sharable
public final class SocksServerHandler extends SimpleChannelInboundHandler<SocksRequest> {

	private static Logger logger = LoggerFactory.getLogger(SocksServerHandler.class);

	@Override
    public void channelRead0(ChannelHandlerContext ctx, SocksRequest socksRequest) throws Exception {
        switch (socksRequest.requestType()) {
		case INIT: {
			// auth support example
			// ctx.pipeline().addFirst(new SocksAuthRequestDecoder());
			// ctx.write(new SocksInitResponse(SocksAuthScheme.AUTH_PASSWORD));
			ctx.pipeline().addFirst(new SocksCmdRequestDecoder());
			ctx.write(new SocksInitResponse(SocksAuthScheme.NO_AUTH));
			break;
		}
		case AUTH:
			ctx.pipeline().addFirst(new SocksCmdRequestDecoder());
			ctx.write(new SocksAuthResponse(SocksAuthStatus.SUCCESS));
			break;
		case CMD:
			SocksCmdRequest req = (SocksCmdRequest) socksRequest;
			if (req.cmdType() == SocksCmdType.CONNECT) {
				ctx.pipeline().addLast(new SocksServerConnectHandler());
				ctx.pipeline().remove(this);
				ctx.fireChannelRead(socksRequest);
			} else {
				ctx.close();
			}
			break;
		case UNKNOWN:
                ctx.close();
			break;
        }
    }

    @Override
	public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable throwable) {
		logger.error(" SocksServerHandler socks hander error ", throwable);
        SocksServerUtils.closeOnFlush(ctx.channel());
    }
}
