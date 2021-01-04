package io.github.kimmking.gateway.outbound.homework;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;

import java.net.URI;

import static io.netty.handler.codec.http.HttpHeaderNames.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaderValues.KEEP_ALIVE;
import static io.netty.handler.codec.http.HttpResponseStatus.NO_CONTENT;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * @author : Luyz
 * @date : 2020/11/4 16:55
 */
public class SimpleNettyChannelOutboundHandler extends ChannelInboundHandlerAdapter {
    private ChannelHandlerContext originCtx;
    private FullHttpRequest originRequest;
    public SimpleNettyChannelOutboundHandler(FullHttpRequest originRequest, ChannelHandlerContext originCtx) {
        this.originCtx = originCtx;
        this.originRequest = originRequest;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        FullHttpResponse response = (FullHttpResponse) msg;
        ByteBuf content = response.content();
        String result = content.toString(CharsetUtil.UTF_8);
        FullHttpResponse finalResponse = null;
        if (result == null || result.isEmpty()) {
            finalResponse = new DefaultFullHttpResponse(HTTP_1_1, NO_CONTENT);
        } else {
            finalResponse = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.wrappedBuffer(result.getBytes()));
            finalResponse.headers().set("Content-Type", "text/html;charset=utf-8");
            finalResponse.headers().setInt("Content-Length", finalResponse.content().readableBytes());
        }
        if (originRequest != null) {
            if (!HttpUtil.isKeepAlive(originRequest)) {
                originCtx.write(finalResponse).addListener(ChannelFutureListener.CLOSE);
            } else {
                finalResponse.headers().set(CONNECTION, KEEP_ALIVE);
                originCtx.write(finalResponse);
            }
            originCtx.flush();
        }
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        URI url = new URI(originRequest.uri());
        //配置HttpRequest的请求数据和一些配置信息
        FullHttpRequest request = new DefaultFullHttpRequest(
                HttpVersion.HTTP_1_0, HttpMethod.GET, url.toASCIIString());
        request.headers()
                .setAll(originRequest.headers())
//                .set(HttpHeaderNames.CONTENT_TYPE, "text/plain;charset=UTF-8")
                //开启长连接
//                .set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE)
                //设置传递请求内容的长度
                .set(HttpHeaderNames.CONTENT_LENGTH, request.content().readableBytes());
        //发送数据
        ctx.writeAndFlush(request);
    }
}
