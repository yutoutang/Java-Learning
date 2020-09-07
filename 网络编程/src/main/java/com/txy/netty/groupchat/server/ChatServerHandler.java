package com.txy.netty.groupchat.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

public class ChatServerHandler extends SimpleChannelInboundHandler<String> {

    // 利用 channelGroup 管理聊天室中所有的 用户
    // GlobalEventExecutor.INSTANCE 全局事件执行器 是一个单例
    private static ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-hh HH:mm:ss");

    // 利用一个 ChannelGroup 存入 channel 发送信息
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        channelGroup.writeAndFlush(ctx.channel().remoteAddress() + "->" + sdf.format(new Date()) + "\t加入群聊");
        channelGroup.add(ctx.channel());
    }

    // 上线
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println(ctx.channel().remoteAddress() + "->" + sdf.format(new Date())  +  "\t 上线了");
    }

    // 离线
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println(ctx.channel().remoteAddress() + "->" + sdf.format(new Date()) + "\t 离线了");
    }

    // 断开连接，将客户端离线信息发送给在线客户 自动 remove
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        channelGroup.writeAndFlush(ctx.channel().remoteAddress() + "->" + sdf.format(new Date())  + "\t退出群聊");
        System.out.println(channelGroup.size());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        Channel channel = ctx.channel();
        // 读取客户端的消息
        channelGroup.forEach(ch -> {
            if (ch == channel){
                ch.writeAndFlush("[自己]" + "->" + sdf.format(new Date()) + ":说\t" + msg);
            }else {
                ch.writeAndFlush("[客户端]" + "->" +  sdf.format(new Date()) +":说\t" + msg);
            }
        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.channel().close();
        System.out.println(ctx.channel().remoteAddress() + "\t 下线了");
    }
}
