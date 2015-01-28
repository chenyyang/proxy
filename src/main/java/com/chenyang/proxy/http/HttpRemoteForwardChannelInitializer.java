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
import com.chenyang.proxy.http.HttpRemoteForwardHandler.RemoteChannelInactiveCallback;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;

public class HttpRemoteForwardChannelInitializer extends ChannelInitializer<SocketChannel> {


    private Channel uaChannel;
    private RemoteChannelInactiveCallback remoteChannelInactiveCallback;

    public HttpRemoteForwardChannelInitializer(Channel uaChannel,
                                                   RemoteChannelInactiveCallback remoteChannelInactiveCallback) {
        this.uaChannel = uaChannel;
        this.remoteChannelInactiveCallback = remoteChannelInactiveCallback;
    }

    @Override
    public void initChannel(SocketChannel channel) throws Exception {

        channel.attr(HttpConnectionAttribute.ATTRIBUTE_KEY).set(uaChannel
                .attr(HttpConnectionAttribute.ATTRIBUTE_KEY).get());

        ChannelPipeline pipeline = channel.pipeline();

		// pipeline.addLast("idlestate", new IdleStateHandler(0, 0, 3,
		// TimeUnit.MINUTES));
		// pipeline.addLast("idlehandler", new ProxyIdleHandler());

        pipeline.addLast("codec", new HttpClientCodec());

        pipeline.addLast(HttpRemoteForwardHandler.HANDLER_NAME, new HttpRemoteForwardHandler(uaChannel,
                remoteChannelInactiveCallback));

    }
}
