package com.aps.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final StringRedisTemplate stringRedisTemplate;

    public void blacklist(String tokenId, Duration ttl) {
        if (tokenId == null || tokenId.isBlank() || ttl == null || ttl.isNegative() || ttl.isZero()) {
            return;
        }
        stringRedisTemplate.opsForValue().set(buildKey(tokenId), "1", ttl);
    }

    public boolean isBlacklisted(String tokenId) {
        if (tokenId == null || tokenId.isBlank()) {
            return false;
        }
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(buildKey(tokenId)));
    }

    private String buildKey(String tokenId) {
        return "auth:blacklist:" + tokenId;
    }
}
