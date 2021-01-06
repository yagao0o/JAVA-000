package com.qingyi.redislock;

import com.qingyi.redislock.util.RedisLock;
import com.qingyi.redislock.util.RedisSingleton;
import com.qingyi.redislock.util.Stock;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;
import redis.clients.jedis.Jedis;

/**
 * @author : Luyz
 * @date : 2021/1/6 14:32
 */
@SpringBootApplication
@EnableAutoConfiguration
public class RedisLockApplication {
    public static void main(String[] args) {
        SpringApplication.run(RedisLockApplication.class, args);
        Stock apples = new Stock("apple");
        apples.addItems(100);
        System.out.println(apples.getOne());
        System.out.println(apples.getItems(50));
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(apples.getOne());
        System.out.println(apples.getItems(48));
        System.out.println(apples.getOne());
    }
}
