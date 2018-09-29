package com.github.taccisum.learning.redission;

import org.assertj.core.util.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RScript;
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

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author tac - liaojf@cheegu.com
 * @since 2018/9/25
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("lua")
@ImportAutoConfiguration(LuaTest.Config.class)
public class LuaTest {
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
    public void testScript() throws Exception {
        redissonClient.getBucket("tac").set(1);
        Integer tac = redissonClient.getScript().eval(RScript.Mode.READ_ONLY, lua("test_script"), RScript.ReturnType.INTEGER, Lists.newArrayList("tac"));
        assertThat(tac).isEqualTo(1);
    }

    @Test
    public void testScript1() throws Exception {
        redissonClient.getBucket("tac").set(1);
        assertThat(redissonClient.getScript().<Long>eval(RScript.Mode.READ_ONLY, lua("test_script1"), RScript.ReturnType.INTEGER, Lists.newArrayList("tac"), true)).isEqualTo(2);
        assertThat(redissonClient.getScript().<Integer>eval(RScript.Mode.READ_ONLY, lua("test_script1"), RScript.ReturnType.INTEGER, Lists.newArrayList("tac"), false)).isEqualTo(1);
    }

    static String lua(String fileName) throws IOException {
        String classpath = Paths.get("lua", fileName + ".lua").toString();
        URL resource = LuaTest.class.getClassLoader().getResource(classpath);
        if (resource == null) {
            throw new RuntimeException("not such lua script file on classpath " + classpath);
        }
        Path p;
        try {
            p = Paths.get(resource.toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        if (!Files.isReadable(p)) {
            throw new RuntimeException(String.format("file [%s] is not readable", p.toString()));
        }
        try {
            StringBuilder sb = new StringBuilder();
            List<String> lines = Files.readAllLines(p);
            for (String line : lines) {
                sb.append(line).append(System.lineSeparator());
            }
            String content = sb.toString();
            String script = content.substring(0, content.length() - System.lineSeparator().length());
            System.out.println(script);
            return script;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
