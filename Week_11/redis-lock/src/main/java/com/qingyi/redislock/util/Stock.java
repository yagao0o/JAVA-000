package com.qingyi.redislock.util;

/**
 * @author : Luyz
 * @date : 2021/1/6 18:20
 */
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
