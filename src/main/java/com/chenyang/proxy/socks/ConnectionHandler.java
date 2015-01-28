
package com.chenyang.proxy.socks;


import com.chenyang.proxy.util.ConnectionCntUtil;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;

public class ConnectionHandler extends ChannelDuplexHandler {

	@Override
	public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
		if (ConnectionCntUtil.addConnectionCnt()) {
			super.channelRegistered(ctx);
			int i=0;
		} else {
			SocksServerUtils.closeOnFlush(ctx.channel());
		}
	}

	@Override
	public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
		ConnectionCntUtil.reduceConnectionCnt();
		super.channelUnregistered(ctx);
	}



}
