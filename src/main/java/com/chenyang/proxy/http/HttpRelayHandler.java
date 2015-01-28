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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOption;
import io.netty.util.ReferenceCountUtil;

public class HttpRelayHandler extends ChannelInboundHandlerAdapter {

	private static Logger logger = LoggerFactory.getLogger(HttpRelayHandler.class);
	public static final String HANDLER_NAME = "apnproxy.relay";

	private final Channel relayChannel;
	private final String tag;

	public HttpRelayHandler(String tag, Channel relayChannel) {
		this.tag = tag;
		this.relayChannel = relayChannel;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {

		if (!ctx.channel().config().getOption(ChannelOption.AUTO_READ)) {
			ctx.read();
		}
	}

	@Override
	public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {

		if (relayChannel.isActive()) {
			relayChannel.writeAndFlush(msg).addListener(new ChannelFutureListener() {
				@Override
				public void operationComplete(ChannelFuture future) throws Exception {
					if (!ctx.channel().config().getOption(ChannelOption.AUTO_READ)) {
						ctx.read();
					}
				}
			});
		} else {
			ReferenceCountUtil.release(msg);
		}

	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		if (relayChannel != null && relayChannel.isActive()) {
			relayChannel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
		}
		ctx.fireChannelInactive();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("error tag : {}", tag, cause);
		ctx.close();
	}

}