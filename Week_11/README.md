# Week11 作业
# Day 21

**圣诞快乐**

# Day 22

**新年快乐**

1. （选做）命令行下练习操作Redis的各种基本数据结构和命令。 
     - string \ hash \ list 这三种类型比较熟悉，平时较为常用，略

    - set 的集合操作
    ```Bash
    127.0.0.1:6379> sadd set1 1 3 5 7 9
    5
    127.0.0.1:6379> sadd set2 2 3 5 7 11
    5
    127.0.0.1:6379> sdiff set1 set2
    1
    9
    127.0.0.1:6379> sdiff set2 set1
    2
    11
    127.0.0.1:6379> sinter set1 set2
    3
    5
    7
    127.0.0.1:6379> sunion set1 set2
    1
    2
    3
    5
    7
    9
    11
    ```

    - sorted set 
    ```
    127.0.0.1:6379> zadd math 76 xiaoqiang 84 xiaohong
    2
    127.0.0.1:6379> zadd math 95 libai 92 hanhan
    2
    127.0.0.1:6379> ZRANGE math 0 -1
    xiaoqiang
    xiaohong
    hanhan
    libai
    127.0.0.1:6379> ZRANGE math 0 -1 withscores
    xiaoqiang
    76
    xiaohong
    84
    hanhan
    92
    libai
    95
    127.0.0.1:6379> ZRANGEBYSCORE math 90 100
    hanhan
    libai
    ```
    - bitmap
    ```
    // 和string联动
    127.0.0.1:6379> setbit k1 1 1
    (integer) 0
    127.0.0.1:6379> get k1
    "@"
    127.0.0.1:6379> setbit k1 7 1
    (integer) 0
    127.0.0.1:6379> get k1

    // BITCOUNT 统计的其实是按byte进行统计【如果设置start和end的下标的话，每个byte对应8个bit】
    127.0.0.1:6379> BITCOUNT k1 0 0
    (integer) 2
    "A"

    // BITPOS start也是按照byte计算
    127.0.0.1:6379> bitpos k1 0
    0
    127.0.0.1:6379> bitpos k1 1
    1
    127.0.0.1:6379> bitpos k1 1 30
    1019
    ```
    - geo
    ```
    127.0.0.1:6379> GEOADD jinan 117.13928 36.673065 p1 117.137618 36.68427 p2
    2
    127.0.0.1:6379> GEODIST jinan p1 p2
    1255.1987
    127.0.0.1:6379> GEOHASH jinan p1
    wwe29s4hp40
    127.0.0.1:6379> GEORADIUSBYMEMBER jinan p1 1 km
    p1
    127.0.0.1:6379> GEORADIUSBYMEMBER jinan p1 5 km WITHCOORD withdist
    p1
    0.0000
    117.13928014039993286
    36.67306432676461725
    p2
    1.2552
    117.13761717081069946
    36.68427032901005447
    ```
    // TODO: hyperloglogs

2. （选做）分别基于jedis，RedisTemplate，Lettuce，Redission实现redis基本操作 的demo，可以使用spring-boot集成上述工具。 
// TODO:

3. （选做）spring集成练习: 
    - 实现update方法，配合@CachePut
    - 实现delete方法，配合@CacheEvict
    - 将示例中的spring集成Lettuce改成jedis或redisson。 
// TODO:

4. （必做）基于Redis封装分布式数据操作： 
    - 在Java中实现一个简单的分布式锁； 
    - 在Java中实现一个分布式计数器，模拟减库存。 

    [项目代码](./redis-lock),做了简单的实现，异常处理没做，续期没做，超时也没来得及做，估计需要学习一下其他同学的实现，想了个版本感觉很不优雅……
    - 基于jedis写了个简单的基础工具类,Lock部分参考ppt重的lua脚本
    ```Java
    public class RedisSingleton {
        private final Jedis jedis;
        private static final Object LOCK = new Object();
        private static RedisSingleton singleton = null;

        private RedisSingleton() {
            jedis = new Jedis("localhost", 6379);
        }

        public static RedisSingleton getSingleton() {
            if (singleton == null) {
                synchronized (LOCK) {
                    if (singleton == null) {
                        singleton = new RedisSingleton();
                    }
                }
            }
            return singleton;
        }

        public void setInteger(String name, Integer value) {
            jedis.set(name, value.toString());
        }

        public Integer getInteger(String name) {
            String sizeStr = jedis.get(name);
            if (sizeStr != null) {
                return Integer.parseInt(sizeStr);
            }
            return 0;
        }

        public boolean getLock(String lockName, String randomValue) {
            String result = jedis.set(lockName, randomValue, "NX", "EX", 30000);
            return result != null;
        }

        public boolean releaseLock(String lockName, String randomValue) {
            String lua = "if redis.call(\"get\", KEYS[1]) == ARGV[1] then\n" +
                    "\treturn redis.call(\"del\",KEYS[1])\n" +
                    "else\n" + "\treturn 0\n" + "end\n";
            Object result = jedis.eval(lua, Arrays.asList(lockName), Arrays.asList(randomValue));
            return "1".equals(result.toString());
        }
    }

    ```
    - 分布式锁实现（简陋版本）
    ```Java
    /**
    * @author : Luyz
    * @date : 2021/1/6 18:05
    */
    public class RedisLock {
        String name;
        String value;
        public RedisLock(String name) {
            this.name = name;
        }

        public void getLock() {
            value = UUID.randomUUID().toString();
            while (true) {
                if(RedisSingleton.getSingleton().getLock(name, value)) {
                    break;
                }
            }
            // 没做异常处理
        }

        public void unLock() {
            RedisSingleton.getSingleton().releaseLock(name, value);
        }
    }
    ```
    - 在分布式锁的基础上，添加了库存管理部分：
    ```Java
    public class Stock {
        String name;

        public Stock(String name) {
            this.name = name;
        }

        public boolean getOne() {
            return getItems(1);
        }

        public boolean getItems(Integer count) {
            RedisLock lock = new RedisLock(name + ":lock");
            lock.getLock();
            RedisSingleton redisSingleton = RedisSingleton.getSingleton();
            Integer remain = redisSingleton.getInteger(name);
            if (remain >= count) {
                redisSingleton.setInteger(name, remain - count);
                lock.unLock();
                return true;
            } else {
                lock.unLock();
                return false;
            }
        }

        public void addOne() {
            addItems(1);
        }

        public void addItems(Integer count) {
            RedisLock lock = new RedisLock(name + ":lock");
            lock.getLock();
            RedisSingleton redisSingleton = RedisSingleton.getSingleton();
            Integer remain = redisSingleton.getInteger(name);
            redisSingleton.setInteger(name, remain + count);
            lock.unLock();
        }
    }

    ```


5. 基于Redis的PubSub实现订单异步处理

    // TODO:


// TODO: 挑战题

