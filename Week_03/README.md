# Week03 作业
1、按今天的课程要求，实现一个网关，
基础代码可以 fork:https://github.com/kimmking/JavaCourseCodes 02nio/nio02 文件夹下
实现以后，代码提交到 Github。
## Day5 作业
**一、整合你上次作业的 httpclient/okhttp**  
> 将netty的server，把写死的字符串，替换为使用httpclient或者okhttp方式来调用后段的服务，返回后端的响应数据。

**二、（选做）使用 netty 实现后端 http 访问(代替上一步骤);**  
> 将第一步作业的httpclient修改为使用netty调用

## Day6 作业
**一、实现过滤器**
> 实现一个 request 的过滤器 filter ，包括两个功能
> 1. 把请求的http头拿到，添加一个key为nio，value为自己名字的拼音的头
> 2. 请求后端服务时，将请求头添加到调用后端的请求中

**二、（选做）实现路由**
> 添加一个类似负载均衡的功能，代理多个服务。有以下模式可选：
> 1. 随机取一个服务
> 2. 依次轮询
> 3. 按权重分配
