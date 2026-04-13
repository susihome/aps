package com.aps.service;

import com.aps.domain.entity.AuthSession;
import com.aps.domain.entity.AuthUserSessionState;
import com.aps.domain.enums.AuthSessionStatus;
import com.aps.service.exception.TokenInvalidException;
import com.aps.service.repository.AuthSessionRepository;
import com.aps.service.repository.AuthUserSessionStateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthSessionService {

    private final AuthSessionRepository authSessionRepository;
    private final AuthUserSessionStateRepository authUserSessionStateRepository;
    private final StringRedisTemplate stringRedisTemplate;

    @Transactional
    public long getOrCreateSessionVersion(UUID userId) {
        AuthUserSessionState state = authUserSessionStateRepository.findById(userId)
                .orElseGet(() -> {
                    AuthUserSessionState newState = new AuthUserSessionState();
                    newState.setUserId(userId);
                    newState.setSessionVersion(1L);
                    return authUserSessionStateRepository.save(newState);
                });
        cacheSessionVersion(userId, state.getSessionVersion());
        return state.getSessionVersion();
    }

    @Transactional
    public void createSession(UUID sessionId, UUID userId, String username, String refreshTokenJti,
                              long sessionVersion, Instant expiresAt) {
        AuthSession session = new AuthSession();
        session.setId(sessionId);
        session.setUserId(userId);
        session.setUsername(username);
        session.setRefreshTokenJti(refreshTokenJti);
        session.setSessionVersion(sessionVersion);
        session.setStatus(AuthSessionStatus.ACTIVE);
        session.setExpiresAt(LocalDateTime.ofInstant(expiresAt, ZoneOffset.UTC));
        authSessionRepository.save(session);

        String sessionKey = buildSessionKey(sessionId);
        stringRedisTemplate.opsForHash().put(sessionKey, "userId", userId.toString());
        stringRedisTemplate.opsForHash().put(sessionKey, "refreshTokenJti", refreshTokenJti);
        stringRedisTemplate.opsForHash().put(sessionKey, "sessionVersion", Long.toString(sessionVersion));
        stringRedisTemplate.expireAt(sessionKey, expiresAt);
        stringRedisTemplate.opsForSet().add(buildUserSessionsKey(userId), sessionId.toString());
    }

    @Transactional(readOnly = true)
    public void validateRefreshSession(UUID userId, UUID sessionId, String refreshTokenJti, long tokenVersion) {
        if (!isSessionVersionValid(userId, tokenVersion)) {
            throw new TokenInvalidException("刷新令牌会话已失效");
        }

        Object cachedJti = stringRedisTemplate.opsForHash().get(buildSessionKey(sessionId), "refreshTokenJti");
        if (cachedJti != null) {
            if (!refreshTokenJti.equals(cachedJti.toString())) {
                throw new TokenInvalidException("刷新令牌会话已失效");
            }
            return;
        }

        AuthSession session = authSessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new TokenInvalidException("刷新令牌会话已失效"));

        if (session.getStatus() != AuthSessionStatus.ACTIVE
                || session.getExpiresAt().isBefore(LocalDateTime.now(ZoneOffset.UTC))
                || !refreshTokenJti.equals(session.getRefreshTokenJti())
                || !Long.valueOf(tokenVersion).equals(session.getSessionVersion())) {
            throw new TokenInvalidException("刷新令牌会话已失效");
        }
    }

    @Transactional(readOnly = true)
    public boolean isSessionVersionValid(UUID userId, long tokenVersion) {
        String key = buildUserVersionKey(userId);
        String cachedVersion = stringRedisTemplate.opsForValue().get(key);
        if (cachedVersion != null) {
            return Long.parseLong(cachedVersion) == tokenVersion;
        }

        long currentVersion = authUserSessionStateRepository.findById(userId)
                .map(AuthUserSessionState::getSessionVersion)
                .orElse(1L);
        cacheSessionVersion(userId, currentVersion);
        return currentVersion == tokenVersion;
    }

    @Transactional
    public void revokeSession(UUID userId, UUID sessionId) {
        authSessionRepository.findByIdAndUserId(sessionId, userId)
                .ifPresent(session -> {
                    session.setStatus(AuthSessionStatus.REVOKED);
                    authSessionRepository.save(session);
                });
        stringRedisTemplate.delete(buildSessionKey(sessionId));
        stringRedisTemplate.opsForSet().remove(buildUserSessionsKey(userId), sessionId.toString());
    }

    @Transactional
    public void revokeAllUserSessions(UUID userId) {
        List<AuthSession> sessions = authSessionRepository.findByUserIdAndStatus(userId, AuthSessionStatus.ACTIVE);
        for (AuthSession session : sessions) {
            session.setStatus(AuthSessionStatus.REVOKED);
            stringRedisTemplate.delete(buildSessionKey(session.getId()));
        }
        authSessionRepository.saveAll(sessions);
        stringRedisTemplate.delete(buildUserSessionsKey(userId));

        AuthUserSessionState state = authUserSessionStateRepository.findById(userId)
                .orElseGet(() -> {
                    AuthUserSessionState newState = new AuthUserSessionState();
                    newState.setUserId(userId);
                    newState.setSessionVersion(1L);
                    return newState;
                });
        state.setSessionVersion(state.getSessionVersion() + 1);
        state.setForceLogoutAt(LocalDateTime.now(ZoneOffset.UTC));
        authUserSessionStateRepository.save(state);
        cacheSessionVersion(userId, state.getSessionVersion());
    }

    @Transactional(readOnly = true)
    public List<AuthService.SessionView> listActiveSessions(UUID userId, UUID currentSessionId) {
        return authSessionRepository.findByUserIdAndStatus(userId, AuthSessionStatus.ACTIVE).stream()
                .sorted(Comparator.comparing(AuthSession::getCreateTime).reversed())
                .map(session -> new AuthService.SessionView(
                        session.getId(),
                        session.getUsername(),
                        session.getClientType(),
                        session.getClientIp(),
                        session.getUserAgent(),
                        session.getCreateTime(),
                        session.getExpiresAt(),
                        session.getLastAccessAt(),
                        session.getId().equals(currentSessionId)
                ))
                .toList();
    }

    @Transactional
    public void touchSession(UUID sessionId) {
        authSessionRepository.findById(sessionId)
                .filter(session -> session.getStatus() == AuthSessionStatus.ACTIVE)
                .ifPresent(session -> {
                    session.setLastAccessAt(LocalDateTime.now(ZoneOffset.UTC));
                    authSessionRepository.save(session);
                });
    }

    @Transactional
    public void cleanupExpiredSessions() {
        List<AuthSession> expiredSessions = authSessionRepository.findTop100ByStatusAndExpiresAtBefore(
                AuthSessionStatus.ACTIVE,
                LocalDateTime.now(ZoneOffset.UTC));
        if (expiredSessions.isEmpty()) {
            return;
        }

        for (AuthSession session : expiredSessions) {
            session.setStatus(AuthSessionStatus.EXPIRED);
            stringRedisTemplate.delete(buildSessionKey(session.getId()));
            stringRedisTemplate.opsForSet().remove(buildUserSessionsKey(session.getUserId()), session.getId().toString());
        }
        authSessionRepository.saveAll(expiredSessions);
    }

    private void cacheSessionVersion(UUID userId, long sessionVersion) {
        stringRedisTemplate.opsForValue().set(buildUserVersionKey(userId), Long.toString(sessionVersion));
    }

    private String buildSessionKey(UUID sessionId) {
        return "auth:refresh:" + sessionId;
    }

    private String buildUserSessionsKey(UUID userId) {
        return "auth:user:sessions:" + userId;
    }

    private String buildUserVersionKey(UUID userId) {
        return "auth:user:version:" + userId;
    }
}
