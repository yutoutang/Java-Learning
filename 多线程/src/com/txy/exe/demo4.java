package com.txy.exe;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 阻塞队列 生产、消费
 * 一次进 一出 队列中单个变量
 */

class MyDemo4R {
    private volatile boolean FLAG = true; // 不加锁了 保证对象可见性 true 生产 false 消费
    private AtomicInteger atomicInteger = new AtomicInteger();
    private BlockingQueue<String> blockingQueue = null;


    public MyDemo4R(BlockingQueue<String> blockingQueue) {
        this.blockingQueue = blockingQueue;
    }

    public void prod() throws Exception{
        String data = null;
        boolean retValue;
        while (FLAG){
            data = atomicInteger.getAndIncrement() + "";
            retValue = blockingQueue.offer(data, 2L, TimeUnit.SECONDS); // 判读当前线程生产操作是否成功
            if (retValue){
                System.out.println(Thread.currentThread().getName() + "\t插入" + data + "成功");
            }else {
                System.out.println(Thread.currentThread().getName() + "\t插入失败");
            }
            TimeUnit.SECONDS.sleep(1);
        }
        System.out.println(Thread.currentThread().getName() + "生产结束");
    }

    public void con() throws Exception{
        String data = null;
        boolean retValue;
        while (FLAG){
            data = blockingQueue.poll(2L, TimeUnit.SECONDS);

            if (null == data || "".equalsIgnoreCase(data)){
                FLAG = false;
                System.out.println();
                System.out.println();
                System.out.println();
                System.out.println(Thread.currentThread().getName() + "停止消费");
            }
        }

        System.out.println(Thread.currentThread().getName() + "消费成功");
    }
}


public class demo4 {

    public static void main(String[] args) {
        MyDemo4R myDemo4R = new MyDemo4R(new ArrayBlockingQueue<String >(10));

        new Thread(()->{
            System.out.println(Thread.currentThread().getName() + "生产线程启动");
            try {
                myDemo4R.prod();
            } catch (Exception e) {
                e.printStackTrace();
            }
        },"AAA").start();

        new Thread(()->{
            System.out.println(Thread.currentThread().getName() + "消费线程启动");
            try {
                myDemo4R.con();
            } catch (Exception e) {
                e.printStackTrace();
            }
        },"BBB").start();


        try{
            TimeUnit.SECONDS.sleep(10);
        }catch(InterruptedException e){
            e.printStackTrace();
        }

        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println("结束");


    }
}
