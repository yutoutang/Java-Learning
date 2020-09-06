package com.txy.groupchat;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

public class GroupServer {

    private Integer port = 6000;
    private ServerSocketChannel listChannel;
    private Selector selector;
    private int n = 0;

    public GroupServer(){
        try {
            selector = Selector.open();

            listChannel = ServerSocketChannel.open();

            listChannel.configureBlocking(false);

            listChannel.socket().bind(new InetSocketAddress(port));

            listChannel.register(selector, SelectionKey.OP_ACCEPT);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void listen(){
        System.out.println("服务器启动!");
        System.out.println(Thread.currentThread().getName());
        while (true){
            try {
                if (selector.select(10) != 0){
                    // 拿到所有连接 SelectionKeys
                    Set<SelectionKey> selectionKey = selector.selectedKeys();
                    Iterator<SelectionKey> keyIterator = selectionKey.iterator();
                    while (keyIterator.hasNext()){
                        SelectionKey key = keyIterator.next();

                        if (key.isAcceptable()){
                            // 处理 accept 事件，拿到 socketChannel 客户端连接成功
                            SocketChannel socketChannel = listChannel.accept();
                            socketChannel.configureBlocking(false);
                            System.out.println("客户端：" + socketChannel.getRemoteAddress() + "\t连接成功！");
                            // 客户端注册事件
                            socketChannel.register(selector, SelectionKey.OP_READ, ByteBuffer.allocate(1024));
                        }
                        else if (key.isReadable()){
                            readData(key);
                        }
                        keyIterator.remove();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public void readData(SelectionKey key){
        SocketChannel socketChannel = null;
        try {
            // 获取客户端发送的消息
            socketChannel = (SocketChannel) key.channel();
            // 通过 attachment 拿到缓冲区Buffer，并读取 channel 中的数据
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            int read = socketChannel.read(buffer);
            if (read > 0){
                StringBuffer sb = new StringBuffer();
                buffer.flip();
                while (buffer.hasRemaining()){
                    sb.append((char)buffer.get());
                }
                String msg = sb.toString();
                // 消息转发
                System.out.println("客户端：" + socketChannel.getRemoteAddress() + "\t发送信息：" + msg);
                sendInfoToOtherClients(msg, socketChannel);
            }
        } catch (IOException e) {
            try {
                System.out.println("客户端：" + socketChannel.getRemoteAddress() + "\t以下线");
                // 取消注册
                key.cancel();
                // 关闭通道
                socketChannel.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    // 向各个客户端发送，当前客户端的消息 有 BUG selector.selectedKeys() 的数量一直是 1
    public void sendInfoToOtherClients(String msg, SocketChannel self){
        System.out.println("服务器转发" + Thread.currentThread().getName());
        for (SelectionKey key : selector.keys()){

            Channel targetChannel =  key.channel();

            if (targetChannel instanceof SocketChannel && targetChannel != self){
               try {
                   // 转发消息
                   ByteBuffer byteBuffer = ByteBuffer.wrap(msg.getBytes());
                   ((SocketChannel)targetChannel).write(byteBuffer);
               } catch (IOException e) {
                   e.printStackTrace();
               }
            }
        }
    }

    public static void main(String[] args) {
        GroupServer groupServer = new GroupServer();

        groupServer.listen();
    }


}
