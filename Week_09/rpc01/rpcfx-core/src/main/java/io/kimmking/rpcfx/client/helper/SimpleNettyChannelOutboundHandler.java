package io.kimmking.rpcfx.client.helper;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

public class SimpleNettyChannelOutboundHandler extends ChannelInboundHandlerAdapter {
    private String respJson;
    private final String url;
    private ChannelPromise promise;
    private ChannelHandlerContext ctx;

    public SimpleNettyChannelOutboundHandler(String url) {
        this.url = url;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        FullHttpResponse response = (FullHttpResponse) msg;
        ByteBuf content = response.content();
        respJson = content.toString(CharsetUtil.UTF_8);
        promise.setSuccess();
    }

    public String getRespJson() {
        return respJson;
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    public synchronized ChannelPromise sendMessage(String message) throws URISyntaxException {
        URI uri = new URI(url);
        //配置HttpRequest的请求数据和一些配置信息
        byte[] bytes = message.getBytes(CharsetUtil.UTF_8);
        ByteBuf buf = Unpooled.wrappedBuffer(bytes);
        FullHttpRequest request = new DefaultFullHttpRequest(
                HttpVersion.HTTP_1_0, HttpMethod.POST, uri.toASCIIString(), buf);
        request.headers()
                .set("Content-Type", "application/json; charset=utf-8")
//                .set(HttpHeaderNames.CONTENT_TYPE, "text/plain;charset=UTF-8")
                //开启长连接
//                .set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE)
                //设置传递请求内容的长度
                .set(HttpHeaderNames.CONTENT_LENGTH, request.content().readableBytes());
        //发送数据
        while (ctx == null) {
            try {
                TimeUnit.MILLISECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.out.println("等待ChannelHandlerContext实例化过程中出错");
            }
        }
        promise = ctx.newPromise();
        ctx.writeAndFlush(request);
        return promise;
    }
}
