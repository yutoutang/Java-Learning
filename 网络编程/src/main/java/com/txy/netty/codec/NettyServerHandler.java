package com.txy.netty.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.util.CharsetUtil;

public class NettyServerHandler extends ChannelInboundHandlerAdapter {

    // 读取客户端发送的消息
    // ChannelHandlerContext：channel pipeline 地址
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("server ctx:" + ctx);
        System.out.println("server thread" + Thread.currentThread().getName());
        Channel channel = ctx.channel();
        ChannelPipeline pipeline = ctx.pipeline(); // 本质是双向链表
        // 将 msg 转 buff(netty)
        ByteBuf buf = (ByteBuf)msg;
        System.out.println("client: "+ ctx.channel().remoteAddress() +"msg:" + buf.toString(CharsetUtil.UTF_8));
    }

    // 数据读取完毕
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        // write + flush 将数据写入到缓冲，并刷新，对发送数据进行编码
        ctx.writeAndFlush(Unpooled.copiedBuffer("hello client", CharsetUtil.UTF_8));
    }

    // 处理异常 关闭通道
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.channel().close();
    }
}
