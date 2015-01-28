package com.chenyang.proxy;

import com.chenyang.proxy.http.HttpServer;
import com.chenyang.proxy.socks.SocksServer;
import com.chenyang.proxy.util.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

//http 代理是借鉴git@github.com:apn-proxy/apn-proxy.git socks5借鉴netty示例
public class AgentMain {

    private static Logger logger = LoggerFactory.getLogger(AgentMain.class);

    public static void main(String[] args) throws Exception {

        Executors.getInstance().submitCommon(new Runnable() {
            public void run() {
                HttpServer.main(null);
            }
        });

        new SocksServer().main(null);

    }

}
