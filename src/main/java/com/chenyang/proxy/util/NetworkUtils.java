
package com.chenyang.proxy.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class NetworkUtils {
	private static final Logger logger = LoggerFactory.getLogger(NetworkUtils.class);
	private static List<InetAddress> ipList = new ArrayList<InetAddress>();
	private static AtomicInteger index = new AtomicInteger(0);

	public static List<InetAddress> getLocalIpList() {
		if (ipList.isEmpty()) {
			try {
				for (NetworkInterface iface : Collections.list(NetworkInterface.getNetworkInterfaces())) {
					for (InetAddress raddr : Collections.list(iface.getInetAddresses())) {
						if (raddr.isSiteLocalAddress() && !raddr.isLoopbackAddress() && raddr instanceof Inet4Address) {
							logger.info("Checking ip address {}", raddr);
							ipList.add(raddr);
						}
					}
				}
			} catch (SocketException e) {
				logger.info("get local machine ip error.", e);
			}
		}
		return ipList;
	}

	public static int getValidPortCount() {
		try {
			int ipCountPerPort = 400000;
			int ipCount = Collections.list(NetworkInterface.getNetworkInterfaces()).size();
			// return (ipCount * ipCountPerPort) > 110000 ? 110000 : ipCount * ipCountPerPort;
			return ipCountPerPort;
		} catch (SocketException e) {
			logger.info("get local machine ip error.", e);
		}
		return 0;
	}

	public static InetAddress getCyclicLocalIp() {
		int offset = index.get() < Integer.MAX_VALUE ? index.getAndIncrement() % getLocalIpList().size() : index.getAndSet(0) % getLocalIpList().size();
		return getLocalIpList().get(offset);
	}

	public static long ipv4ToLong(String strIP) {
		long[] ip = new long[4];
        String[] ipStr = StringUtils.split(strIP, ".");
        if (ipStr == null || ipStr.length != 4) {
            logger.error(" ipStr.length  : {}", ipStr.length);
            return 0;
        }

        ip[0] = NumberUtils.toLong(ipStr[0]);
        ip[1] = NumberUtils.toLong(ipStr[1]);
        ip[2] = NumberUtils.toLong(ipStr[2]);
        ip[3] = NumberUtils.toLong(ipStr[3]);
		return (ip[0] << 24) + (ip[1] << 16) + (ip[2] << 8) + ip[3];
	}

    public static String longToIP(long longIp) {

        StringBuffer sb = new StringBuffer("");
        // 直接右移24位
        sb.append(String.valueOf((longIp >>> 24)));
        sb.append(".");
        // 将高8位置0，然后右移16位
        sb.append(String.valueOf((longIp & 0x00FFFFFF) >>> 16));
        sb.append(".");
        // 将高16位置0，然后右移8位
        sb.append(String.valueOf((longIp & 0x0000FFFF) >>> 8));
        sb.append(".");
        // 将高24位置0
        sb.append(String.valueOf((longIp & 0x000000FF)));
        return sb.toString();
    }

}
