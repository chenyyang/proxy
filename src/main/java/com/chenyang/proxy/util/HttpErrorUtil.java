
package com.chenyang.proxy.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpMessage;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;


public class HttpErrorUtil {

	public static HttpMessage buildHttpErrorMessage(HttpResponseStatus status, String errorMsg) {
		ByteBuf errorResponseContent = Unpooled.copiedBuffer(errorMsg, CharsetUtil.UTF_8);
		// send error response
		FullHttpMessage errorResponseMsg = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, errorResponseContent);
		errorResponseMsg.headers().add(HttpHeaders.Names.CONTENT_ENCODING, CharsetUtil.UTF_8.name());
		errorResponseMsg.headers().add(HttpHeaders.Names.CONTENT_LENGTH, errorResponseContent.readableBytes());

		return errorResponseMsg;
	}

    public static void writeAndFlush(Channel ch, HttpResponseStatus status) {
        if (ch.isActive() && status != null) {
            ch.writeAndFlush(HttpErrorUtil.buildHttpErrorMessage(status, status.reasonPhrase()));
        }
    }
}
