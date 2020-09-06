package com.txy.nio;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class NIOFileChannelR {
    public static void main(String[] args) throws IOException {
        // 1. 创建文件输入流
        File file = new File("1.txt");
        FileInputStream fis = new FileInputStream(file);

        // 2. 创建输入流通道
        FileChannel channel = fis.getChannel();
        // 3. 创建缓冲区
        ByteBuffer byteBuffer = ByteBuffer.allocate((int) file.length());
        // 4. 读
        channel.read(byteBuffer);
        // 5. 打印
        System.out.println(new String(byteBuffer.array()));

        fis.close();
    }
}
