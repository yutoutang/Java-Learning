package com.txy.bio;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BIOServer {

    public static void handler(Socket socket){
        try {
            System.out.println(Thread.currentThread().getName() + "\t连接");
            byte[] bytes = new byte[1024];
            // 获取输入流
            InputStream socketInputStream = socket.getInputStream();
            // 读取客户端发送数据
            while (true){
                // 读取完数据后，阻塞
                System.out.println(Thread.currentThread().getName() + "read...");
                int read = socketInputStream.read(bytes);
                if (read != -1){
                    System.out.println(new String(bytes, 0, read));
                }
                else {
                    break;
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        finally {
            System.out.println("关闭client");
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        // 1. 创建一个线程池
        ExecutorService threadPool = Executors.newCachedThreadPool();

        // 2. 如果有客户端连接就创建一个线程与此通信
        ServerSocket serverSocket = new ServerSocket(2000);

        System.out.println("服务器启动");

        while (true){
            System.out.println(Thread.currentThread().getName() + "等待连接");
            // 没有数据发送时，main线程一直卡着
            final Socket socket = serverSocket.accept();
            System.out.println("连接一个客户端");

            threadPool.execute(()->{
                handler(socket);
            });
        }
    }



}
