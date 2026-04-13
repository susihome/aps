package com.aps.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ScheduleLockService {

    private static final Duration LOCK_TTL = Duration.ofMinutes(10);
    private static final RedisScript<Boolean> UNLOCK_SCRIPT = new DefaultRedisScript<>(
            """
            if redis.call('get', KEYS[1]) == ARGV[1] then
                return redis.call('del', KEYS[1]) == 1
            end
            return false
            """,
            Boolean.class);
    private static final RedisScript<Boolean> RENEW_SCRIPT = new DefaultRedisScript<>(
            """
            if redis.call('get', KEYS[1]) == ARGV[1] then
                return redis.call('expire', KEYS[1], ARGV[2]) == 1
            end
            return false
            """,
            Boolean.class);

    private final StringRedisTemplate stringRedisTemplate;

    public boolean tryLock(UUID scheduleId, String ownerToken) {
        return Boolean.TRUE.equals(stringRedisTemplate.opsForValue()
                .setIfAbsent(buildKey(scheduleId), ownerToken, LOCK_TTL));
    }

    public boolean unlock(UUID scheduleId, String ownerToken) {
        return Boolean.TRUE.equals(stringRedisTemplate.execute(
                UNLOCK_SCRIPT,
                List.of(buildKey(scheduleId)),
                ownerToken));
    }

    public boolean renewLock(UUID scheduleId, String ownerToken) {
        return Boolean.TRUE.equals(stringRedisTemplate.execute(
                RENEW_SCRIPT,
                List.of(buildKey(scheduleId)),
                ownerToken,
                String.valueOf(LOCK_TTL.toSeconds())));
    }

    public boolean isOwnedBy(UUID scheduleId, String ownerToken) {
        String currentOwnerToken = stringRedisTemplate.opsForValue().get(buildKey(scheduleId));
        return ownerToken != null && ownerToken.equals(currentOwnerToken);
    }

    private String buildKey(UUID scheduleId) {
        return "lock:schedule:solve:" + scheduleId;
    }
}
