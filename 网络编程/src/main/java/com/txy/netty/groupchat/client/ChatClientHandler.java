package com.txy.netty.groupchat.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import java.text.SimpleDateFormat;

public class ChatClientHandler extends SimpleChannelInboundHandler<String> {

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-hh HH:mm:ss");

//    // 客户端发送消息
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

    }

    // 客户端接受转发
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        System.out.println(msg.trim());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.channel().close();
    }
}
