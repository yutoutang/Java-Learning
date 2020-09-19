package com.txy.netty.codec;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class NettyClient {
    public static void main(String[] args) {
        // 客户端只需要一个 eventLoop
        EventLoopGroup eventExecutors = new NioEventLoopGroup();

        try {
            // 创建启动助手
            Bootstrap b = new Bootstrap();

            b.group(eventExecutors)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new NettyClientHandler());
                        }
                    });

            System.out.println("Client is ok....");
            // 客户端连接 channelFuture 涉及到 netty 的异步模型
            ChannelFuture channelFuture = b.connect("127.0.0.1", 6000).sync();

            channelFuture.channel().closeFuture().sync();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            eventExecutors.shutdownGracefully();
        }
    }
}
