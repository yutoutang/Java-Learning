package com.txy.jvm;

public class MyObject {

    public static void main(String[] args) {
        Object object = new Object();

        System.out.println(object.getClass().getClassLoader());

        MyObject myObject = new MyObject();

        System.out.println(myObject.getClass().getClassLoader().getParent().getParent());
        System.out.println(myObject.getClass().getClassLoader().getParent());
        System.out.println(myObject.getClass().getClassLoader());

       Thread t1 = new Thread();
       t1.start();
       t1.start(); // 不能调用两次 java.lang.IllegalThreadStateException
    }

}
