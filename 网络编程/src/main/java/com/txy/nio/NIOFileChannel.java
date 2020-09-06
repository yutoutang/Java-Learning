package com.txy.nio;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class NIOFileChannel {

    public static void main(String[] args) throws IOException {
        String str = "hello world";
        // 写操作
        // 1. 创建输出流
        FileOutputStream fos = new FileOutputStream("1.txt");
        // 2. 创建 FileChannel 包装初始的IO流 fileChannel 的真实类型是 FileChannelImpl
        FileChannel fileChannel = fos.getChannel();
        // 3. 创建缓冲区
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        // 4. 存入数据至缓冲区 以字节形式
        byteBuffer.put(str.getBytes());
        // 5. buffer写入channel 需要进行 flip
        byteBuffer.flip();
        fileChannel.write(byteBuffer);
        // 6. 关闭相关流
        fos.close();
    }

}
