
package com.paysystem.paysystem.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RedisLockService {

    private final RedisTemplate<String, String> redisTemplate;

    public boolean acquireLock(String lockKey, Duration timeout) {
        return Boolean.TRUE.equals(
                redisTemplate.opsForValue().setIfAbsent(lockKey, "locked", timeout)
        );
    }

    public void releaseLock(String lockKey) {
        redisTemplate.delete(lockKey);
    }
}
