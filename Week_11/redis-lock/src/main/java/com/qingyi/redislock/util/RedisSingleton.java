package com.qingyi.redislock.util;

import redis.clients.jedis.Jedis;

import java.util.Arrays;

/**
 * @author : Luyz
 * @date : 2021/1/6 15:45
 */
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
