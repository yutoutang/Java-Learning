package com.txy.cas_volatile;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicStampedReference;

/**
 * ABA问题解决
 */

public class ABADemo {

    static AtomicReference<Integer> atomicReference = new AtomicReference<>(100);

    static AtomicStampedReference<Integer> atomicStampedReference = new AtomicStampedReference<>(100, 1);

    public static void main(String[] args) {
        System.out.println("===========ABA产生===========");

        new Thread(()->{
            atomicReference.compareAndSet(100, 101); // A -> B
            atomicReference.compareAndSet(101, 100); // B -> A
        },"t1").start();

        new Thread(()->{
            // 暂停1s，保证线程1完成ABA操作
            try{
                TimeUnit.SECONDS.sleep(1);
            }catch(InterruptedException e){
                e.printStackTrace();
            }
            System.out.println(atomicReference.compareAndSet(100, 2019) + "\t" + atomicReference.get());
        },"t2").start();


        try{
            TimeUnit.SECONDS.sleep(2);
        }catch(InterruptedException e){
            e.printStackTrace();
        }
        System.out.println("===========ABA解决===========");

        new Thread(()->{
            int stamp = atomicStampedReference.getStamp();
            System.out.println(Thread.currentThread().getName() + "\t第一次版本号" + stamp);

            try{
                TimeUnit.SECONDS.sleep(1);
            }catch(InterruptedException e){
                e.printStackTrace();
            }
            // ABA
            // 期望值、更新值、旧版本号、新版本号
            atomicStampedReference.compareAndSet(100, 101,
                    atomicStampedReference.getStamp(),
                    atomicStampedReference.getStamp()+1); // A - B
            System.out.println(Thread.currentThread().getName() + "\t第二次版本号" + atomicStampedReference.getStamp());

            atomicStampedReference.compareAndSet(101, 100,
                    atomicStampedReference.getStamp(),
                    atomicStampedReference.getStamp() + 1); // B - A
            System.out.println(Thread.currentThread().getName() + "\t第三次次版本号" + atomicStampedReference.getStamp());



        },"t3").start();

        new Thread(()->{
            int stamp = atomicStampedReference.getStamp();
            System.out.println(Thread.currentThread().getName() + "\t第一次版本号" + stamp);

            // 暂停3s 保证t3线程完成ABA操作
            try{
                TimeUnit.SECONDS.sleep(3);
            }catch(InterruptedException e){
                e.printStackTrace();
            }

            boolean res = atomicStampedReference.compareAndSet(100, 2019, stamp, stamp+ 1);

            System.out.println(Thread.currentThread().getName() + "\t修改成功否" + res);
            System.out.println(Thread.currentThread().getName() + "\t实际版本号" + atomicStampedReference.getStamp());
            System.out.println(Thread.currentThread().getName() + "\t当前实际值" + atomicStampedReference.getReference());
        },"t4").start();

    }
}
