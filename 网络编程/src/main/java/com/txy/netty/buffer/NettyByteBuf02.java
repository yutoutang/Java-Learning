package com.txy.netty.buffer;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class NettyByteBuf02 {
    public static void main(String[] args) {
        // ByteBuf api 测试
        ByteBuf buf = Unpooled.copiedBuffer("hello world", StandardCharsets.UTF_8);

        if (buf.hasArray()){
            byte[] content = buf.array();
            System.out.println(buf.capacity()); // 33
            String s = new String(content, StandardCharsets.UTF_8);
            System.out.println(s); // hello world                       后面 ASCII 为0的字符也输出了
            System.out.println(buf.getCharSequence(0, buf.writerIndex(), StandardCharsets.UTF_8)); // 推荐这种方式去读 hello world
            System.out.println("bytebuf=" + buf);
            System.out.println(buf.arrayOffset());  // 0
            System.out.println(buf.readerIndex());  // 0
            System.out.println(buf.writerIndex());  // 11
            System.out.println(buf.readableBytes());  // 可读的字节数 = 11
        }
    }
}
