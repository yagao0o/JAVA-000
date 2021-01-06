# Week12

# Day 23
1. （必做）配置redis的主从复制，sentinel高可用，Cluster集群。 提交如下内容到github： 
    - config配置文件， 
    - 启动和操作、验证集群下数据读写的命令步骤。

    之前安装使用的redis版本是4.0.9，和老师演示版本大同小异，故未更新，部分配置参数和命令与课堂不同。
    - 纯主从模式，1主2从，[配置文件](./master-slave),启动命令：
    ```
    redis-server redis-1.conf
    redis-server redis-2.conf
    redis-server redis-3.conf

    redis-cli -h 127.0.0.1 -p 6380
        127.0.0.1:6380> set x1 2
        (error) READONLY You can't write against a read only slave.
    redis-cli -h 127.0.0.1 -p 6379
        127.0.0.1:6379> set x1 12
        OK
    切换到80
        127.0.0.1:6380> get x1
        "12"
    ```
    - 哨兵模式，1主2从2哨兵，[配置文件](./sentinel),启动命令：
    ```
    redis-server redis-1.conf
    redis-server redis-2.conf
    redis-server redis-3.conf
    redis-sentinel sentinel.conf
    redis-sentinel sentinel-2.conf
    ```
    正常操作下：同上。将主节点shutdown掉后，哨兵节点观察到信息如下：
    ```
    80493:X 07 Jan 00:28:02.510 # +failover-state-select-slave master mymaster 127.0.0.1 6379
    80493:X 07 Jan 00:28:02.591 # +selected-slave slave 127.0.0.1:6381 127.0.0.1 6381 @ mymaster 127.0.0.1 6379
    80493:X 07 Jan 00:28:02.591 * +failover-state-send-slaveof-noone slave 127.0.0.1:6381 127.0.0.1 6381 @ mymaster 127.0.0.1 6379
    80493:X 07 Jan 00:28:02.691 * +failover-state-wait-promotion slave 127.0.0.1:6381 127.0.0.1 6381 @ mymaster 127.0.0.1 6379
    80493:X 07 Jan 00:28:02.795 # +promoted-slave slave 127.0.0.1:6381 127.0.0.1 6381 @ mymaster 127.0.0.1 6379
    80493:X 07 Jan 00:28:02.795 # +failover-state-reconf-slaves master mymaster 127.0.0.1 6379
    80493:X 07 Jan 00:28:02.896 * +slave-reconf-sent slave 127.0.0.1:6380 127.0.0.1 6380 @ mymaster 127.0.0.1 6379
    80493:X 07 Jan 00:28:03.524 # -odown master mymaster 127.0.0.1 6379
    80493:X 07 Jan 00:28:03.826 * +slave-reconf-inprog slave 127.0.0.1:6380 127.0.0.1 6380 @ mymaster 127.0.0.1 6379
    80493:X 07 Jan 00:28:03.826 * +slave-reconf-done slave 127.0.0.1:6380 127.0.0.1 6380 @ mymaster 127.0.0.1 6379
    80493:X 07 Jan 00:28:03.910 # +failover-end master mymaster 127.0.0.1 6379
    80493:X 07 Jan 00:28:03.910 # +switch-master mymaster 127.0.0.1 6379 127.0.0.1 6381
    ```
    同时节点3日志如下：
    ```
    79919:M 07 Jan 00:28:02.691 * MASTER MODE enabled (user request from 'id=7 addr=127.0.0.1:60237 fd=11 name=sentinel-93a3e6c6-cmd age=76 idle=0 flags=x db=0 sub=0 psub=0 multi=3 qbuf=0 qbuf-free=32768 obl=36 oll=0 omem=0 events=r cmd=exec')
    79919:M 07 Jan 00:28:02.694 # CONFIG REWRITE executed with success.
    ```
    可见节点3被选举为新master，参考上步进行节点测试，仅节点3可写。重新启动节点1后，自动将当前节点切换为从节点，并从新主节点（节点3）抓取数据进行同步。

    - Cluster集群
    // TODO:

2. （选做）练习示例代码里下列类中的作业题：``08cache/redis/src/main/java/io/kimmking/cache/RedisApplication.java``

3. （挑战☆）练习redission的各种功能； 
4. （挑战☆☆）练习hazelcast的各种功能； 
5. （挑战☆☆☆）搭建hazelcast 3节点集群，写入100万数据到一个map，模拟和演 示高可用，测试一下性能；