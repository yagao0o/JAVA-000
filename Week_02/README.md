# Week02 作业
## Day3 作业
**1、使用 GCLogAnalysis.java 自己演练一遍串行/并行/CMS/G1的案例。写一段对于不同 GC 的总结。**
- 串行
    - 使用128M内存，命令：``java -XX:+UseSerialGC -Xms128m -Xmx128m -Xloggc:serialgc-128.log -XX:+PrintGCDetails -XX:+PrintGCDateStamps GCLogAnalysis``  
        程序运行结果：OOM  
        输出见日志文件：[serialgc-128.log](./gclog/serialgc-128.log)
    - 使用512M内存，命令：``java -XX:+UseSerialGC -Xms512m -Xmx512m -Xloggc:serialgc-512.log -XX:+PrintGCDetails -XX:+PrintGCDateStamps GCLogAnalysis``  
        程序输出：
        ```
        正在执行...
        执行结束!共生成对象次数:9123
        ``` 
        GC输出见日志文件：[serialgc-512.log](./gclog/serialgc-512.log)
    - 使用4G内存，命令：``java -XX:+UseSerialGC -Xms4g -Xmx4g -Xloggc:serialgc-4096.log -XX:+PrintGCDetails -XX:+PrintGCDateStamps GCLogAnalysis``  
        程序输出：
        ```
        正在执行...
        执行结束!共生成对象次数:8580
        ``` 
        GC输出见日志文件：[serialgc-512.log](./gclog/serialgc-4096.log)
- 并行
    - 使用512M内存，命令：``java -Xms512m -Xmx512m -Xloggc:parallelgc-512.log -XX:+PrintGCDetails -XX:+PrintGCDateStamps GCLogAnalysis``  
        程序输出：
        ```
        正在执行...
        执行结束!共生成对象次数:8808
        ``` 
        输出见日志文件：[parallelgc-512.log](./gclog/parallelgc-512.log)
    - 使用128M内存，命令：``java -Xms128m -Xmx128m -Xloggc:parallelgc-128.log -XX:+PrintGCDetails -XX:+PrintGCDateStamps GCLogAnalysis``  
        程序运行结果：OOM  
        输出见日志文件：[parallelgc-128.log](./gclog/parallelgc-128.log)
    - 使用4G内存，命令：``java -Xms4g -Xmx4g -Xloggc:parallelgc-4096.log -XX:+PrintGCDetails -XX:+PrintGCDateStamps GCLogAnalysis``  
        程序输出：
        ```
        正在执行...
        执行结束!共生成对象次数:8669
        ``` 
        输出见日志文件：[parallelgc-4096.log](./gclog/parallelgc-4096.log)
- CMS
    - 使用512M内存，命令：``java -XX:+UseConcMarkSweepGC -Xms512m -Xmx512m -Xloggc:cmsgc-512.log -XX:+PrintGCDetails -XX:+PrintGCDateStamps GCLogAnalysis``  
        程序输出：
        ```
        正在执行...
        执行结束!共生成对象次数:9598
        ``` 
        输出见日志文件：[cmsgc-512.log](./gclog/cmsgc-512.log)
    - 使用128M内存，命令：``java -XX:+UseConcMarkSweepGC -Xms128m -Xmx128m -Xloggc:cmsgc-128.log -XX:+PrintGCDetails -XX:+PrintGCDateStamps GCLogAnalysis``  
        程序运行结果：OOM  
        输出见日志文件：[cmsgc-128.log](./gclog/cmsgc-128.log)
    - 使用4G内存，命令：``java -XX:+UseConcMarkSweepGC -Xms4g -Xmx4g -Xloggc:cmsgc-4096.log -XX:+PrintGCDetails -XX:+PrintGCDateStamps GCLogAnalysis``  
        程序输出：
        ```
        正在执行...
        执行结束!共生成对象次数:10205
        ``` 
        输出见日志文件：[cmsgc-4096.log](./gclog/cmsgc-4096.log)
- G1
    - 使用512M内存，命令：``java -XX:+UseG1GC -Xms512m -Xmx512m -Xloggc:g1gc-512.log -XX:+PrintGCDateStamps GCLogAnalysis``  
        程序输出：
        ```
        正在执行...
        执行结束!共生成对象次数:10944
        ``` 
        输出见日志文件：[g1gc-512.log](./gclog/g1gc-512.log)
    - 使用128M内存，命令：``java -XX:+UseG1GC -Xms128m -Xmx128m -Xloggc:g1gc-128.log -XX:+PrintGCDateStamps GCLogAnalysis``  
        程序运行结果：OOM  
        输出见日志文件：[g1gc-128.log](./gclog/g1gc-128.log)
    - 使用4G内存，命令：``java -XX:+UseG1GC -Xms4g -Xmx4g -Xloggc:g1gc-4096.log -XX:+PrintGCDateStamps GCLogAnalysis``  
        程序输出：
        ```
        正在执行...
        执行结束!共生成对象次数:10944
        ``` 
        输出见日志文件：[g1gc-4096.log](./gclog/g1gc-4096.log)
> 总结  
> 1. 对于4类GC来说，设置128M的最大堆内存总会造成OOM，Old区写满后Full GC不能回收可用空间导致OOM出现，因此对于不同的应用，有其对应的堆内存要求，当最大堆内存低于要求时，会因GC导致OOM。同时堆内存小时，Old区容易被写满，会造成频繁的Full GC，无论使用哪种GC都会频繁的STW，影响业务性能。 
> 2. 反之来说，设置4G的堆内存时，可明显减少GC次数，Young GC均大量减少，并且均未产生Full GC，因此，在系统资源足够的情况下，可以考虑尽量多指定堆内存大小。
> 3. 对于设置512M堆内存的情况横向比对，串行GC，随着时间后推，每次Young GC时间变长，最高到56ms，最低20ms，但执行过程中未出现Full GC；对于并行GC来说，Young GC最低2ms，最高23ms，Full GC出现了8次，每次消耗时间逐渐增多，由37ms到46ms；CMS GC，Young GC最短时间10ms，最长时间34ms, Full GC被分为多个部分，其中影响业务的STW阶段：CMS Initial Mark、CMS Final Remark，两者执行时长均在1ms左右，其他阶段不影响业务，整体STW的时间非常短；G1 GC，Young GC大部分在5ms以内，少量在15ms左右，Full GC阶段，initial-mark、remark、cleanup三阶段需要STW，每次Full GC这三阶段总时间在2ms左右。
> 4. 整体结果来看G1 GC > CMS GC > 并行GC = 串行GC，并行GC和串行GC在此demo中差别不大


**2、使用压测工具(wrk或sb)，演练gateway-server-0.0.1-SNAPSHOT.jar 示例。写一段对于不同 GC 的总结**
压测命令：``wrk -t8 -c40 -d60s http://localhost:8088/api/hello``
- 串行GC
    - 512M内存，命令：``java -XX:+UseSerialGC -Xmx512m -Xms512m -jar gateway-server-0.0.1-SNAPSHOT.jar``  
    压测结果：
    ```
    Running 1m test @ http://localhost:8088/api/hello
    8 threads and 40 connections
    Thread Stats   Avg      Stdev     Max   +/- Stdev
        Latency     7.53ms   30.77ms 280.74ms   94.83%
        Req/Sec     6.47k     1.35k   10.09k    90.09%
    2669901 requests in 1.00m, 318.76MB read
    Requests/sec:  44427.82
    Transfer/sec:      5.30MB
    ```
    - 2G内存，命令：``java -XX:+UseSerialGC -Xmx2g -Xms2g -jar gateway-server-0.0.1-SNAPSHOT.jar``  
    压测结果：
    ```
    Running 1m test @ http://localhost:8088/api/hello
    8 threads and 40 connections
    Thread Stats   Avg      Stdev     Max   +/- Stdev
        Latency     8.56ms   42.18ms 546.44ms   95.77%
        Req/Sec     6.68k     1.31k   10.03k    88.90%
    2658411 requests in 1.00m, 317.39MB read
    Requests/sec:  44256.38
    Transfer/sec:      5.28MB
    ```
- 并行GC
    - 512M内存，命令：``java -jar -Xmx512m -Xms512m gateway-server-0.0.1-SNAPSHOT.jar``  
    压测结果：
    ```
    Running 1m test @ http://localhost:8088/api/hello
    8 threads and 40 connections
    Thread Stats   Avg      Stdev     Max   +/- Stdev
        Latency     4.31ms   20.08ms 216.96ms   96.33%
        Req/Sec     6.67k     1.39k   18.26k    88.69%
    2452977 requests in 1.00m, 292.86MB read
    Requests/sec:  40836.40
    Transfer/sec:      4.88MB
    ```
    - 2G内存，命令：``java -jar -Xmx2g -Xms2g gateway-server-0.0.1-SNAPSHOT.jar``  
    压测结果：
    ```
    Running 1m test @ http://localhost:8088/api/hello
    8 threads and 40 connections
    Thread Stats   Avg      Stdev     Max   +/- Stdev
        Latency     7.42ms   30.21ms 237.98ms   94.79%
        Req/Sec     6.48k     1.45k   10.80k    88.51%
    2668843 requests in 1.00m, 318.63MB read
    Requests/sec:  44445.05
    Transfer/sec:      5.31MB
    ```
- CMS GC
    - 512M内存，命令：``java -XX:+UseConcMarkSweepGC -Xmx512m -Xms512m -jar gateway-server-0.0.1-SNAPSHOT.jar``  
    压测结果：
    ```
    Running 1m test @ http://localhost:8088/api/hello
    8 threads and 40 connections
    Thread Stats   Avg      Stdev     Max   +/- Stdev
        Latency    34.59ms  154.79ms   1.37s    95.89%
        Req/Sec     5.67k     1.39k   22.25k    88.93%
    2523606 requests in 1.00m, 301.29MB read
    Requests/sec:  41994.52
    Transfer/sec:      5.01MB
    ```
    - 2G内存，命令：``java -XX:+UseConcMarkSweepGC -Xmx2g -Xms2g -jar gateway-server-0.0.1-SNAPSHOT.jar``  
    压测结果：
    ```
    Running 1m test @ http://localhost:8088/api/hello
    8 threads and 40 connections
    Thread Stats   Avg      Stdev     Max   +/- Stdev
        Latency    18.98ms   65.09ms 816.56ms   91.58%
        Req/Sec     5.62k     1.66k   17.47k    84.10%
    2507217 requests in 1.00m, 299.34MB read
    Requests/sec:  41730.80
    Transfer/sec:      4.98MB
    ```
- G1 GC
    - 512M内存，命令：``java -XX:+UseG1GC -Xmx512m -Xms512m -jar gateway-server-0.0.1-SNAPSHOT.jar``  
    压测结果：
    ```
    Running 1m test @ http://localhost:8088/api/hello
    8 threads and 40 connections
    Thread Stats   Avg      Stdev     Max   +/- Stdev
        Latency     8.92ms   35.26ms 363.62ms   94.41%
        Req/Sec     5.74k     1.22k   10.67k    90.44%
    2550977 requests in 1.00m, 304.56MB read
    Requests/sec:  42449.83
    Transfer/sec:      5.07MB
    ```
    - 2G内存，命令：``java -XX:+UseG1GC -Xmx2g -Xms2g -jar gateway-server-0.0.1-SNAPSHOT.jar``  
    压测结果：
    ```
    Running 1m test @ http://localhost:8088/api/hello
    8 threads and 40 connections
    Thread Stats   Avg      Stdev     Max   +/- Stdev
        Latency     5.34ms   23.74ms 288.16ms   95.90%
        Req/Sec     5.94k     1.15k   10.76k    90.25%
    2443416 requests in 1.00m, 291.72MB read
    Requests/sec:  40656.33
    Transfer/sec:      4.85MB
    ```

> 总结：
> 1. 对于QPS指标来说，4种GC，不相上下，没有明显差异
> 2. 对于延迟来说，针对均值讨论，CMS的延迟明显高于其他三类GC，并行 GC和G1 GC的表现略好于串行GC
> 3. 整体感觉差别不大……


**3、(选做)如果自己本地有可以运行的项目，可以按照2的方式进行演练。**

（略）:P

## Day4 作业
**1、(可选)运行课上的例子，以及 Netty 的例子，分析相关现象。**    
    测试命令及参数：``sb -u http://localhost:*/ -c 40 -N 30``
    
- HttpServer01 BIO版本
```
RPS: 31.7 (requests/second)
Max: 1481ms
Min: 279ms
Avg: 1234.2ms

  50%	below 1251ms
  60%	below 1251ms
  70%	below 1252ms
  80%	below 1252ms
  90%	below 1253ms
  95%	below 1255ms
  98%	below 1257ms
  99%	below 1265ms
99.9%	below 1481ms
```
- HttpServer02 多线程版本
```
RPS: 985 (requests/second)
Max: 316ms
Min: 0ms
Avg: 33.5ms

  50%	below 29ms
  60%	below 31ms
  70%	below 33ms
  80%	below 38ms
  90%	below 47ms
  95%	below 61ms
  98%	below 83ms
  99%	below 101ms
99.9%	below 167ms
```

- HttpServer03 线程池版本
```
RPS: 1003.2 (requests/second)
Max: 314ms
Min: 0ms
Avg: 30.1ms

  50%	below 27ms
  60%	below 28ms
  70%	below 30ms
  80%	below 31ms
  90%	below 37ms
  95%	below 51ms
  98%	below 84ms
  99%	below 106ms
99.9%	below 156ms
```

- Netty 版本
```
RPS: 4017.1 (requests/second)
Max: 353ms
Min: 0ms
Avg: 0.9ms

  50%	below 0ms
  60%	below 0ms
  70%	below 0ms
  80%	below 0ms
  90%	below 1ms
  95%	below 4ms
  98%	below 13ms
  99%	below 22ms
99.9%	below 47ms
```


**2、写一段代码，使用 HttpClient 或 OkHttp 访问 http://localhost:8801，代码提交到 Github。**
```Java
package luyz.learn;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import java.io.IOException;

/**
 * @author : Luyz
 * @date : 2020/10/28 16:56
 */
public class HttpClientDemo {
    public static void main(String[] args) {
        doRequest();
    }
    private static void doRequest() {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet("http://localhost:8801");
        try {
            CloseableHttpResponse response = client.execute(httpGet);
            HttpEntity responseEntity = response.getEntity();
            if (responseEntity != null) {
                System.out.println(EntityUtils.toString(responseEntity));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

```