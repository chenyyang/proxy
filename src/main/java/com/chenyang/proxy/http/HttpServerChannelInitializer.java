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

import com.chenyang.proxy.socks.ConnectionHandler;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;

public class HttpServerChannelInitializer extends ChannelInitializer<SocketChannel> {

    HttpPreHandler preHandler = new HttpPreHandler();
    HttpSchemaHandler schemaHandler = new HttpSchemaHandler();
    
    @Override
    public void initChannel(SocketChannel channel) throws Exception {
        ChannelPipeline pipeline = channel.pipeline();
        pipeline.addLast(new ConnectionHandler());
        pipeline.addLast("codec", new HttpServerCodec());
        pipeline.addLast(HttpPreHandler.HANDLER_NAME, preHandler);
        pipeline.addLast(HttpSchemaHandler.HANDLER_NAME, schemaHandler);
    }
}
