package io.kimmking.rpcfx.client.helper;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

public class NettyClient {
    String backendUrl;
    /**
     *
     */
    EventLoopGroup group;
    /**
     * 客户端通道
     */
    private Channel clientChannel;
    SimpleNettyChannelOutboundHandler handler;

    public NettyClient(String backendUrl) {
        this.backendUrl = backendUrl;
        initClient();
    }

    public void initClient() {
        group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        handler = new SimpleNettyChannelOutboundHandler(backendUrl);
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
                            channel.pipeline().addLast(handler);
                        }
                    });
            ChannelFuture channelFuture = bootstrap.connect();//.sync();
//            channelFuture.channel().closeFuture().sync();
            //注册连接事件
            channelFuture.addListener((ChannelFutureListener)future -> {
                //如果连接成功
                if (future.isSuccess()) {
                    clientChannel = channelFuture.channel();
                }
                //如果连接失败，尝试重新连接
                else{
                    future.channel().close();
                    bootstrap.connect(uri.getHost(), uri.getPort());
                }
            });
            //注册关闭事件
            channelFuture.channel().closeFuture().addListener(cfl -> {
                close();
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void close() {
        //关闭客户端套接字
        if(clientChannel!=null){
            clientChannel.close();
        }
        //关闭客户端线程组
        if (group != null) {
            group.shutdownGracefully();
        }
    }

    public String sendMessage(String message) throws InterruptedException, URISyntaxException {
        while (clientChannel == null) {
            try {
                TimeUnit.MILLISECONDS.sleep(1);
                //logger.error("等待ChannelHandlerContext实例化");
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.out.println("等待channel准备中出错");
            }
        }
        ChannelPromise promise = handler.sendMessage(message);
        promise.await(3, TimeUnit.SECONDS);
        close();
        return handler.getRespJson();
    }

}
