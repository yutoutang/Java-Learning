package com.txy.exe;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 生产和消费模式 lock方式 多线程打印
 * 一个初始值为零的变量，两个线程对其交替操作，一个加1，一个减1
 * 一个生产 一个消费
 * lock实现
 */

class MyDemo3R {
    private int number = 0;
    private Lock lock = new ReentrantLock();
    private Condition c = lock.newCondition();

    public void prod(){
        lock.lock();
        try{
            while (number != 0){
                c.await();
            }
            System.out.println(Thread.currentThread().getName() + "\t生产：" + number);
            number += 1;
            c.signalAll();

        }catch (Exception e){
            e.printStackTrace();
        }finally{
            lock.unlock();
        }
    }

    public void con(){
        lock.lock();
        try{
            while (number !=1){
                c.await();
            }
            System.out.println(Thread.currentThread().getName() + "\t消费：" + number);
            number -= 1;
            c.signalAll();
        }catch (Exception e){
            e.printStackTrace();
        }finally{
            lock.unlock();
        }
    }
}

public class demo3 {
    public static void main(String[] args) {
        MyDemo3R myDemo3R = new MyDemo3R();

        new Thread(()->{
            for (int i =0; i < 10; i++){
                myDemo3R.prod();
            }
        },"AA").start();

        new Thread(()->{
            for (int i =0; i < 10; i++){
                myDemo3R.con();
            }
        },"BB").start();

        new Thread(()->{
            for (int i =0; i < 10; i++){
                myDemo3R.prod();
            }
        },"CC").start();

        new Thread(()->{
            for (int i =0; i < 10; i++){
                myDemo3R.con();
            }
        },"DD").start();

    }
}
