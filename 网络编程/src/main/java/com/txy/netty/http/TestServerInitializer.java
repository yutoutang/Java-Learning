package com.txy.netty.http;

import io.netty.channel.*;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;

public class TestServerInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        // pipeline中的handler TestServerInitializer -> HttpServerCodec -> TestHttpServerHandler
        // 处理 http
        pipeline.addLast(new HttpServerCodec());
        // 自定义 handler
        pipeline.addLast(new TestHttpServerHandler());

        System.out.println("OK");
    }
}
