package com.github.taccisum.learning.redission.runner;

import org.redisson.api.RKeys;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * @author tac - liaojf@cheegu.com
 * @since 2018/9/21
 */
@Component
public class FooRunner implements ApplicationRunner {
    @Autowired
    private RedissonClient redissonClient;

    @Override
    public void run(ApplicationArguments applicationArguments) throws Exception {
        if (redissonClient.isShutdown()) {
            System.out.println("redission is shutdown now");
        }
        RKeys keys = redissonClient.getKeys();
        keys.getKeys().forEach(k -> System.out.println(k));
    }
}
