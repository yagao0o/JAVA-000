
# Week09 作业
## Day17 作业
1. (选做)实现简单的Protocol Buffer/Thrift/gRPC(选任一个)远程调用demo。 

> TODO:

2. (选做)实现简单的WebService-Axis2/CXF远程调用demo。 

> 公司的一些比较老的工程都是使用Axis2和CXF的

3. (必做)改造自定义RPC的程序，提交到github:  
    项目整体代码[完整项目链接](./rpc01)

    1. 尝试将服务端写死查找接口实现类变成泛型和反射 
    
    课上老师写的差不多了，但现场翻车的那部分，课上说是由于使用了基础类型int，导致反射失败，作业代码里面将 OrderService 和 UserService中的基础类型修改为了包装类型，直接使用 getMethod 获取了方法。不过参数部分用Stream的map映射后类型不能正常转换，请指导一下。

    ```Java
        private Method resolveMethodFromClass(Class<?> klass, String methodName, Object[] parameters) throws NoSuchMethodException {
            Class<?>[] classes = new Class[parameters.length];
            for (int i = 0; i < parameters.length; i++) {
                classes[i] = parameters[i].getClass();
            }
            // 想转成stream，但用stream map后类型没法正常转换
            // Class<?>[] classes2 = Arrays.stream(parameters).map(Object::getClass).toArray();
            return klass.getMethod(methodName, classes);
        }
    ```

    2. 尝试将客户端动态代理改成AOP，添加异常处理

    使用byte-buddy字节码增强来处理，其中拦截器中url怎么传过去没想明白，如果两个请求对应两个url，很可能有问题。
    
    ```Java
    public final class RpcfxByteBuddy {
        static {
            ParserConfig.getGlobalInstance().addAccept("io.kimmking");
        }

        public static <T> T create(final Class<T> serviceClass, final String url) {
            // 0. 替换动态代理 -> AOP{字节码增强}
            try {
                ByteBuddyInterceptor.url = url;
                return new ByteBuddy()
                        .subclass(serviceClass)
                        .method(ElementMatchers.isDeclaredBy(serviceClass))
                        .intercept(MethodDelegation.to(ByteBuddyInterceptor.class))
                        .make()
                        .load(ClassLoader.getSystemClassLoader())
                        .getLoaded()
                        .newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    /**
    * 拦截器
    * @author : Luyz
    * @date : 2020/12/16 14:49
    */
    public class ByteBuddyInterceptor {
        public static String url = "";

        @RuntimeType
        public static Object intercept(@AllArguments Object[] allArguments, @Origin Method method) throws Exception {
            return RequestHelper.doRequest(allArguments, method.getDeclaringClass().getName(), method.getName(), url);
        }
    }

    /** RequestHelper 是我自定义的辅助类，处理请求部分，动态代理和字节码增强同时可以用到**/
    public class RequestHelper {
        public static final MediaType JSONTYPE = MediaType.get("application/json; charset=utf-8");
        public static Object doRequest(Object[] params, String className, String methodName,String url) throws IOException,
                RpcfxException {
            RpcfxRequest request = new RpcfxRequest();
            request.setServiceClass(className);
            request.setMethod(methodName);
            request.setParams(params);

            RpcfxResponse response = post(request, url);
            // 这里判断response.status，处理异常
            // 考虑封装一个全局的RpcfxException
            if (!response.isStatus()) {
                throw response.getException();
            }
            return JSON.parse(response.getResult().toString());
        }

        private static RpcfxResponse post(RpcfxRequest req, String url) throws IOException {
            String reqJson = JSON.toJSONString(req);
            System.out.println("req json: "+reqJson);

            // 1.可以复用client
            // 2.尝试使用httpclient或者netty client
            OkHttpClient client = new OkHttpClient();
            final Request request = new Request.Builder()
                    .url(url)
                    .post(RequestBody.create(JSONTYPE, reqJson))
                    .build();
            String respJson = client.newCall(request).execute().body().string();
            System.out.println("resp json: "+respJson);
            return JSON.parseObject(respJson, RpcfxResponse.class);
        }
    }
    ```
    3. 尝试使用Netty+HTTP作为client端传输方式

    添加了一个NettyClient，以及SimpleNettyChannelOutboundHandler来处理请求，替换post方法中okhttp的部分
    ```JAVA
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
   
   private static RpcfxResponse post(RpcfxRequest req, String url) throws IOException {
        String reqJson = JSON.toJSONString(req);
        String respJson = "";
        // netty 方式
        NettyClient client = new NettyClient(url);
        try {
            respJson = client.sendMessage(reqJson);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return JSON.parseObject(respJson, RpcfxResponse.class);
    }
    ```

4. (选做☆☆)升级自定义RPC的程序:
    1. 尝试使用压测并分析优化RPC性能 
    2. 尝试使用Netty+TCP作为两端传输方式 
    3. 尝试自定义二进制序列化 
    4. 尝试压测改进后的RPC并分析优化，有问题欢迎群里讨论 
    5. 尝试将fastjson改成xstream 
    6. 尝试使用字节码生成方式代替服务端反射【这个题目在3.3中已经做了】


## Day18

1. (选做)按课程第二部分练习各个技术点的应用。
2. (选做)按dubbo-samples项目的各个demo学习具体功能使用。
3. (必做)结合dubbo+hmily，实现一个TCC外汇交易处理，代码提交到github:
    - 用户A的美元账户和人民币账户都在A库，使用1美元兑换7人民币;
    - 用户B的美元账户和人民币账户都在B库，使用7人民币兑换1美元;
    - 设计账户表，冻结资产表，实现上述两个本地事务的分布式事务。
4. (挑战☆☆)尝试扩展Dubbo
    - 基于上次作业的自定义序列化，实现Dubbo的序列化扩展;
    - 基于上次作业的自定义RPC，实现Dubbo的RPC扩展;
    - 在Dubbo的filter机制上，实现REST权限控制，可参考dubbox;
    - 实现一个自定义Dubbo的Cluster/Loadbalance扩展，如果一分钟内调用某个服务/ 提供者超过10次，则拒绝提供服务直到下一分钟;
    - 整合Dubbo+Sentinel，实现限流功能; 6)整合Dubbo与Skywalking，实现全链路性能监控。