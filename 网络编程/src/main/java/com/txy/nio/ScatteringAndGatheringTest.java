package com.txy.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.AbstractSelector;
import java.util.Arrays;

public class ScatteringAndGatheringTest {

    public static void main(String[] args) throws IOException {

        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();

        InetSocketAddress inetSocketAddress = new InetSocketAddress(7000);
        // 绑定端口 并启动
        serverSocketChannel.socket().bind(inetSocketAddress);

        // 创建 Buffer 数组
        ByteBuffer[] byteBuffers = new ByteBuffer[2];

        byteBuffers[0] = ByteBuffer.allocate(5);
        byteBuffers[1] = ByteBuffer.allocate(3);

        // 等待客户端连接
        SocketChannel socketChannel = serverSocketChannel.accept();

        int messageLen = 8;

        while (true){
            int byteRead = 0;
            while (byteRead < messageLen){
                long read = socketChannel.read(byteBuffers);
                byteRead += read;  // 累计读取个数
                System.out.println("byteRead=" + byteRead);
                // 使用流打印 查看当前 buffer 的pos和limit
                Arrays.stream(byteBuffers).
                        map(buffer -> "position=" + buffer.position() + "\tlimit=" + buffer.limit()).
                        forEach(System.out::println);
            }

            // 将所有 buffer flip
            Arrays.asList(byteBuffers).forEach(ByteBuffer::flip);

            // 将数据读出 显示回客户端
            long byteWrite = 0;
            while (byteWrite < messageLen){
                long write = socketChannel.write(byteBuffers);
                byteWrite += write;
            }

            Arrays.asList(byteBuffers).forEach(ByteBuffer::clear);
            System.out.println("\n");
        }


    }

}
