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

package com.chenyang.proxy.socks5;


import org.junit.Assert;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author xmx
 * @version $Id: com.xx_dev.apn.proxy.test.TestProxyWithNetty 14-1-8 16:13 (xmx) Exp $
 */
public class HttpTest {

	public static void http(int respStatus, String urlPath) throws IOException {
		SocketAddress addr = new InetSocketAddress("127.0.0.1", 8081);
		Proxy proxy = new Proxy(Proxy.Type.HTTP, addr);
		URL url = new URL(urlPath);
		URLConnection conn = url.openConnection(proxy);
		HttpURLConnection httpURLConnection = (HttpURLConnection) conn;

		Assert.assertEquals(respStatus, httpURLConnection.getResponseCode());

		InputStream input=conn.getInputStream();
		StringBuffer buf = new StringBuffer();
		
		byte[] resp = new byte[1024];
		while ((input.read(resp)) >= 0) {
			buf.append(new String(resp));
		}
		Assert.assertTrue(buf.toString().contains("OK"));
	}

}
