package com.example.my_seckill.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

// 不做任何配置，存到redis中存的是二进制数据，如果我们要存对象，我们要对其序列化
@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String , Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        // (默认)jdk产生的是二进制， gennericjsontojson 产生的java字符串
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();

        redisTemplate.setKeySerializer(new StringRedisSerializer()); // key序列化
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer()); // value序列化  ,jdk产生的是字符串，如果jsontojson需要传入类，但是genneicijsontojson不需要传入java对象

        // hash是特殊的数据类型
        redisTemplate.setHashKeySerializer(new StringRedisSerializer()); // hash key序列化
        redisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer()); // hash value序列化



        // 注入连接工厂
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        return redisTemplate;
    }

    @Bean
    public DefaultRedisScript<Long> script() {
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        // 放在了 application.yaml 同层目录
        redisScript.setLocation(new ClassPathResource("stock.lua"));
        redisScript.setResultType(Long.class);
        return redisScript;
    }
}
