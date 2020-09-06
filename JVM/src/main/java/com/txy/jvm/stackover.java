package com.txy.jvm;

public class stackover {

    public static void m1(){
        m1();
    }

    public static void main(String[] args) {
        System.out.println("!111");
        m1();
        System.out.println("222");
    }
}
