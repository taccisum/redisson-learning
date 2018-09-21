package com.github.taccisum.learning.redission;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RKeys;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author tac - liaojf@cheegu.com
 * @since 2018/9/21
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class GettingStartTest {
    @Autowired
    private RedissonClient redissonClient;

    @Test
    public void keys() throws Exception {
        if (redissonClient.isShutdown()) {
            System.out.println("redission is shutdown now");
        }
        RKeys keys = redissonClient.getKeys();
        keys.getKeys().forEach(k -> System.out.println(k));
    }
}
