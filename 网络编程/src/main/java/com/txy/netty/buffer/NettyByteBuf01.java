package com.txy.netty.buffer;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class NettyByteBuf01 {
    public static void main(String[] args) {
        // 创建一个 ByteBuf
        // 1. 创建对象，该对象包含一个数组 arr，是一个 byte[10]
        // 2. netty 的 buf 不需要使用 flip 进行反转 底层了维护 readerIndex （下一个读的位置） 和 writeIndex （下一个写入的位置 i+1）
        // 3. buf 的三个区域 readerIndex writeIndex capacity
        // 0 ---- readerIndex 可读的区域
        // readerIndex --- writeIndex 可写的区域
        // writeIndex --- capacity 容量
        ByteBuf buf = Unpooled.buffer(10);

        for (int i = 0; i < 10; i++) {
            buf.writeByte(i);
        }

        for (int i = 0; i < buf.capacity(); i++) {
            System.out.println(buf.readByte());
        }
    }
}
