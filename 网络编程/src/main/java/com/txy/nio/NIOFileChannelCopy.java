package com.txy.nio;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * 利用 Channel 和 Buffer 完成文件拷贝
 */

public class NIOFileChannelCopy {
    public static void main(String[] args) throws IOException {
        FileInputStream fis = new FileInputStream("1.txt");
        FileChannel fisChannel = fis.getChannel();

        FileOutputStream fos = new FileOutputStream("2.txt");
        FileChannel fosChannel = fos.getChannel();

        ByteBuffer byteBuffer = ByteBuffer.allocate(512);

        while (true){
            byteBuffer.clear();
            int read = fisChannel.read(byteBuffer);
            System.out.println("read" + read);
            if (read == -1){
                break;
            }
            byteBuffer.flip();
            fosChannel.write(byteBuffer);
        }

        fis.close();
        fos.close();

    }
}
