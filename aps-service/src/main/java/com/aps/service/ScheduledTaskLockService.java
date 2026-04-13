package com.aps.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduledTaskLockService {

    private static final Duration LOCK_TTL = Duration.ofMinutes(5);
    private static final RedisScript<Boolean> UNLOCK_SCRIPT = new DefaultRedisScript<>(
            """
            if redis.call('get', KEYS[1]) == ARGV[1] then
                return redis.call('del', KEYS[1]) == 1
            end
            return false
            """,
            Boolean.class);

    private final StringRedisTemplate stringRedisTemplate;

    public boolean tryLock(String taskName, String ownerToken) {
        return Boolean.TRUE.equals(stringRedisTemplate.opsForValue()
                .setIfAbsent(buildKey(taskName), ownerToken, LOCK_TTL));
    }

    public boolean unlock(String taskName, String ownerToken) {
        return Boolean.TRUE.equals(stringRedisTemplate.execute(
                UNLOCK_SCRIPT,
                List.of(buildKey(taskName)),
                ownerToken));
    }

    private String buildKey(String taskName) {
        return "lock:scheduled:" + taskName;
    }
}
