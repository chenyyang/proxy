/*
 * Copyright (c) 2014 The APN-PROXY Project
 *
 * The APN-PROXY Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.chenyang.proxy.http;

import com.chenyang.proxy.common.HttpConnectionAttribute;
import com.chenyang.proxy.common.HttpRemote;
import com.chenyang.proxy.util.NetworkUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

public class HttpUserAgentTunnelHandler extends ChannelInboundHandlerAdapter {

	public static final String HANDLER_NAME = "apnproxy.useragent.tunnel";
    private static Logger logger = LoggerFactory.getLogger(HttpUserAgentTunnelHandler.class);

	@Override
	public void channelRead(final ChannelHandlerContext uaChannelCtx, Object msg) throws Exception {

		if (msg instanceof HttpRequest) {
			// Channel uaChannel = uaChannelCtx.channel();

			// connect remote
			Bootstrap bootstrap = new Bootstrap();
			bootstrap.group(uaChannelCtx.channel().eventLoop()).channel(NioSocketChannel.class).option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
					.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT).option(ChannelOption.AUTO_READ, false)
					.handler(new HttpTunnelChannelInitializer(uaChannelCtx.channel()));

			final HttpRemote apnProxyRemote = uaChannelCtx.channel().attr(HttpConnectionAttribute.ATTRIBUTE_KEY).get().getRemote();

            bootstrap.connect(apnProxyRemote.getInetSocketAddress(),
                new InetSocketAddress(NetworkUtils.getCyclicLocalIp().getHostAddress(), 0)).addListener(new ChannelFutureListener() {
				@Override
				public void operationComplete(final ChannelFuture future1) throws Exception {
					if (future1.isSuccess()) {
						HttpResponse proxyConnectSuccessResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, new HttpResponseStatus(200,
								"Connection established"));
						uaChannelCtx.writeAndFlush(proxyConnectSuccessResponse).addListener(new ChannelFutureListener() {
							@Override
							public void operationComplete(ChannelFuture future2) throws Exception {
								// remove handlers
								uaChannelCtx.pipeline().remove("codec");
								uaChannelCtx.pipeline().remove(HttpPreHandler.HANDLER_NAME);
								uaChannelCtx.pipeline().remove(HttpUserAgentTunnelHandler.HANDLER_NAME);

								uaChannelCtx.pipeline().addLast(new HttpRelayHandler("UA --> " + apnProxyRemote.getRemoteAddr(), future1.channel()));
							}

						});

					} else {
						if (uaChannelCtx.channel().isActive()) {
							uaChannelCtx.channel().writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
						}
					}
				}
			});

		}
		ReferenceCountUtil.release(msg);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error(" error ", cause);
		ctx.close();
	}
}
