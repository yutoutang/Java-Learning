package com.txy.jvm;

import java.util.Random;

public class T1 {

    public static void main(String[] args) {
//        long maxM = Runtime.getRuntime().maxMemory();
//        long totalM = Runtime.getRuntime().totalMemory();
//        System.out.println(maxM / (double)1024 / 1024);
//        System.out.println(totalM/ (double)1024 / 1024);
        String str = "A";
        while (true){
            str += str + new Random().nextInt(88888);
        }

    }
}
