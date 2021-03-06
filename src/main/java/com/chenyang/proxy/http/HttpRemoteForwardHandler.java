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
import io.netty.channel.*;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpRemoteForwardHandler extends ChannelInboundHandlerAdapter {

	private static Logger logger = LoggerFactory.getLogger(HttpRemoteForwardHandler.class);

	public static final String HANDLER_NAME = "apnproxy.remote.forward";

	private Channel uaChannel;

	private RemoteChannelInactiveCallback remoteChannelInactiveCallback;

	private int remainMsgCount = 0;

	public HttpRemoteForwardHandler(Channel uaChannel, RemoteChannelInactiveCallback remoteChannelInactiveCallback) {
		this.uaChannel = uaChannel;
		this.remoteChannelInactiveCallback = remoteChannelInactiveCallback;
	}

	@Override
	public void channelActive(ChannelHandlerContext remoteChannelCtx) throws Exception {
		remoteChannelCtx.read();
	}

	public void channelRead(final ChannelHandlerContext remoteChannelCtx, final Object msg) throws Exception {

		remainMsgCount++;

		if (remainMsgCount <= 5) {
			remoteChannelCtx.read();
		}

		HttpObject ho = (HttpObject) msg;

		if (ho instanceof HttpResponse) {
			HttpResponse httpResponse = (HttpResponse) ho;

			httpResponse.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
			httpResponse.headers().set("Proxy-Connection", HttpHeaders.Values.KEEP_ALIVE);
		}

		if (uaChannel.isActive()) {
			uaChannel.writeAndFlush(ho).addListener(new ChannelFutureListener() {
				@Override
				public void operationComplete(ChannelFuture future) throws Exception {
					if (future.isSuccess()) {
						remainMsgCount--;
						remoteChannelCtx.read();
					} else {
						remoteChannelCtx.close();
					}
				}
			});
		} else {
			remoteChannelCtx.close();
		}
	}

	@Override
	public void channelInactive(final ChannelHandlerContext remoteChannelCtx) throws Exception {

		final String remoteAddr = uaChannel.attr(HttpConnectionAttribute.ATTRIBUTE_KEY).get().getRemote().getRemoteAddr();

		remoteChannelInactiveCallback.remoteChannelInactive(uaChannel, remoteAddr);

		remoteChannelCtx.fireChannelInactive();

	}

	@Override
	public void exceptionCaught(ChannelHandlerContext remoteChannelCtx, Throwable cause) throws Exception {
		logger.error(cause.getMessage() + uaChannel.attr(HttpConnectionAttribute.ATTRIBUTE_KEY), cause);
		remoteChannelCtx.close();
	}

	public interface RemoteChannelInactiveCallback {
		public void remoteChannelInactive(Channel uaChannel, String remoeAddr) throws Exception;
	}

}
