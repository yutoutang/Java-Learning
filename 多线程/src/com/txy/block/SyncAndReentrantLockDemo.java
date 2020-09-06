package com.txy.block;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 题目：多个线程之间按顺序调用，实现 A->B->C三个线程启动，
 * AA打印5次、BB打印10次、CC打印15次
 *
 * 来10轮
 */

class ShareResource{
    private int number = 1; // A:1, B:2, C:3
    private Lock lock = new ReentrantLock();
    private Condition c1 = lock.newCondition(); // A
    private Condition c2 = lock.newCondition(); // B
    private Condition c3 = lock.newCondition(); // C


    // 可以利用枚举的方法 将下面三个函数 合并为1个
    public void Print5(){
        lock.lock();
        try{
            // 1. 判断
            while (number != 1){
                c1.await();
            }
            // 2. 干活
            for (int i = 0; i < 5; i++) {
                System.out.println(Thread.currentThread().getName() + "\t" + i);
            }
            // 3. 通知B
            number = 2;
            c2.signal();
        }catch (Exception e){
            e.printStackTrace();
        }finally{
            lock.unlock();
        }
    }

    public void Print10(){
        lock.lock();
        try{
            // 1. 判断
            while (number != 2){
                c2.await();
            }
            // 2. 干活
            for (int i = 0; i < 10; i++) {
                System.out.println(Thread.currentThread().getName() + "\t" + i);
            }
            // 3. 通知B
            number = 3;
            c3.signal();
        }catch (Exception e){
            e.printStackTrace();
        }finally{
            lock.unlock();
        }
    }

    public void Print15(){
        lock.lock();
        try{
            // 1. 判断
            while (number != 3){
                c3.await();
            }
            // 2. 干活
            for (int i = 0; i < 15; i++) {
                System.out.println(Thread.currentThread().getName() + "\t" + i);
            }
            // 3. 通知B
            number = 1;
            c1.signal();
        }catch (Exception e){
            e.printStackTrace();
        }finally{
            lock.unlock();
        }
    }
}


public class SyncAndReentrantLockDemo {

    public static void main(String[] args) {

        ShareResource shareResource = new ShareResource();

        new Thread(()->{
            for (int i = 0; i < 10; i++) {
                shareResource.Print5();
            }
        }, "A").start();

        new Thread(()->{
            for (int i = 0; i < 10; i++) {
                shareResource.Print10();
            }
        }, "B").start();

        new Thread(()->{
            for (int i = 0; i < 10; i++) {
                shareResource.Print15();
            }
        }, "C").start();
    }
}
