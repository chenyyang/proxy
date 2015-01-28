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
import com.chenyang.proxy.util.HostAuthenticationUtil;
import com.chenyang.proxy.util.HostNamePortUtil;
import com.chenyang.proxy.util.HttpErrorUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ChannelHandler.Sharable
public class HttpSchemaHandler extends ChannelInboundHandlerAdapter {

	private static Logger logger = LoggerFactory.getLogger(HttpSchemaHandler.class);
	public static final String HANDLER_NAME = "apnproxy.schema";

	@Override
	public void channelRead(ChannelHandlerContext uaChannelCtx, final Object msg) throws Exception {

		if (msg instanceof HttpRequest) {
			HttpRequest httpRequest = (HttpRequest) msg;

			String originalHost = HostNamePortUtil.getHostName(httpRequest);
			int originalPort = HostNamePortUtil.getPort(httpRequest);
            HttpRemote apnProxyRemote = new HttpRemote(originalHost, originalPort);

            if (!HostAuthenticationUtil.isValidAddress(apnProxyRemote.getInetSocketAddress())) {
                HttpErrorUtil.writeAndFlush(uaChannelCtx.channel(), HttpResponseStatus.FORBIDDEN);
                return;
            }

			Channel uaChannel = uaChannelCtx.channel();

			HttpConnectionAttribute apnProxyConnectionAttribute = HttpConnectionAttribute.build(uaChannel.remoteAddress().toString(), httpRequest
					.getMethod().name(), httpRequest.getUri(), httpRequest.getProtocolVersion().text(),
					httpRequest.headers().get(HttpHeaders.Names.USER_AGENT), apnProxyRemote);

			uaChannelCtx.attr(HttpConnectionAttribute.ATTRIBUTE_KEY).set(apnProxyConnectionAttribute);
			uaChannel.attr(HttpConnectionAttribute.ATTRIBUTE_KEY).set(apnProxyConnectionAttribute);

			if (httpRequest.getMethod().equals(HttpMethod.CONNECT)) {
				if (uaChannelCtx.pipeline().get(HttpUserAgentForwardHandler.HANDLER_NAME) != null) {
					uaChannelCtx.pipeline().remove(HttpUserAgentForwardHandler.HANDLER_NAME);
				}
				if (uaChannelCtx.pipeline().get(HttpUserAgentTunnelHandler.HANDLER_NAME) == null) {
					uaChannelCtx.pipeline().addLast(HttpUserAgentTunnelHandler.HANDLER_NAME, new HttpUserAgentTunnelHandler());
				}
			} else {
				if (uaChannelCtx.pipeline().get(HttpUserAgentForwardHandler.HANDLER_NAME) == null) {
					uaChannelCtx.pipeline().addLast(HttpUserAgentForwardHandler.HANDLER_NAME, new HttpUserAgentForwardHandler());
				}
			}
		}

		uaChannelCtx.fireChannelRead(msg);
	}
}
