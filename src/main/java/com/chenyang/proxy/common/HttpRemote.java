package com.chenyang.proxy.common;

import java.net.InetSocketAddress;

public class HttpRemote {
    private String remoteHost;
    private int remotePort;
    private InetSocketAddress inetSocketAddress;

    public HttpRemote(String remoteHost, int remotePort) {
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
        inetSocketAddress = new InetSocketAddress(this.remoteHost, this.remotePort);
    }

    public final String getRemoteAddr() {
        return this.remoteHost + ":" + this.remotePort;
    }

    public String toString() {
        return this.getRemoteAddr();
    }

    public InetSocketAddress getInetSocketAddress() {
        return inetSocketAddress;
    }

}
