
package com.chenyang.proxy.util;

import com.chenyang.proxy.common.Constants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

public class ConnectionCntUtil {

	private static AtomicInteger connectionCnt = new AtomicInteger(0);
	private static int lastSecondAddConnectionCnt = 0;
	private static int lastSecondReduceConnectionCnt = 0;
	private static final Logger logger = LoggerFactory.getLogger(ConnectionCntUtil.class);
	
	static{
		new Thread(){
			@Override
			public void run() {
				while (true) {
					logger.info("current connection: {}, last second add connection: {}, reduce connection: {}", // NL
							ConnectionCntUtil.getAllConnectionCnt(), lastSecondAddConnectionCnt, lastSecondReduceConnectionCnt);
					lastSecondAddConnectionCnt = 0;
					lastSecondReduceConnectionCnt = 0;
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}.start();
	}
	
	public static boolean addConnectionCnt() {
		connectionCnt.incrementAndGet();
		lastSecondAddConnectionCnt++;
		if (getAllConnectionCnt() > Constants.CONNECTION_LIMIT) {
			return false;
		}
		return true;
	}

	public static void reduceConnectionCnt() {
		connectionCnt.decrementAndGet();
		lastSecondReduceConnectionCnt++;
	}

	public static int getAllConnectionCnt() {
		return connectionCnt.get();
	}



}
