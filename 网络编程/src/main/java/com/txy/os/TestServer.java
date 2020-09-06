package com.txy.os;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * NIO 实现简单的聊天室
 */

public class TestServer {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(8090);
        System.out.println("step 1: new ServerSocket");

        while (true){
            Socket client = serverSocket.accept();
            System.out.println("step 2: client\t" + client.getPort());
            new Thread(()->{
                try {
                    InputStream ins = client.getInputStream();
                    BufferedReader buf = new BufferedReader(new InputStreamReader(ins));
                    while (true){
                        System.out.println(buf.readLine());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }
}
