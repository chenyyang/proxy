
package com.chenyang.proxy.socks;

import com.chenyang.proxy.util.HostAuthenticationUtil;
import com.chenyang.proxy.util.NetworkUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.socks.SocksCmdRequest;
import io.netty.handler.codec.socks.SocksCmdResponse;
import io.netty.handler.codec.socks.SocksCmdStatus;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.Promise;

import java.net.InetSocketAddress;

@ChannelHandler.Sharable
public final class SocksServerConnectHandler extends SimpleChannelInboundHandler<SocksCmdRequest> {

	private final Bootstrap b = new Bootstrap();

	private static Logger logger = LoggerFactory.getLogger(SocksServerConnectHandler.class);

	@Override
	public void channelRead0(final ChannelHandlerContext ctx, final SocksCmdRequest request) throws Exception {
        InetSocketAddress remoteAddress = new InetSocketAddress(request.host(), request.port());
        if (!HostAuthenticationUtil.isValidAddress(remoteAddress)) {
			ctx.channel().writeAndFlush(new SocksCmdResponse(SocksCmdStatus.FAILURE, request.addressType()));
			SocksServerUtils.closeOnFlush(ctx.channel());
			return;
		}
		Promise<Channel> promise = ctx.executor().newPromise();
		promise.addListener(new GenericFutureListener<Future<Channel>>() {
			@Override
			public void operationComplete(final Future<Channel> future) throws Exception {
				final Channel outboundChannel = future.getNow();
				if (future.isSuccess()) {
					ctx.channel().writeAndFlush(new SocksCmdResponse(SocksCmdStatus.SUCCESS, request.addressType()))
							.addListener(new ChannelFutureListener() {
								@Override
								public void operationComplete(ChannelFuture channelFuture) {
									ctx.pipeline().remove(SocksServerConnectHandler.this);
									outboundChannel.pipeline().addLast(new RelayHandler(ctx.channel()));
									ctx.pipeline().addLast(new SendMessageDecoder());
									ctx.pipeline().addLast(new RelayHandler(outboundChannel));
								}
							});
				} else {
					ctx.channel().writeAndFlush(new SocksCmdResponse(SocksCmdStatus.FAILURE, request.addressType()));
					SocksServerUtils.closeOnFlush(ctx.channel());
				}
			}
		});

		final Channel inboundChannel = ctx.channel();
		b.group(inboundChannel.eventLoop()).channel(NioSocketChannel.class).option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
				.option(ChannelOption.SO_KEEPALIVE, true).handler(new DirectClientHandler(promise));

        b.connect(remoteAddress, new InetSocketAddress(NetworkUtils.getCyclicLocalIp().getHostAddress(), 0))
				.addListener(new ChannelFutureListener() {
					@Override
					public void operationComplete(ChannelFuture future) throws Exception {
						if (future.isSuccess()) {
							// Connection established use handler provided
							// results
						} else {
							// Close the connection if the connection attempt
							// has failed.
							logger.error("SocksServerConnectHandler connect remote server error and close");
							ctx.channel().writeAndFlush(new SocksCmdResponse(SocksCmdStatus.FAILURE, request.addressType()));
							SocksServerUtils.closeOnFlush(ctx.channel());
						}
					}
				});
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		logger.error("SocksServerConnectHandler connect error ", cause);
		SocksServerUtils.closeOnFlush(ctx.channel());
	}
}
