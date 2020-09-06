package com.txy.lock;

import java.util.concurrent.CountDownLatch;

/**
 * CountDownLatch
 *
 * 问题执行
 * 1
 * 3
 * 2
 * 4
 * 5
 * main
 * 6
 */

public class CountDownLatchDemo {

    public static void main(String[] args) throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(6);

        for (int i=1; i <= 6; i++){
            new Thread(()->{
                System.out.println(Thread.currentThread().getName());
                countDownLatch.countDown();
            }, CountryEnum.forEach_CountryEnum(i).getRetMessage()).start();
        }

        // 等待 countDownLatch 减至零
        countDownLatch.await();
        System.out.println(Thread.currentThread().getName());

    }

    private static void closedoor() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(6);

        for (int i=1; i <= 6; i++){
            new Thread(()->{
                System.out.println(Thread.currentThread().getName());
                countDownLatch.countDown();
            }, String.valueOf(i)).start();
        }

        // 等待 countDownLatch 减至零
        countDownLatch.await();
        System.out.println(Thread.currentThread().getName());
    }


}
