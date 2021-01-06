package com.qingyi.redislock.util;

import java.util.UUID;

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
