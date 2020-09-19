package com.txy.lock;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadLocalDemo {

    private static final ThreadLocal<DateFormat> DATE_FORMAT = new ThreadLocal<DateFormat>(){
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // DateFormat 线程不安全
        }
    };

    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // 线程不安全
    // pool-1-thread-1	2020-09-19 10:57:45	4545-09-19 10:57:45
    // false
    ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(10, 100, 1,
            TimeUnit.SECONDS, new LinkedBlockingQueue<>(1000), Executors.defaultThreadFactory());

    // 线程不安全
    void test(){
        try {

            for (int i=1; i <= 20; i++){
                poolExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        String dataString = simpleDateFormat.format(new Date());
                        try {
                            Date parseDate = simpleDateFormat.parse(dataString);
                            String format = simpleDateFormat.format(parseDate);
                            System.out.println(Thread.currentThread().getName() + "\t" + dataString +"\t"+ format);
                            System.out.println(dataString.equals(format));

                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }finally {
            poolExecutor.shutdown();
        }
    }

    // 线程安全
    void test_safe() {
        try {
            for (int i = 1; i <= 20; i++) {
                poolExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        String dataString = DATE_FORMAT.get().format(new Date());

                        try {
                            Date parseDate = DATE_FORMAT.get().parse(dataString);
                            String format = DATE_FORMAT.get().format(parseDate);
                            System.out.println(Thread.currentThread().getName() + "\t" + dataString + "\t" + format);
                            System.out.println(dataString.equals(format));

                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        } finally {
            poolExecutor.shutdown();
        }
    }

    public static void main(String[] args) {
        ThreadLocalDemo t = new ThreadLocalDemo();
        t.test_safe();
    }

}
