package com.chenyang.proxy.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;

import java.net.URI;
import java.net.URISyntaxException;

/**
 *
 * http工具类，解析host和ip
 */
public class HostNamePortUtil {

	private static final Logger logger = Logger.getLogger(HostNamePortUtil.class);

	public static String getHostName(HttpRequest httpRequest) {
		String originalHostHeader = httpRequest.headers().get(HttpHeaders.Names.HOST);

		if (StringUtils.isBlank(originalHostHeader) && httpRequest.getMethod().equals(HttpMethod.CONNECT)) {
			originalHostHeader = httpRequest.getUri();
		}

		if (StringUtils.isNotBlank(originalHostHeader)) {
			String originalHost = StringUtils.split(originalHostHeader, ": ")[0];
			return originalHost;
		} else {
			String uriStr = httpRequest.getUri();
			try {
				URI uri = new URI(uriStr);
				String originalHost = uri.getHost();

				return originalHost;
			} catch (URISyntaxException e) {
				logger.error(e.getMessage(), e);
				return null;
			}
		}
	}

	public static int getPort(HttpRequest httpRequest) {
		int originalPort = 80;

		if (httpRequest.getMethod().equals(HttpMethod.CONNECT)) {
			originalPort = 443;
		}

		String originalHostHeader = httpRequest.headers().get(HttpHeaders.Names.HOST);

		if (StringUtils.isBlank(originalHostHeader) && httpRequest.getMethod().equals(HttpMethod.CONNECT)) {
			originalHostHeader = httpRequest.getUri();
		}

		if (StringUtils.isNotBlank(originalHostHeader)) {
			if (StringUtils.split(originalHostHeader, ": ").length == 2) {
				originalPort = Integer.parseInt(StringUtils.split(originalHostHeader, ": ")[1]);
			}
		} else {
			String uriStr = httpRequest.getUri();
			try {
				URI uri = URI.create(uriStr);

				if (uri.getPort() > 0) {
					originalPort = uri.getPort();
				}
			} catch (IllegalArgumentException e) {
				logger.error(e.getMessage(), e);
				originalPort = -1;
			}
		}

		return originalPort;
	}

}
