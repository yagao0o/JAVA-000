# Week03 作业
1、按今天的课程要求，实现一个网关，
基础代码可以 fork:https://github.com/kimmking/JavaCourseCodes 02nio/nio02 文件夹下
实现以后，代码提交到 Github。

---
***作业项目完整代码: [nio02链接](./nio02),netty client的地方参考了很多百度，不知道写的对不对，请助教老师指点一下。***
---
---
## Day5 作业

**一、整合你上次作业的 httpclient/okhttp**  
> 将netty的server，把写死的字符串，替换为使用httpclient或者okhttp方式来调用后段的服务，返回后端的响应数据。

```JAVA
package io.github.kimmking.gateway.outbound.homework;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpUtil;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

import static io.netty.handler.codec.http.HttpHeaderNames.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaderValues.KEEP_ALIVE;
import static io.netty.handler.codec.http.HttpResponseStatus.NO_CONTENT;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * @author : Luyz
 * @date : 2020/11/4 15:47
 */
public class SimpleHandler {
    private String backendUrl;

    public SimpleHandler(String backendUrl) {
        this.backendUrl = backendUrl.endsWith("/") ? backendUrl.substring(0, backendUrl.length() - 1) : backendUrl;
    }

    public void handle(final FullHttpRequest fullRequest, final ChannelHandlerContext ctx) {
        final String url = this.backendUrl + fullRequest.uri();
        String result = doRequest(url);
        FullHttpResponse response = null;
        if (result == null || result.isEmpty()) {
            response = new DefaultFullHttpResponse(HTTP_1_1, NO_CONTENT);
        } else {
            response = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.wrappedBuffer(result.getBytes()));
            response.headers().set("Content-Type", "text/html;charset=utf-8");
            response.headers().setInt("Content-Length", response.content().readableBytes());
        }
        if (fullRequest != null) {
            if (!HttpUtil.isKeepAlive(fullRequest)) {
                ctx.write(response).addListener(ChannelFutureListener.CLOSE);
            } else {
                response.headers().set(CONNECTION, KEEP_ALIVE);
                ctx.write(response);
            }
            ctx.flush();
        }

    }

    private String doRequest(String url) {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(url);
        try {
            CloseableHttpResponse response = client.execute(httpGet);
            HttpEntity responseEntity = response.getEntity();
            if (responseEntity != null) {
                return EntityUtils.toString(responseEntity);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
```

**二、（选做）使用 netty 实现后端 http 访问(代替上一步骤);**  
> 将第一步作业的httpclient修改为使用netty调用

- SimpleNettyHandler
``` Java
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

```

- SimpleNettyChannelInboundHandler 处理client返回请求
``` Java
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
public class SimpleNettyChannelInboundHandler extends ChannelInboundHandlerAdapter {
    private ChannelHandlerContext originCtx;
    private FullHttpRequest originRequest;
    public SimpleNettyChannelInboundHandler(FullHttpRequest originRequest, ChannelHandlerContext originCtx) {
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
        URI url = new URI("/");
        //配置HttpRequest的请求数据和一些配置信息
        FullHttpRequest request = new DefaultFullHttpRequest(
                HttpVersion.HTTP_1_0, HttpMethod.GET, url.toASCIIString());
        request.headers()
                .set(HttpHeaderNames.CONTENT_TYPE, "text/plain;charset=UTF-8")
                //开启长连接
                .set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE)
                //设置传递请求内容的长度
                .set(HttpHeaderNames.CONTENT_LENGTH, request.content().readableBytes());
        //发送数据
        ctx.writeAndFlush(request);
    }
}

```

**基于netty client的版本，由于对于netty不是特别熟悉，感觉写的比较差，请助教老师指点一下。**

## Day6 作业
**一、实现过滤器**
> 实现一个 request 的过滤器 filter ，包括两个功能
> 1. 把请求的http头拿到，添加一个key为nio，value为自己名字的拼音的头
> 2. 请求后端服务时，将请求头添加到调用后端的请求中

- 新增一个过滤器 HeaderHandler ，对所有的请求，添加一个 HttpHeader，并在 HttpInboundInitializer 的原handler前，调用此 handler
``` Java
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
```


- 对于使用HttpClient的，修改部分如下，全部代码见[源文件链接](./nio02/src/main/java/io/github/kimmking/gateway/outbound/homework/SimpleHandler.java)：
``` Java
String result = doRequest(url, fullRequest.headers());

HttpGet httpGet = new HttpGet(url);
headers.forEach(i -> {
    httpGet.setHeader(i.getKey(), i.getValue());
});
```

- 对于使用netty，主要修改部分如下，全部代码见[源文件链接](./nio02/src/main/java/io/github/kimmking/gateway/outbound/homework/SimpleNettyChannelInboundHandler.java)：
``` Java
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
            .set(HttpHeaderNames.CONTENT_LENGTH, request.content().readableBytes())
    //发送数据
    ctx.writeAndFlush(request);
}
```

**二、（选做）实现路由**
> 添加一个类似负载均衡的功能，代理多个服务。有以下模式可选：
> 1. 随机取一个服务 Random
> 2. 依次轮询 RoundRibbon
> 3. 按权重分配 Weight
> - server01，20
> - server02，30
> - server03，50

- 将proxyServer修改为使用逗号分隔的目标服务器列表
- 在权重模式下，新增一个配置proxyServerWeights，为逗号分隔的目标服务器权重列表，数量要求与目标服务器一致
- 路由添加, 在HttpInboundHandler中，在构造函数生成handler时，调用router.route分配目标地址，代码片段如下，全部代码见[源文件链接](./nio02/src/main/java/io/github/kimmking/gateway/inbound/HttpInboundHandler.java)
```Java
private final List<String> proxyServer;
private SimpleNettyHandler handler;
private HttpEndpointRouter router;

public HttpInboundHandler(List<String> proxyServer) {
    this.proxyServer = proxyServer;
    this.router = new WeightEndpointRouter();
    handler = new SimpleNettyHandler(router.route(this.proxyServer));
}
```

- 随机模式，根据proxyServer的大小直接进行random，取对应配置，全部代码见[源文件链接](./nio02/src/main/java/io/github/kimmking/gateway/outbound/homework/router/RandomEndpointRouter.java)
```Java
package io.github.kimmking.gateway.outbound.homework.router;

import io.github.kimmking.gateway.router.HttpEndpointRouter;

import java.util.List;
import java.util.Random;

/**
 * @author : Luyz
 * @date : 2020/11/4 21:42
 */
public class RandomEndpointRouter implements HttpEndpointRouter {
    @Override
    public String route(List<String> endpoints) {
        Random random = new Random();
        return endpoints.get(random.nextInt(endpoints.size()));
    }
}
```

- 轮询模式，设置静态变量计数器i,每次+1，超过大小后从0开始，加synchronized保证线程安全（不知道对不对，请助教老师帮忙看下），全部代码见[源文件链接](./nio02/src/main/java/io/github/kimmking/gateway/outbound/homework/router/RoundRibbonEndpointRouter.java)
```
package io.github.kimmking.gateway.outbound.homework.router;

import io.github.kimmking.gateway.router.HttpEndpointRouter;

import java.util.List;

/**
 * @author : Luyz
 * @date : 2020/11/4 21:44
 */
public class RoundRibbonEndpointRouter implements HttpEndpointRouter {
    private static Integer i = 0;
    @Override
    public String route(List<String> endpoints) {
        return endpoints.get(getNext(endpoints.size()));
    }

    private synchronized int getNext(int size) {
        i += 1;
        if (i >= size) {
            i = 0;
        }
        return i;
    }
}
```


- 按权重分配，新增一个配置proxyServerWeights，为逗号分隔的目标服务器权重列表，数量要求与目标服务器一致，根据权重总量random一个值，再计算其掉落区间，全部代码见[源文件链接](./nio02/src/main/java/io/github/kimmking/gateway/outbound/homework/router/WeightEndpointRouter.java)
```
package io.github.kimmking.gateway.outbound.homework.router;

import io.github.kimmking.gateway.router.HttpEndpointRouter;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * @author : Luyz
 * @date : 2020/11/4 21:45
 */
public class WeightEndpointRouter implements HttpEndpointRouter {
    static List<Integer> weightList;
    public WeightEndpointRouter() {
        initWeightList();
    }
    /***
     * @param endpoints
     * @return
     */
    @Override
    public String route(List<String> endpoints) {
        int i = getNext();
        return endpoints.get(i);
    }

    private int getNext() {
        Integer total = weightList.stream().reduce(Integer::sum).get();
        Random random = new Random();
        int next = random.nextInt(total);
        for (int i = 0; i < weightList.size(); i++) {
            next = next - weightList.get(i);
            if (next < 0) {
                return i;
            }
        }
        return 0;
    }

    /***
     * 暂且默认设置为10，20，40，80的权重
     */
    private synchronized void initWeightList() {
        if (weightList == null || weightList.size() == 0) {
            String proxyServerWeights = System.getProperty("proxyServerWeights", "10,20,40,80");
            List<String> weightListString = Arrays.asList(proxyServerWeights.split(","));
            weightList = weightListString.stream().map(Integer::parseInt).collect(Collectors.toList());
        }
    }
}
```