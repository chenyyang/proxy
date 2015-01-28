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

import com.chenyang.proxy.common.Constants;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpServer {

	private static Logger logger = LoggerFactory.getLogger(HttpServer.class);
	private EventLoopGroup bossGroup;
	private EventLoopGroup workerGroup;

	public void start() {
		int port = Constants.Http.PORT;

		logger.info("ApnProxy Server Listen on: " + port);

		ServerBootstrap serverBootStrap = new ServerBootstrap();

		bossGroup = new NioEventLoopGroup(1);
		workerGroup = new NioEventLoopGroup();

		try {
			serverBootStrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).localAddress(port)
					.childHandler(new HttpServerChannelInitializer());
			serverBootStrap.bind().sync().channel().closeFuture().sync();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			logger.error("showdown the server");
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}

	public void shutdown() {
		bossGroup.shutdownGracefully();
		workerGroup.shutdownGracefully();
	}

	public static void main(String[] arg) {
		logger.info("Start apnproxy server for junit test");
		HttpServer server = new HttpServer();
		server.start();
	}

}
