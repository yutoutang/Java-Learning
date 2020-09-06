package com.txy.nio;

import java.io.*;
import java.nio.channels.FileChannel;

public class NIOFileChannel_Tran {

    public static void main(String[] args) throws IOException {
        FileInputStream fis = new FileInputStream("test.jpg");
        FileChannel fisChannel = fis.getChannel();

        FileOutputStream fos = new FileOutputStream("test1.jpg");
        FileChannel fosChannel = fos.getChannel();

        fosChannel.transferFrom(fisChannel, 0, fisChannel.size());

        fis.close();
        fos.close();
    }
}
