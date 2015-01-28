package com.chenyang.proxy.http;/*
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



import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

public class HttpTunnelChannelInitializer extends ChannelInitializer<SocketChannel> {

	private final Channel uaChannel;

	public HttpTunnelChannelInitializer(Channel uaChannel) {
		this.uaChannel = uaChannel;
	}

	/**
	 * @see io.netty.channel.ChannelInitializer#initChannel(io.netty.channel.Channel)
	 */
	@Override
	protected void initChannel(SocketChannel channel) throws Exception {
        com.chenyang.proxy.common.HttpRemote apnProxyRemote = uaChannel
                .attr(com.chenyang.proxy.common.HttpConnectionAttribute.ATTRIBUTE_KEY).get().getRemote();

        channel.attr(com.chenyang.proxy.common.HttpConnectionAttribute.ATTRIBUTE_KEY).set(uaChannel.attr(
            com.chenyang.proxy.common.HttpConnectionAttribute.ATTRIBUTE_KEY).get());

		ChannelPipeline pipeline = channel.pipeline();

		// pipeline.addLast("idlestate", new IdleStateHandler(0, 0, 3,
		// TimeUnit.MINUTES));
		// pipeline.addLast("idlehandler", new ProxyIdleHandler());

        pipeline.addLast(new com.chenyang.proxy.http.HttpRelayHandler(apnProxyRemote.getRemoteAddr() + " --> UA", uaChannel));

	}
}
