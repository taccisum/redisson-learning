package com.github.taccisum.learning.redission;

import org.redisson.spring.starter.RedissonAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author tac - liaojf@cheegu.com
 * @since 2018/9/21
 */
@SpringBootApplication
@ImportAutoConfiguration(RedissonAutoConfiguration.class)
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
