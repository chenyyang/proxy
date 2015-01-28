
package com.chenyang.proxy.socks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class SendMessageDecoder extends ByteToMessageDecoder {

	private static Logger logger = LoggerFactory.getLogger(SendMessageDecoder.class);

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		logger.info("decode lenth : {} , capacity : {}, writableBytes : {} write index : {} , read index : {}", in.readableBytes(), in.capacity(),
				in.writableBytes(), in.writerIndex(), in.readerIndex());
		if (in.readableBytes() >= 4) {
			out.add(in.readBytes(in.readableBytes()));
		}

	}

}
