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

    配置过程：
    - 启动6个server，[配置文件](./cluster)
    - server-cli ，使用cluster meet命令添加各个节点。
    ```
    127.0.0.1:6379> CLUSTER MEET 127.0.0.1 6380
    127.0.0.1:6379> CLUSTER MEET 127.0.0.1 6381
    127.0.0.1:6379> CLUSTER MEET 127.0.0.1 6382
    127.0.0.1:6379> CLUSTER MEET 127.0.0.1 6383
    127.0.0.1:6379> CLUSTER MEET 127.0.0.1 6384
    ```
    - 分配slot给三个节点, [add_slot脚本](./cluster/add_slot.sh)
    ```Bash
    sh add_slots.sh 0 5461 6379
    sh add_slots.sh 5462 10922 6381
    sh add_slots.sh 10923 16383 6383
    ```
    - 三个从节点分别对应上主节点
    ```
    ➜ redis-cli -p 6380 cluster replicate 86a14fbe3424d92418e75b806a82a37a1e62f3eb
    ➜ redis-cli -p 6382 cluster replicate 7904e06421fb8287886902f2bb00aa6d81f77ba9
    ➜ redis-cli -p 6384 cluster replicate 9307bff39389c14593e1f8f0cf05d70a6d667026
    ```

    - 验证：cluster nodes及slot
    ```
    ➜ redis-cli -p 6379 cluster nodes
    2194598e8e1cd9084689308155e854f2f9b155b9 127.0.0.1:6384@16384 slave 9307bff39389c14593e1f8f0cf05d70a6d667026 0 1610004272000 5 connected
    9307bff39389c14593e1f8f0cf05d70a6d667026 127.0.0.1:6383@16383 master - 0 1610004273272 4 connected 10923-16383
    8b88411d87b0bd8e7ed583f41b97f5c14c628810 127.0.0.1:6382@16382 slave 7904e06421fb8287886902f2bb00aa6d81f77ba9 0 1610004271215 3 connected
    993658e69da347b4681ddd90a4199938d918cf61 127.0.0.1:6380@16380 slave 86a14fbe3424d92418e75b806a82a37a1e62f3eb 0 1610004272144 1 connected
    7904e06421fb8287886902f2bb00aa6d81f77ba9 127.0.0.1:6381@16381 master - 0 1610004272249 2 connected 5462-10922
    86a14fbe3424d92418e75b806a82a37a1e62f3eb 127.0.0.1:6379@16379 myself,master - 0 1610004272000 1 connected 0-5461
    ➜ redis-cli -p 6379 cluster slots
    1)  1) (integer) 10923
        2) (integer) 16383
        3)  1) "127.0.0.1"
            2) (integer) 6383
            3) "9307bff39389c14593e1f8f0cf05d70a6d667026"
        4)  1) "127.0.0.1"
            2) (integer) 6384
            3) "2194598e8e1cd9084689308155e854f2f9b155b9"
    2)  1) (integer) 5462
        2) (integer) 10922
        3)  1) "127.0.0.1"
            2) (integer) 6381
            3) "7904e06421fb8287886902f2bb00aa6d81f77ba9"
        4)  1) "127.0.0.1"
            2) (integer) 6382
            3) "8b88411d87b0bd8e7ed583f41b97f5c14c628810"
    3)  1) (integer) 0
        2) (integer) 5461
        3)  1) "127.0.0.1"
            2) (integer) 6379
            3) "86a14fbe3424d92418e75b806a82a37a1e62f3eb"
        4)  1) "127.0.0.1"
            2) (integer) 6380
            3) "993658e69da347b4681ddd90a4199938d918cf61"
    ```
2. （选做）练习示例代码里下列类中的作业题：``08cache/redis/src/main/java/io/kimmking/cache/RedisApplication.java``

3. （挑战☆）练习redission的各种功能； 
4. （挑战☆☆）练习hazelcast的各种功能； 
5. （挑战☆☆☆）搭建hazelcast 3节点集群，写入100万数据到一个map，模拟和演 示高可用，测试一下性能；