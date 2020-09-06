package com.txy.lock;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 手写自选锁
 * 循环比较直到成功为止
 */

public class SpinLock {

    // 原子引用线程
    AtomicReference<Thread> atomicReference = new AtomicReference<>();

    public void myLock(){
        // 加锁
        // 当前进入线程
        Thread thread = Thread.currentThread();
        System.out.println(thread.getName() + "\t come in");

        // 自旋 while + CAS 多线程中保证当前锁被解锁时，才能锁下一个线程
        while (!atomicReference.compareAndSet(null, thread)){

        }
    }

    public void myUnLock(){
        // 解锁
        Thread thread = Thread.currentThread();
        atomicReference.compareAndSet(thread, null);
        System.out.println(thread + "\t invoke myUnLock");
    }


    /**
     * AA	 come in
     * BB	 come in
     * Thread[AA,5,main]	 invoke myUnLock
     * Thread[BB,5,main]	 invoke myUnLock
     *
     * A线程加锁 -> 等待5s后解锁
     * A线程加锁1s -> B线程加锁 (B线程需要等到A线程解锁后，才能成功加锁)
     *
     */
    public static void main(String[] args) {

        SpinLock spinLock = new SpinLock();

        new Thread(() -> {
            spinLock.myLock();

            try{
                TimeUnit.SECONDS.sleep(5);
            }catch(InterruptedException e){
                e.printStackTrace();
            }
            spinLock.myUnLock();
        }, "AA").start();


        try{
            TimeUnit.SECONDS.sleep(1);
        }catch(InterruptedException e){
            e.printStackTrace();
        }

        new Thread(() -> {
            spinLock.myLock();

            try{
                TimeUnit.SECONDS.sleep(1);
            }catch(InterruptedException e){
                e.printStackTrace();
            }
            spinLock.myUnLock();
            try{
                TimeUnit.SECONDS.sleep(5);
            }catch(InterruptedException e){
                e.printStackTrace();
            }
        }, "BB").start();

    }

}
