package com.example.blitzbuy.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

@Slf4j
@Configuration
public class RedisConfig {


    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    @Bean
    public RedisConfiguration defaultRedisConfig() {
        log.info("DefaultRedisConfig: with redis host:{} and port:{}", redisHost, redisPort);
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(redisHost);
        config.setPort(redisPort);
        return config;
    }


    @Bean
    public LettuceConnectionFactory redisConnectionFactory(RedisConfiguration defaultRedisConfig) {
        return new LettuceConnectionFactory(defaultRedisConfig);
    }


}
