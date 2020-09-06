package com.txy.exe;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * demo1 问题 将相同函数整合
 *
 */




public class demo2 {

    private final AtomicInteger count = new AtomicInteger();

    private static class MyDemoR2 implements Runnable {
        private Lock lock = new ReentrantLock();
        // 记录线程条件
        private Condition thiCondition;
        private Condition afterCondition;

        @Override
        public void run() {
            lock.lock();
            try{

            }catch (Exception e){
                e.printStackTrace();
            }finally{
                lock.unlock();
            }
        }
    }

}
