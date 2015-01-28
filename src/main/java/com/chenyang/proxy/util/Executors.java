
package com.chenyang.proxy.util;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Executors {

    private static Executors instance;

    public static Executors getInstance() {
        if(instance==null) {
            synchronized(Executors.class) {
                if(instance==null) {
                    instance=new Executors();
                }
            }
        }
        return instance;
    }

    private ExecutorService executor;

    private Executors() {
    }

    protected synchronized ExecutorService getExecutor() {
        if(executor==null) {
            final ThreadFactory parent= java.util.concurrent.Executors.defaultThreadFactory();
            ThreadFactory factory=new ThreadFactory() {
                @Override
                public Thread newThread(Runnable runnable) {
                    Thread thread=parent.newThread(runnable);
                    thread.setDaemon(true);
                    return thread;
                }
            };
			executor = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), factory);
        }
        return executor;
    }

    public void shutdown() {
        if(executor!=null) {
            executor.shutdown();
        }
    }

    public <T> Future<T> submitCommon(Callable<T> call) {
        return getExecutor().submit(call);
    }

	public <T> Future<?> submitCommon(Runnable call) {
		return getExecutor().submit(call);
	}

}
