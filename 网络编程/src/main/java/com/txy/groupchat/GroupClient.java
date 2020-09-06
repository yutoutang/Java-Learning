package com.txy.groupchat;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

public class GroupClient {

    private String host = "127.0.0.1";
    private Integer port = 6000;
    private Selector selector;
    private SocketChannel socketChannel;
    private String username;

    public GroupClient(){
        try {
            selector = Selector.open();
            // 连接服务器
            socketChannel = SocketChannel.open(new InetSocketAddress(host, port));

            socketChannel.configureBlocking(false);

            socketChannel.register(selector, SelectionKey.OP_READ);

            username = socketChannel.getLocalAddress().toString().substring(1);

            System.out.println(username + "is ok...");

        } catch (IOException e) {
            System.out.println("服务器异常");
        }
    }

    // 客户端发送数据
    public void sendInfo(String msg){
        msg = username + ":" + msg;
        System.out.println(msg);
        try {
            ByteBuffer byteBuffer = ByteBuffer.wrap(msg.getBytes());
            socketChannel.write(byteBuffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 客户端读取服务器的转发
    public void readInfo(){
        try {
            if (selector.select() > 0){
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> keyIterator = selectionKeys.iterator();
                while (keyIterator.hasNext()){
                    SelectionKey key = keyIterator.next();
                    if (key.isReadable()){
                        SocketChannel sourceChannel = (SocketChannel)key.channel();

                        ByteBuffer buffer = ByteBuffer.allocate(1024);
                        sourceChannel.read(buffer);
                        StringBuffer sb = new StringBuffer();
                        buffer.flip();
                        while (buffer.hasRemaining()){
                            sb.append((char)buffer.get());
                        }
                        String msg = sb.toString();
                        System.out.println(msg);
                    }
                    keyIterator.remove();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public static void main(String[] args) {
        GroupClient groupClient = new GroupClient();
        System.out.println(Thread.currentThread().getName());

        // 每隔 3s 读取服务器端 发送数据
        new Thread(()->{
            while (true){
//                System.out.println("start");
                groupClient.readInfo();
//                try {
//                    Thread.sleep(300);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
            }
        }).start();

        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNext()){
            String s = scanner.nextLine();
            groupClient.sendInfo(s);
        }

    }

}
