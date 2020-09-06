package com.txy.cas_volatile;

public class SingletonDemo {

    volatile private static SingletonDemo instance = null;

    private SingletonDemo(){
        System.out.println(Thread.currentThread().getName() + "\t" + "构造方法");
    }

    // DCL（Double Check Lock 双端检锁机制）
    public static SingletonDemo getInstance(){
        // 加锁之前和之后双重判断 此外还需要控制指令重排的情况
        if (instance == null){
            synchronized (SingletonDemo.class){
                if (instance == null){
                    // memory = allocate() 分配内存空间 1
                    // instance(memory) 初始化对象 2
                    // instance = memory 设置instance指向的内存地址，此时instance != null 3
                    // 语句2和语句3不存在数据依赖关系，语句2和语句3可能发生重排为132的执行顺序，导致instance还没初始化时，就被访问了
                    instance = new SingletonDemo();
                }
            }
        }
        return instance;
    }

    public static void main(String[] args) {
        // 多线程后，单例模式失效
        for (int i=1; i <= 10; i++){
            new Thread(SingletonDemo::getInstance, String.valueOf(i)).start();
        }
    }

}
