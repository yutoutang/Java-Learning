package com.txy.netty.codec;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class NettyServer {
    public static void main(String[] args) throws InterruptedException {

        // 创建 BossGroup 和 WorkGroup
        // 8 个线程 循环分配
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workGroup = new NioEventLoopGroup();

        try {
            // 创建服务器端启动的对象，配置启动参数
            ServerBootstrap b = new ServerBootstrap();

            b.group(bossGroup, workGroup) // 设置两个线程组
                    .channel(NioServerSocketChannel.class) // 设置服务器通道的实现类
                    .option(ChannelOption.SO_BACKLOG, 128) // 设置线程队列等待连接个数
                    .childOption(ChannelOption.SO_KEEPALIVE, true)  // 设置保持活动连接状态 心跳
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        // 创建一个通道初始化对象 匿名对象 给 pipeline设置 handler
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new NettyServerHandler());
                        }
                    }); // workGroup 的对应的 Event 管道处理器

            System.out.println("Server is ready....");

            // 绑定端口 并且同步处理
            ChannelFuture cf = b.bind(6000).sync();

            // 对关闭通道进行监听
            cf.channel().closeFuture().sync();
        }finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }
}
