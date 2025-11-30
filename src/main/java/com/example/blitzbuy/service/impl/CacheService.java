package com.example.blitzbuy.service.impl;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class CacheService {
    private final StringRedisTemplate redisTemplate;

    public CacheService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // we have to create a method that runs on startup

    public void cacheString(String key, String value, long timeout, TimeUnit timeUnit){
        redisTemplate.opsForValue().set(key, value, timeout, timeUnit);
    }

    public String getCachedString(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public Long incrementCounter(String key) {
        return redisTemplate.opsForValue().increment(key);
    }

    public Long decrementCounter(String key){
        return redisTemplate.opsForValue().decrement(key);
    }
}
