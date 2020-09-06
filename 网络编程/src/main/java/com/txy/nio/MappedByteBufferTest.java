package com.txy.nio;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class MappedByteBufferTest {
    public static void main(String[] args) throws IOException {
        RandomAccessFile randomAccessFile = new RandomAccessFile("1.txt", "rw");

        FileChannel fileChannel = randomAccessFile.getChannel();

        // MappedByteBuffer 抽象类
        // FileChannel.MapMode.READ_WRITE：读写模式
        // 0：可以直接修改的起始位置
        // 5：映射到内存的大小 （下标从 0 开始）
        // java.nio.DirectByteBuffer 实现类
        MappedByteBuffer map = fileChannel.map(FileChannel.MapMode.READ_WRITE, 0, 5);

        // IDEA 内部查看没有修改，需要从外部查看 已经修改成功
        map.put(0, (byte)'9');
        map.put(3,(byte)'H');

        randomAccessFile.close();
    }
}
