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

import com.chenyang.proxy.util.HostNamePortUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.util.ReferenceCountUtil;

@ChannelHandler.Sharable
public class HttpPreHandler extends ChannelInboundHandlerAdapter {

	public static final String HANDLER_NAME = "apnproxy.pre";
	private static Logger logger = LoggerFactory.getLogger(HttpPreHandler.class);

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (preCheck(ctx, msg)) {
			ctx.fireChannelRead(msg);
		} else {
			ReferenceCountUtil.release(msg);
		}
	}

	private boolean preCheck(ChannelHandlerContext ctx, Object msg) {
		if (msg instanceof HttpRequest) {
			HttpRequest httpRequest = (HttpRequest) msg;
			String originalHost = HostNamePortUtil.getHostName(httpRequest);
            // if (!HostAuthenticationUtil.isValidDomain(originalHost)) {
            // HttpErrorUtil.writeAndFlush(ctx.channel(), HttpResponseStatus.FORBIDDEN);
            // return false;
            // }
		}

		return true;
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("error", cause);
		ctx.close();
	}

}
