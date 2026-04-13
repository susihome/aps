package com.aps.service.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component
@Slf4j
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 生成访问令牌
     */
    public String generateAccessToken(UUID userId, String username, List<String> roles, UUID sessionId, long sessionVersion) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(username)
                .claim("userId", userId.toString())
                .claim("roles", roles)
                .claim("sessionId", sessionId.toString())
                .claim("sessionVersion", sessionVersion)
                .claim("type", "access")
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 生成刷新令牌
     */
    public String generateRefreshToken(UUID userId, String username, UUID sessionId, long sessionVersion) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshExpiration);

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(username)
                .claim("userId", userId.toString())
                .claim("sessionId", sessionId.toString())
                .claim("sessionVersion", sessionVersion)
                .claim("type", "refresh")
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 从令牌中获取用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.getSubject();
    }

    /**
     * 从令牌中获取用户ID
     */
    public UUID getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        String userId = claims.get("userId", String.class);
        return UUID.fromString(userId);
    }

    public UUID getSessionIdFromToken(String token) {
        Claims claims = parseToken(token);
        String sessionId = claims.get("sessionId", String.class);
        return UUID.fromString(sessionId);
    }

    public long getSessionVersionFromToken(String token) {
        Claims claims = parseToken(token);
        Number sessionVersion = claims.get("sessionVersion", Number.class);
        return sessionVersion.longValue();
    }

    public String getTokenId(String token) {
        return parseToken(token).getId();
    }

    public Instant getExpiration(String token) {
        return parseToken(token).getExpiration().toInstant();
    }

    public Duration getRemainingValidity(String token) {
        Duration duration = Duration.between(Instant.now(), getExpiration(token));
        return duration.isNegative() ? Duration.ZERO : duration;
    }

    /**
     * 从令牌中获取角色列表
     */
    @SuppressWarnings("unchecked")
    public List<String> getRolesFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("roles", List.class);
    }

    /**
     * 验证令牌
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("JWT token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 检查是否为刷新令牌
     */
    public boolean isRefreshToken(String token) {
        try {
            Claims claims = parseToken(token);
            return "refresh".equals(claims.get("type", String.class));
        } catch (JwtException e) {
            return false;
        }
    }

    /**
     * 解析令牌
     */
    private Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
