package com.github.taccisum.learning.redission;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RKeys;
import org.redisson.api.RedissonClient;
import org.redisson.spring.starter.RedissonAutoConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import redis.embedded.RedisServer;

/**
 * @author tac - liaojf@cheegu.com
 * @since 2018/9/21
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@ActiveProfiles("getting-start")
@ImportAutoConfiguration(GettingStartTest.Config.class)
public class GettingStartTest {
    @Autowired
    private RedissonClient redissonClient;

    @TestConfiguration
    @AutoConfigureBefore(RedissonAutoConfiguration.class)
    public static class Config {
        @Bean(destroyMethod = "stop")
        public RedisServer redisServer() {
            RedisServer redisServer = RedisServer.builder().setting("maxheap 32M").port(6379).build();
            redisServer.start();
            return redisServer;
        }
    }

    @Test
    public void keys() throws Exception {
        if (redissonClient.isShutdown()) {
            System.out.println("redission is shutdown now");
        }
        RKeys keys = redissonClient.getKeys();
        keys.getKeys().forEach(k -> System.out.println(k));
    }

    @Test
    public void async() throws Exception {
        RAtomicLong atomicLong = redissonClient.getAtomicLong("atomicLong");
        atomicLong.incrementAndGetAsync()
                .whenComplete((res, e) -> {
                    System.out.println(res);
                });
    }
}
