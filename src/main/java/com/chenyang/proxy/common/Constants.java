package com.chenyang.proxy.common;

import com.chenyang.proxy.util.NetworkUtils;

public interface Constants {

    public interface Http {
        public static final String HOST = "Host";
        public static final String SERVER = "Server";
        public static final String CONTENT_LENGTH = "Content-Length";
        public static final String CONTENT_TYPE = "Content-Type";
        public static final String CONTENT_ENCODING = "Content-Encoding";
        public static final String TEST_HTML = "this is <b>test</b> html!";
        public static final boolean DEBUG = false;
        public static final String TEXT_HTML = "text/html";
        public static final int DEFAULT_PORT = 80;
        public static final String CONNECTION = "Connection";
        public static final String PROXY_CONNECTION = "Proxy-Connection";
        public static final String PROXY_SERVER_NAME = "zms-java-proxy";
        public static final boolean USE_SYSOUT = true;
        public static final byte[] LINE_BREAK = new byte[] { '\r', '\n' };
        public static final int PORT = 8081;
    }

    public static final int BACK_LOG = 1000;

    public interface Socks {
        public static final int PORT = 1080;
    }

    static final int CONNECTION_LIMIT = NetworkUtils.getValidPortCount();

    public interface ThriftServer {
        public static final int PORT = 8082;
        public static final String errorReqResponse = "{\"code\":-1,\"description\":\"请求参数错误\"} ";
        public static final String FILE_KEY_AGENT_ADDRESSES = "agentAddresses";
        public static final String FILE_KEY_AGENT_DOMAINS = "agentDomains";
        public static final String FILE_VALUE_SPLIT = ",";
    }
}
