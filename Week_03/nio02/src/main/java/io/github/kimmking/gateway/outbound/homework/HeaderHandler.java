package io.github.kimmking.gateway.outbound.homework;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;

/**
 * @author : Luyz
 * @date : 2020/11/4 21:19
 */
public class HeaderHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        FullHttpRequest fullRequest = (FullHttpRequest) msg;
        fullRequest.headers().add("nio", "LuYazhao");
        ctx.fireChannelRead(msg);
    }
}
