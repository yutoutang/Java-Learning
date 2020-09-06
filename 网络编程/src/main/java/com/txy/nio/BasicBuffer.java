package com.txy.nio;

import java.nio.IntBuffer;
import java.nio.channels.Channel;
import java.nio.channels.FileChannel;

public class BasicBuffer {

    public static void main(String[] args) {
        // 举例说明buffer的使用
        // 创建一个可以存放5个int的Buffer
        IntBuffer intBuffer = IntBuffer.allocate(5);

        // 存buffer
        for (int i = 0; i < intBuffer.capacity(); i++) {
            intBuffer.put(i * 2);
        }
        // 取buffer，在取之前需要读写切换(!)
        intBuffer.flip();
        intBuffer.position(1);
        intBuffer.limit(3);
        while (intBuffer.hasRemaining()){
            System.out.println(intBuffer.get());
        }

    }

}
