package com.github.taccisum.learning.redission;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RLock;
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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author tac - liaojf@cheegu.com
 * @since 2018/9/21
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("lock")
@ImportAutoConfiguration(LockTest.Config.class)
public class LockTest {
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
    public void simple() throws Exception {
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        for (int i = 0; i < 10; i++) {
            executorService.execute(new Counter(redissonClient));
        }
        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.SECONDS);
        assertThat(Counter.count).isEqualTo(1000 * 10);
    }

    @Test
    public void atomic() throws Exception {
        RAtomicLong atomicLong = redissonClient.getAtomicLong("atomicLong");
        assertThat(atomicLong.get()).isEqualTo(0);
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        for (int i = 0; i < 10; i++) {
            executorService.execute(() -> {
                for (int j = 0; j < 1000; j++) {
                    atomicLong.getAndIncrement();
                }
            });
        }
        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.SECONDS);
        assertThat(atomicLong.get()).isEqualTo(1000 * 10);
    }

    @Test
    public void counter() throws Exception {
        RAtomicLong count = redissonClient.getAtomicLong("count");
        assertThat(count.get()).isEqualTo(0);
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        final int MAX = 5000;
        for (int i = 0; i < 10; i++) {
            executorService.execute(() -> {
//                error
//                while (count.get() < MAX) {
//                    count.getAndIncrement();
//                }

//                right
                while (count.get() < MAX) {
                    RLock lock = redissonClient.getLock("counter");
                    //noinspection Duplicates
                    try {
                        lock.lock();
                        if (count.get() < MAX) {
                            count.getAndIncrement();
                        }
                    } finally {
                        lock.unlock();
                    }
                }
            });
        }
        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.SECONDS);
        assertThat(count.get()).isEqualTo(MAX);
    }

    @Test
    public void counter1() throws Exception {
        RAtomicLong count1 = redissonClient.getAtomicLong("count_1");
        RAtomicLong count2 = redissonClient.getAtomicLong("count_2");
        assertThat(count1.get()).isEqualTo(0);
        assertThat(count2.get()).isEqualTo(0);
        ExecutorService executorService = Executors.newFixedThreadPool(20);
        final int MAX = 5000;
        for (int i = 0; i < 10; i++) {
            executorService.execute(() -> {
                while (count1.get() < MAX) {
                    RLock lock = redissonClient.getLock("counter_1");
                    //noinspection Duplicates
                    try {
                        lock.lock();
                        if (count1.get() < MAX) {
                            count1.getAndIncrement();
                        }
                    } finally {
                        lock.unlock();
                    }
                }
            });
        }
        for (int i = 0; i < 10; i++) {
            executorService.execute(() -> {
                while (count2.get() < MAX) {
                    RLock lock = redissonClient.getLock("counter_2");
                    //noinspection Duplicates
                    try {
                        lock.lock();
                        if (count2.get() < MAX) {
                            count2.getAndIncrement();
                        }
                    } finally {
                        lock.unlock();
                    }
                }
            });
        }
        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.SECONDS);
        assertThat(count1.get()).isEqualTo(MAX);
        assertThat(count2.get()).isEqualTo(MAX);
    }

    @Test
    public void getAndIncrementPerformance() throws Exception {
        RAtomicLong atomicLong = redissonClient.getAtomicLong("getAndIncrementPerformance");
        for (int i = 0; i < 10000; i++) {
            atomicLong.getAndIncrement();
        }
    }

    public static class Counter extends Thread {
        private RedissonClient redissonClient;
        private static int count = 0;

        public Counter(RedissonClient redissonClient) {
            this.redissonClient = redissonClient;
        }

        @Override
        public void run() {
            for (int i = 0; i < 1000; i++) {
                RLock lock = redissonClient.getLock("simple-lock");
                try {
                    lock.lock();
                    count++;
                } finally {
                    lock.unlock();
                }
            }
        }
    }
}
