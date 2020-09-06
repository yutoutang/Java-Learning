package com.txy.block;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 *  一个初始值为零的变量，两个线程对其交替操作，一个加1，一个减1，抢5
 *
 *  1  线程   操作   资源类
 *  2  判断   干活   通知
 *  3  防止虚假唤醒机制 用 while 替换 if
 *
 *  AA	1
 *  BB	0
 *  AA	1
 *  BB	0
 *  AA	1
 *  BB	0
 *  AA	1
 *  BB	0
 *  AA	1
 *  BB	0
 */

class ShareData{
    private int number = 0;
    private Lock lock = new ReentrantLock();
    private Condition condition = lock.newCondition();

    public void increment() throws Exception{
        // 加锁
        lock.lock();
        try{
            // 1. 判断
            while (number != 0){
                // 等待 不能生产
                condition.await();
            }
            // 2. 干活
            number ++;
            System.out.println(Thread.currentThread().getName() + "\t" + number);
            // 3. 通知唤醒
            condition.signalAll();
        }catch (Exception e){
            e.printStackTrace();
        }finally{
            lock.unlock();
        }
    }

    public void decrement() throws Exception{
        // 加锁
        lock.lock();
        try{
            // 1. 判断
            while (number == 0){
                // 等待 不能生产
                condition.await();
            }
            // 2. 干活
            number --;
            System.out.println(Thread.currentThread().getName() + "\t" + number);
            // 3. 通知唤醒
            condition.signalAll();
        }catch (Exception e){
            e.printStackTrace();
        }finally{
            lock.unlock();
        }
    }

}

public class ProdConsumer_Demo {

    public static void main(String[] args) {
        ShareData shareData = new ShareData();

        new Thread(()->{
            for (int i = 0; i < 5; i++) {
                try {
                    shareData.increment();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        },"AA").start();

        new Thread(()->{
            for (int i = 0; i < 5; i++) {
                try {
                    shareData.decrement();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        },"BB").start();

        new Thread(()->{
            for (int i = 0; i < 5; i++) {
                try {
                    shareData.increment();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        },"CC").start();

        new Thread(()->{
            for (int i = 0; i < 5; i++) {
                try {
                    shareData.decrement();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        },"DD").start();
    }
}
