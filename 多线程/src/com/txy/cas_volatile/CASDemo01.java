package com.txy.cas_volatile;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 1. CAS
 *    比较并交换 compare and set
 */

public class CASDemo01 {
    public static void main(String[] args) {
        // 主内存中的值为5
        AtomicInteger atomicInteger = new AtomicInteger(5);
        // mian线程中修改atomicInteger的值
        System.out.println(atomicInteger.compareAndSet(5, 4) + "\t" + atomicInteger.get());

        System.out.println(atomicInteger.compareAndSet(5, 2000) + "\t" + atomicInteger.get());

        atomicInteger.getAndIncrement();
    }
}
