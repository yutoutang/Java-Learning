package com.txy.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * NIO 服务端
 */
public class NIOServer {
    public static void main(String[] args) throws IOException {

        // 1. 创建 serverSocketChannel 并绑定为 port
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().bind(new InetSocketAddress(6666));
        // 非阻塞模式selectKeys
        serverSocketChannel.configureBlocking(false);
        // 2. 创建 Selector
        Selector selector = Selector.open();
        // 3. 将 serverSocketChannel 注册到 selector 中， 连接事件 OP_ACCEPT
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("注册后的selectionKey\t" + selector.keys().size());
        // 4. 循环等待客户端连接
        while (true){
            if (selector.select(1000) == 0){
                // 没有事件发生
//                System.out.println("服务器等待了 1s 无连接");
                continue;
            }
            // > 0 表示已经获取到关注的事件 并拿到关注事件集合 发向获取通道
            // 拿到有事件发生的 selectionKeys
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            System.out.println("selectionKeys 有事件发生的" + selectionKeys.size());

            // 遍历 selectionKeys 利用迭代器
            Iterator<SelectionKey> keyIterator = selectionKeys.iterator();

            while (keyIterator.hasNext()){
                // 获取 selectionKey
                SelectionKey key = keyIterator.next();
                // 通道发生的事件
                if (key.isAcceptable()){
                    // accept 事件 有新的客户端连接 产生新的通道 分配给客户端
                    SocketChannel socketChannel = serverSocketChannel.accept();
                    socketChannel.configureBlocking(false); // 指定非阻塞
                    System.out.println("客户端连接成功:" + socketChannel.hashCode());
                    // 将 socketChannel
                    // 1. 注册至 selector
                    // 2. 关注事件 OP_READ
                    // 3. 关联一个 Buffer
                    socketChannel.register(selector, SelectionKey.OP_READ, ByteBuffer.allocate(1024));
                    System.out.println("客户端连接后的selectionKey\t" + selector.keys().size());

                }
                if (key.isReadable()){
                    // read 事件
                    // 拿到 socketChannel
                    SocketChannel channel = (SocketChannel) key.channel();
                    // 获取该 channel 关联的 Buffer
                    ByteBuffer buffer = (ByteBuffer) key.attachment();
                    channel.read(buffer);
                    System.out.println("from 客户端\t" + new String(buffer.array()));
                }
                // 手动从集合中删除当前 key
                keyIterator.remove();
            }
        }
    }
}
