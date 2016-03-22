package com.corntree.ps.common;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ExecutorManager {

    public static ExecutorService newCachedThreadPool(String poolName) {
        return Executors.newCachedThreadPool(new NamedThreadFactory(poolName));
    }

    public static ExecutorService newFixedThreadPool(int nThreads, String poolName) {
        return Executors.newFixedThreadPool(nThreads, new NamedThreadFactory(poolName));
    }
}
