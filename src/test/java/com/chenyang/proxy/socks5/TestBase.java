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

import com.chenyang.proxy.AgentMain;
import com.chenyang.proxy.EchoServer;
import com.chenyang.proxy.common.AgentAddress;
import com.chenyang.proxy.common.AgentException;
import com.chenyang.proxy.common.ValidDest;
import com.chenyang.proxy.util.HostAuthenticationUtil;
import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author xmx
 * @version $Id: com.xx_dev.apn.proxy.test.TestProxyBase 14-1-8 16:13 (xmx) Exp $
 */
public class TestBase {

    private static final Logger logger = Logger.getLogger(TestBase.class);

    @BeforeClass
    public static void setUpServer() {

        AgentAddress host = new AgentAddress();
        host.setIp("127.0.0.1");
        host.setPort(EchoServer.PORT);
        List<AgentAddress> addresses = new ArrayList<AgentAddress>();
        addresses.add(host);
        ValidDest validDest = new ValidDest(addresses, new ArrayList<String>());
        try {
            HostAuthenticationUtil.syncDest(validDest);
        } catch (AgentException e) {
            e.printStackTrace();
        }

        new Thread() {
            @Override
            public void run() {
                try {
                    EchoServer.start();
                } catch (Exception e) {
                    logger.error(" target server start error ");
                }
            }
        }.start();

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    AgentMain.main(null);
                } catch (Exception e) {
                    logger.error(" socks server start error ");
                }
            }
        });

        t.start();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
    }

    @AfterClass
    public static void shutDownServer() {
        logger.info("Shutdown apnproxy server after junit test");
    }

    @Test
    public void testHttp() throws IOException {
        HttpTest.http(200, "http://127.0.0.1:" + EchoServer.PORT + "/");
    }

    @Test
    public void testSocks() throws IOException {
        SocksTest.socks(200, "http://127.0.0.1:" + EchoServer.PORT + "/");
    }

}
