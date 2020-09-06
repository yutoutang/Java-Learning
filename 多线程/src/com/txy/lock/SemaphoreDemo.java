package com.txy.lock;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class SemaphoreDemo {

    /**
     * 输出：
     * 1	抢到车位
     * 2	抢到车位
     * 3	抢到车位
     * 1	停车3秒后离开车位
     * 4	抢到车位
     * 2	停车3秒后离开车位
     * 5	抢到车位
     * 3	停车3秒后离开车位
     * 6	抢到车位
     *
     */

    public static void main(String[] args) {
        // 三个车位
        Semaphore semaphore = new Semaphore(3);
        // 6 抢 3
        for (int i=1; i <= 6; i++){
            new Thread(()->{
                try {
                    // 抢到
                    semaphore.acquire();
                    System.out.println(Thread.currentThread().getName() + "\t抢到车位");

                    try{
                        TimeUnit.SECONDS.sleep(3);
                    }catch(InterruptedException e){
                        e.printStackTrace();
                    }
                    System.out.println(Thread.currentThread().getName() + "\t停车3秒后离开车位");

                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    // 离开
                    semaphore.release();
                }
            }, String.valueOf(i)).start();
        }
    }
}
