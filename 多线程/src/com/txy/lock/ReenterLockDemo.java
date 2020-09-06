package com.txy.lock;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

class Phone implements Runnable{

    // sendMs和sendMail拥有同一把锁
    public synchronized void sendMs() throws Exception{
        System.out.println(Thread.currentThread().getId() + "\t invoke sendMs()");
        sendEmail();
    }

    public synchronized void sendEmail() throws Exception{
        System.out.println(Thread.currentThread().getId() + "\t invoke sendEmail()");
    }

    Lock lock = new ReentrantLock();
    @Override
    public void run() {
        get();
    }

    public void get(){
        // lock 和 unlock 需要配对
        lock.lock();
        try {
            System.out.println(Thread.currentThread().getId() + "\t invoke get()");
            set();
        }finally {
            lock.unlock();
        }
    }

    public void set(){
        lock.lock();
        try {
            System.out.println(Thread.currentThread().getId() + "\t invoke set()");
        }finally {
            lock.unlock();
        }
    }
}

/**
 * 1.  synchronized是可重入锁
 * t1	 invoke sendMs()     t1线程在外层方法获取锁
 * t1	 invoke sendEmail()  t1在进入内层方法自动获取锁
 * t2	 invoke sendMs()
 * t2	 invoke sendEmail()
 *
 * 2.  ReentrantLocks是可重入锁
 * t3	 invoke get()
 * t3	 invoke set()
 * t4	 invoke get()
 * t4	 invoke set()
 *
 */

public class ReenterLockDemo {

    public static void main(String[] args) throws Exception {
        Phone phone = new Phone();

        new Thread(() -> {
            try {
                phone.sendMs();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, "t1").start();

        new Thread(() -> {
            try {
                phone.sendMs();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, "t2").start();


        Thread t3 = new Thread(phone);
        Thread t4 = new Thread(phone);

        t3.start();
        t4.start();

    }
}
