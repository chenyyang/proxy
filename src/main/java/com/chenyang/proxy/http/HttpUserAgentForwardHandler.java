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
import com.chenyang.proxy.http.HttpRemoteForwardHandler.RemoteChannelInactiveCallback;
import com.chenyang.proxy.util.HttpErrorUtil;
import com.chenyang.proxy.util.NetworkUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.util.ReferenceCountUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.Map.Entry;

public class HttpUserAgentForwardHandler extends ChannelInboundHandlerAdapter implements RemoteChannelInactiveCallback {

    private static Logger logger = LoggerFactory.getLogger(HttpUserAgentForwardHandler.class);
    public static final String HANDLER_NAME = "apnproxy.useragent.forward";

    private Map<String, Channel> remoteChannelMap = new HashMap<String, Channel>();

    private List<HttpContent> httpContentBuffer = new ArrayList<HttpContent>();

    @Override
    public void channelRead(final ChannelHandlerContext uaChannelCtx, final Object msg) throws Exception {

        final Channel uaChannel = uaChannelCtx.channel();

        final HttpRemote apnProxyRemote = uaChannel.attr(HttpConnectionAttribute.ATTRIBUTE_KEY).get().getRemote();

        if (msg instanceof HttpRequest) {
            HttpRequest httpRequest = (HttpRequest) msg;

            Channel remoteChannel = remoteChannelMap.get(apnProxyRemote.getRemoteAddr());

            if (remoteChannel != null && remoteChannel.isActive()) {
                HttpRequest request = constructRequestForProxy(httpRequest, apnProxyRemote);
                remoteChannel.writeAndFlush(request);
            } else {

                Bootstrap bootstrap = new Bootstrap();
                bootstrap.group(uaChannel.eventLoop()).channel(NioSocketChannel.class).option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                        .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT).option(ChannelOption.AUTO_READ, false)
                        .handler(new HttpRemoteForwardChannelInitializer(uaChannel, this));

                ChannelFuture remoteConnectFuture = bootstrap.connect(apnProxyRemote.getInetSocketAddress(), new InetSocketAddress(
                        NetworkUtils.getCyclicLocalIp().getHostAddress(), 0));
                remoteChannel = remoteConnectFuture.channel();
                remoteChannelMap.put(apnProxyRemote.getRemoteAddr(), remoteChannel);

                remoteConnectFuture.addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        if (future.isSuccess()) {
                            future.channel().write(constructRequestForProxy((HttpRequest) msg, apnProxyRemote));

                            for (HttpContent hc : httpContentBuffer) {
                                future.channel().writeAndFlush(hc);

                                if (hc instanceof LastHttpContent) {
                                    future.channel().writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(new ChannelFutureListener() {
                                        @Override
                                        public void operationComplete(ChannelFuture future) throws Exception {
                                            if (future.isSuccess()) {
                                                future.channel().read();
                                            }

                                        }
                                    });
                                }
                            }
                            httpContentBuffer.clear();
                        } else {
                            HttpErrorUtil.writeAndFlush(uaChannel, HttpResponseStatus.INTERNAL_SERVER_ERROR);
                            httpContentBuffer.clear();
                            future.channel().close();
                        }
                    }
                });

            }
            ReferenceCountUtil.release(msg);
        } else {
            Channel remoteChannel = remoteChannelMap.get(apnProxyRemote.getRemoteAddr());
            HttpContent hc = ((HttpContent) msg);
            if (remoteChannel != null && remoteChannel.isActive()) {
                remoteChannel.writeAndFlush(hc);

                if (hc instanceof LastHttpContent) {
                    remoteChannel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(new ChannelFutureListener() {
                        @Override
                        public void operationComplete(ChannelFuture future) throws Exception {
                            if (future.isSuccess()) {
                                future.channel().read();
                            }

                        }
                    });
                }
            } else {
                httpContentBuffer.add(hc);
            }
        }

    }

    @Override
    public void channelInactive(ChannelHandlerContext uaChannelCtx) throws Exception {

        for (Map.Entry<String, Channel> entry : remoteChannelMap.entrySet()) {
            final Channel remoteChannel = entry.getValue();
            remoteChannel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    remoteChannel.close();
                }
            });
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext uaChannelCtx, Throwable cause) throws Exception {
        logger.error("error", cause);
        uaChannelCtx.close();
    }

    @Override
    public void remoteChannelInactive(final Channel uaChannel, String inactiveRemoteAddr) throws Exception {

        remoteChannelMap.remove(inactiveRemoteAddr);

        if (uaChannel.isActive()) {
            uaChannel.writeAndFlush(Unpooled.EMPTY_BUFFER);
        }

    }

    private HttpRequest constructRequestForProxy(HttpRequest httpRequest, HttpRemote apnProxyRemote) {

        String uri = httpRequest.getUri();
        uri = this.getPartialUrl(uri);
        HttpRequest _httpRequest = new DefaultHttpRequest(httpRequest.getProtocolVersion(), httpRequest.getMethod(), uri);
        Set<String> headerNames = httpRequest.headers().names();
        for (String headerName : headerNames) {
            if (StringUtils.equalsIgnoreCase(headerName, "Proxy-Connection")) {
                _httpRequest.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
            } else {
                _httpRequest.headers().add(headerName, httpRequest.headers().getAll(headerName));
            }
        }
        Iterator<Entry<String, String>> iterator = _httpRequest.headers().iterator();
        while (iterator.hasNext()) {
            Entry<String, String> entry = iterator.next();
            logger.info(" heard : {} {}", entry.getKey(), entry.getValue());
        }
        return _httpRequest;
    }

    private String getPartialUrl(String fullUrl) {
        if (StringUtils.startsWith(fullUrl, "http")) {
            int idx = StringUtils.indexOf(fullUrl, "/", 7);
            return idx == -1 ? "/" : StringUtils.substring(fullUrl, idx);
        }

        return fullUrl;
    }

}
