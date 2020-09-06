package com.txy.netty.http;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.spdy.SpdyHeaders;
import io.netty.util.CharsetUtil;

import java.net.URI;

public class TestHttpServerHandler extends SimpleChannelInboundHandler<HttpObject> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
        // 浏览器发送内容 包装 http
        if (msg instanceof HttpRequest){

            HttpRequest request = (HttpRequest)msg;

            URI uri = new URI(request.uri());

            if ("/favicon.ico".equals(uri.getPath())){
                System.out.println("请求 favicon.ico 不做响应");
                return;
            }


            System.out.println(ctx.channel().remoteAddress());
            System.out.println(msg.getClass());

            ByteBuf buf = Unpooled.copiedBuffer("hello 你好 6668", CharsetUtil.UTF_8);

            DefaultHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, buf);

            // text/plain;charset=UTF-8 不乱码
            response.headers().set(HttpHeaderNames.CONTENT_TYPE,"text/plain;charset=UTF-8");
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, buf.readableBytes());

            ctx.writeAndFlush(response);
        }
    }
}
