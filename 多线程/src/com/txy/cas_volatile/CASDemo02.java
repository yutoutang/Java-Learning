package com.txy.cas_volatile;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

// 主内存中 a线程调用addTo60 b线程收到变量被修改的通知
class MyData{ // MyData.java => MyData.class => JVM字节码
    volatile int num = 0;

    public void addTo60(){
        this.num = 60;
    }

    public void addPlus(){
        // num++ 再多线程下是不安全的
        // ++ -> 编译为字节码对应三个指令
        // 获得初始值（getfield）,相加（iadd），写回主内存（putfield）三个操作在多线程抢占时，某些操作会分割
        // 数值小于20000 出现了数值丢失
        num++;
    }

    AtomicInteger atomicInteger = new AtomicInteger();

    public void addPlus2(){
        // 最小单位不可分割
        // CAS操作
        atomicInteger.getAndIncrement();
    }

}

/**
 * 1. 验证volatile的可见性
 *   1.1 num属性不加volatile属性
 *   1.2 num属性添加volatile属性
 * 2. 验证volatile不保证原子性
 *   2.1 原子性：不可分割、完整性，某个线程正在做某个具体业务时，中间不可以被加塞或分割，需要整体同时成功或同时失败
 *   2.2 演示 结果有问题
 *   2.3 解决
 *       * 加sync
 *       * AtomicInteger
 */
public class demo1 {
    // volatile保证原子性，并解决
    private static void setAtomic() {
        MyData myData = new MyData();

        for (int i=1; i <= 20; i++){
            new Thread(()->{
                for (int j = 0; j < 1000 ; j++) {
                    // 不保证原子性
                    myData.addPlus();
                    // 保证原子性
                    myData.addPlus2();
                }
            }, String.valueOf(i)).start();
        }
        // 需要等待20个线程计算完成后，再使用main线程查看结果
        // Thread.activeCount() 当前线程数 默认有两个线程 main + GC
        while (Thread.activeCount() > 2){
            // main线程礼让上面的20个业务线程
            Thread.yield();
        }
        // 最终的计算结果可能不是正确答案  20 * 1000
        System.out.println(Thread.currentThread().getName() + "\t" + "finally number value: " + myData.num);
        System.out.println(Thread.currentThread().getName() + "\t" + "finally number value: " + myData.atomicInteger);
    }

    private static void setOkByVolatile() {
        MyData myData = new MyData();

        new Thread(() -> {
            System.out.println(Thread.currentThread().getName() + "\t" + "come in");
            try{
                TimeUnit.SECONDS.sleep(3);
            }catch(InterruptedException e){
                e.printStackTrace();
            }
            myData.addTo60();
            System.out.println(Thread.currentThread().getName() + "\t" + "update num value" + myData.num);
        }, "A").start();

        // 第二线程是main线程
        while (myData.num == 0){
            // main线程等待，直到num值不再等于0

        }
        // 如果执行下面代码，则main线程被告知num值被修改
        // 向num添加 volatile 关键字后 main线程被告知
        System.out.println(Thread.currentThread().getName() + "\t" + "mission over");
    }
}
