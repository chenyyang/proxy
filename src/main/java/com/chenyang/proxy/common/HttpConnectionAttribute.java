package com.chenyang.proxy.common;

import io.netty.util.AttributeKey;

public class HttpConnectionAttribute {

    public static final AttributeKey<HttpConnectionAttribute> ATTRIBUTE_KEY = AttributeKey.valueOf("connection_context");

    private String uaAddress;

    private String method;

    private String url;

    private String httpVersion;

    private String ua;

    private HttpRemote remote;

    private HttpConnectionAttribute() {

    }

    public static HttpConnectionAttribute build(String uaAddress, String method, String url, String httpVersion, String ua, HttpRemote remote) {
        HttpConnectionAttribute instance = new HttpConnectionAttribute();

        instance.uaAddress = uaAddress;
        instance.method = method;
        instance.url = url;
        instance.httpVersion = httpVersion;
        instance.ua = ua;
        instance.remote = remote;

        return instance;
    }

    public HttpRemote getRemote() {
        return this.remote;
    }

    public String toString() {
        return uaAddress + ", " + method + " " + url + " " + httpVersion + ", UA: " + ua + ", REMOTE: " + remote;
    }
}
