package com.txy.lock;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 多个线程同时读一个资源类没有任何问题，所以为了满足并发量，读取共享资源应该同时进行
 *
 * 但是如果有一个线程去写共享资源，就不应该再其他线程对该资源进行读和写
 *
 * 读 - 读 共存
 * 读 - 写 不共存
 * 写 - 写 不共存
 *
 * 写操作：原子 + 独占 整个过程必须是完整的，中间不能被分割、打断 正在写入1 1写入完成
 *
 * 正确流程：
 * 4	 正在写入：4
 * 4	 写入完成
 * 5	 正在写入：5
 * 5	 写入完成
 *
 * 错误流程：
 * 3	 正在写入：3
 * 2	 正在写入：2
 * 5	 正在写入：5
 * 1	 正在写入：1
 */

// 资源类
class MyCache{
    // 缓存资源需要用 volatile 修饰 保证可见性
    private volatile Map<String, Object> map = new HashMap<>();
    private ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

    // 写
    public void put(String key, Object value){
        rwLock.writeLock().lock();
        try{
            System.out.println(Thread.currentThread().getName() + "\t 正在写入：" + key);

            try{
                TimeUnit.MILLISECONDS.sleep(300);
            }catch(InterruptedException e){
                e.printStackTrace();
            }
            map.put(key, value);
            System.out.println(Thread.currentThread().getName() +  "\t 写入完成");
        }catch (Exception e){
            e.printStackTrace();
        }finally{
            rwLock.writeLock().unlock();
        }

    }

    // 读
    public void get(String key){
        rwLock.readLock().lock();
        try{
            System.out.println(Thread.currentThread().getName() + "\t 正在读取");

            try{
                TimeUnit.MILLISECONDS.sleep(300);
            }catch(InterruptedException e){
                e.printStackTrace();
            }
            Object o = map.get(key);
            System.out.println(Thread.currentThread().getName() + "\t 读取完成" + o);
        }catch (Exception e){
            e.printStackTrace();
        }finally{
            rwLock.readLock().unlock();
        }
    }
}
public class ReadWriteLockDemo {

    public static void main(String[] args) {
        MyCache myCache = new MyCache();
        for (int i=1; i <= 5; i++){
            final int tempInt = i;
            new Thread(()->{
                // 写
                myCache.put(tempInt+"",tempInt+"");
            }, String.valueOf(i)).start();
        }

        for (int i=1; i <= 5; i++){
            final int tempInt = i;
            new Thread(()->{
                myCache.get(tempInt + "");
            }, String.valueOf(i)).start();
        }
    }
}
