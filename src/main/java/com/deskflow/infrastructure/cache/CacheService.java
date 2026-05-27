package com.deskflow.infrastructure.cache;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

/**
 * Thin Redis wrapper used by the auth module for token management.
 * Business-level caching uses @Cacheable/@CacheEvict on service methods.
 */
@Service
@RequiredArgsConstructor
public class CacheService {

    private final StringRedisTemplate stringRedisTemplate;

    public void set(String key, String value, Duration ttl) {
        stringRedisTemplate.opsForValue().set(key, value, ttl);
    }

    public Optional<String> get(String key) {
        return Optional.ofNullable(stringRedisTemplate.opsForValue().get(key));
    }

    public void delete(String key) {
        stringRedisTemplate.delete(key);
    }

    public boolean exists(String key) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
    }

    public void expire(String key, Duration ttl) {
        stringRedisTemplate.expire(key, ttl);
    }
}