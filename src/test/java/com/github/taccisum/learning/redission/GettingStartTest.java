package com.github.taccisum.learning.redission;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RBucket;
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

import static org.assertj.core.api.Assertions.assertThat;

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

    @Test
    public void bucket() throws Exception {
        RBucket<FooModel> foo = redissonClient.getBucket("foo");
        assertThat(foo.isExists()).isFalse();
        foo.set(new FooModel("tac", 123L));
        assertThat(foo.isExists()).isTrue();
        assertThat(foo.get().getFoo1()).isEqualTo("tac");
        assertThat(foo.get().getFoo2()).isEqualTo(123L);
    }

    public static class FooModel {
        private String foo1;
        private Long foo2;

        public FooModel() {
        }

        public FooModel(String foo1, Long foo2) {
            this.foo1 = foo1;
            this.foo2 = foo2;
        }

        public String getFoo1() {
            return foo1;
        }

        public void setFoo1(String foo1) {
            this.foo1 = foo1;
        }

        public Long getFoo2() {
            return foo2;
        }

        public void setFoo2(Long foo2) {
            this.foo2 = foo2;
        }
    }
}
