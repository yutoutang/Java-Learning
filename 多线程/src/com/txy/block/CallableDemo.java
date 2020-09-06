package com.txy.block;


import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

class MyThread implements Callable<Integer>{

    @Override
    public Integer call() throws Exception {
        System.out.println(Thread.currentThread().getName() + "***** come in callable");

        try{
            TimeUnit.SECONDS.sleep(2);
        }catch(InterruptedException e){
            e.printStackTrace();
        }
        return 1024;
    }
}

public class CallableDemo {

    public static void main(String[] args) throws ExecutionException, InterruptedException {


        // FutureTask 实现 Callable接口
        FutureTask<Integer> futureTask = new FutureTask<>(new MyThread());
//        FutureTask<Integer> futureTask1 = new FutureTask<>(new MyThread());

        Thread t1 = new Thread(futureTask, "AA"); // callable开启线程去计算
        Thread t2 = new Thread(futureTask, "BB"); // 复用AA线程的计算结果 只进去AA线程
        t1.start();
        t2.start();
        System.out.println(Thread.currentThread().getName() + "**************");
        // int result02 = futureTask.get();  阻塞main线程

        int result01 = 100;
        // 要求获得callable的计算结果，如果没有计算完成就去响应，会导致堵塞，直到计算完成
        int result02 = futureTask.get();

        System.out.println(result01 + result02);
    }

}
