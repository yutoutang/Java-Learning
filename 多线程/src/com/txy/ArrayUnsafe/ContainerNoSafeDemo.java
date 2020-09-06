package com.txy.ArrayUnsafe;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

public class ContainerNoSafeDemo {

    /**
     * ArrayList线程不安全
     * 1. 故障现象
     *      java.util.ConcurrentModificationException
     *
     * 2. 导致原因
     *      并发争抢修改
     *      一个在写，一个在抢占资源，导致数据异常，
     *
     * 3. 解决方法
     *      3.1 Vector 类 加锁 保证数据一致性的同时，降低并发性
     *      3.2 Collections.synchronizedList
     *      3.3 CopyOnWriteArrayList 写时复制
     * 4. 优化建议
     *
     */
    public static void main(String[] args) {

        Map<String, String> map = new ConcurrentHashMap<>();

        for (int i=1; i <= 30; i++){
            new Thread(()->{
                map.put(Thread.currentThread().getName(),UUID.randomUUID().toString().substring(0,8));
                System.out.println(map);
            }, String.valueOf(i)).start();
        }

    }

    private static void setNoSafe() {
        Set<String> list = new CopyOnWriteArraySet<>();

        for (int i=1; i <= 30; i++){
            new Thread(()->{
                list.add(UUID.randomUUID().toString().substring(0,8));
                System.out.println(list);
            }, String.valueOf(i)).start();
        }
    }

    private static void listNoSafe() {
        List<String> list = new CopyOnWriteArrayList<>();

        for (int i=1; i <= 30; i++){
            new Thread(()->{
                list.add(UUID.randomUUID().toString().substring(0,8));
                System.out.println(list);
            }, String.valueOf(i)).start();
        }

        // java.util.ConcurrentModificationException
    }
}
