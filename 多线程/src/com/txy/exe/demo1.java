package com.txy.exe;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 三个线程顺序打印 0 - 100
 */

// 利用 ReentrantLock
class MyDemo1R {
    private int flag = 1; // A:1 B:2 C:3
    private Lock lock = new ReentrantLock();
    private Condition c1 = lock.newCondition();
    private Condition c2 = lock.newCondition();
    private Condition c3 = lock.newCondition();


    public void PrintA(AtomicInteger count){
        lock.lock();
        try{
            // 判读当前线程是否为 A 不是则 C1 等待
            while (flag != 1){
                c1.await();
            }
            System.out.println(Thread.currentThread().getName() + ":" + count);
            // CAS 增 防止线程不安全 造成数据丢失
            count.getAndIncrement();
            // 通知B
            flag = 2;
            c2.signal();
        }catch (Exception e){
            e.printStackTrace();
        }finally{
            lock.unlock();
        }
    }

    public void PrintB(AtomicInteger count){
        lock.lock();
        try{
            while (flag != 2){
                c2.await();
            }
            System.out.println(Thread.currentThread().getName() + ":" + count);
            count.getAndIncrement();
            // 通知B
            flag = 3;
            c3.signal();
        }catch (Exception e){
            e.printStackTrace();
        }finally{
            lock.unlock();
        }
    }

    public void PrintC(AtomicInteger count){
        lock.lock();
        try{
            while (flag != 3){
                c3.await();
            }
            System.out.println(Thread.currentThread().getName() + ":" + count);

            count.getAndIncrement();
            // 通知B
            flag = 1;
            c1.signal();
        }catch (Exception e){
            e.printStackTrace();
        }finally{
            lock.unlock();
        }
    }
}

public class demo1 {

    public static void main(String[] args) {
        MyDemo1R r = new MyDemo1R();
        AtomicInteger count = new AtomicInteger();

        new Thread(()->{
            while (count.get() < 99){
                r.PrintA(count);
            }
        },"A").start();

        new Thread(()->{
            while (count.get() < 99){
                r.PrintB(count);
            }
        },"B").start();

        new Thread(()->{
            while (count.get() < 99){
                r.PrintC(count);
            }
        },"C").start();

    }

}
