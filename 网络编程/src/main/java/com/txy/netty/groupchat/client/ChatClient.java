package com.txy.netty.groupchat.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;

import java.util.Scanner;

public class ChatClient {
    public static void main(String[] args) {
        EventLoopGroup workgroup = new NioEventLoopGroup();

        try {
            Bootstrap b = new Bootstrap();
            b.group(workgroup).channel(NioSocketChannel.class).handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ChannelPipeline pipeline = ch.pipeline();
                    // 再使用 SimpleChannelInboundHandler<String> 作为 handler 的父类 一定要保证 客户端和服务端
                    // 有相同的 String 的编解码方法 否则，channel.writeAndFlush 方法无法正确刷新 发送、接受流
                    pipeline.addLast("decoder", new StringDecoder());
                    pipeline.addLast("encoder", new StringEncoder());
                    pipeline.addLast(new ChatClientHandler());
                }
            });

            ChannelFuture client_future = b.connect("127.0.0.1", 4000).sync();
            System.out.println(client_future.channel().localAddress() + "客户端已连接！");

            Scanner scanner = new Scanner(System.in);
            while (scanner.hasNext()){
                String s = scanner.nextLine();
                client_future.channel().writeAndFlush(s + "\r\n");
            }

            client_future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            workgroup.shutdownGracefully();
        }
    }
}
