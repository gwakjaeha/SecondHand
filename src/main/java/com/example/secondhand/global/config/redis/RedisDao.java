package com.example.secondhand.global.config.redis;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

@Component
public class RedisDao {
    private final RedisTemplate<String, String> redisTemplate;

    public RedisDao(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void setValues(String key, String data) {
        ValueOperations<String, String> values = redisTemplate.opsForValue();
        values.set(key, data);
    }

    public void setValues(String key, String data, Duration duration) {
        ValueOperations<String, String> values = redisTemplate.opsForValue();
        values.set(key, data, duration);
    }

    public void setValuesForSet(String key, String data) {
        SetOperations<String, String> values = redisTemplate.opsForSet();
        values.add(key, data);
    }

    public void setValuesForHash(String key, Map data) {
        HashOperations<String, String, String> values = redisTemplate.opsForHash();
        values.putAll(key, data);
    }

    public String getValues(String key) {
        ValueOperations<String, String> values = redisTemplate.opsForValue();
        return values.get(key);
    }

    public Set<String> getValuesForSet(String key) {
        SetOperations<String, String> values = redisTemplate.opsForSet();
        Set<String> set = values.members(key);
        return set;
    }

    public Map<String, String> getValuesForHash(String key) {
        HashOperations<String, String, String> values = redisTemplate.opsForHash();
        Map<String, String> entries = values.entries(key);
        return entries;
    }

    public void deleteValues(String key) {
        redisTemplate.delete(key);
    }
}
