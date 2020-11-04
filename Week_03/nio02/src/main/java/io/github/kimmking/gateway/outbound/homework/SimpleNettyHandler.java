package io.github.kimmking.gateway.outbound.homework;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;

import java.net.InetSocketAddress;
import java.net.URI;

/**
 * @author : Luyz
 * @date : 2020/11/4 16:39
 */
public class SimpleNettyHandler {
    private final String backendUrl;

    public SimpleNettyHandler(String backendUrl) {
        this.backendUrl = backendUrl.endsWith("/") ? backendUrl.substring(0, backendUrl.length() - 1) : backendUrl;
    }

    public void handle(final FullHttpRequest fullRequest, final ChannelHandlerContext ctx) {
        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        try {
            URI uri = URI.create(backendUrl);
            bootstrap.group(group)
                    .remoteAddress(new InetSocketAddress(uri.getHost(), uri.getPort()))
                    //长连接
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<Channel>() {
                        @Override
                        protected void initChannel(Channel channel) throws Exception {

                            //包含编码器和解码器
                            channel.pipeline().addLast(new HttpClientCodec());
                            //聚合
                            channel.pipeline().addLast(new HttpObjectAggregator(1024 * 10 * 1024));
                            //解压
                            channel.pipeline().addLast(new HttpContentDecompressor());
                            channel.pipeline().addLast(new SimpleNettyChannelInboundHandler(fullRequest, ctx));
                        }
                    });
            ChannelFuture channelFuture = bootstrap.connect().sync();
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }
}
