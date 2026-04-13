package com.aps.service;

import com.aps.domain.entity.AuthSession;
import com.aps.domain.enums.AuthSessionStatus;
import com.aps.service.repository.AuthSessionRepository;
import com.aps.service.repository.AuthUserSessionStateRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("认证会话服务测试")
class AuthSessionServiceTest {

    @Mock
    private AuthSessionRepository authSessionRepository;

    @Mock
    private AuthUserSessionStateRepository authUserSessionStateRepository;

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private HashOperations<String, Object, Object> hashOperations;

    @Mock
    private SetOperations<String, String> setOperations;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private AuthSessionService authSessionService;

    @Test
    @DisplayName("清理过期会话时应标记为EXPIRED并删除Redis索引")
    void cleanupExpiredSessions_shouldExpireSessionsAndRemoveRedisIndexes() {
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        AuthSession session = new AuthSession();
        session.setId(sessionId);
        session.setUserId(userId);
        session.setUsername("planner");
        session.setStatus(AuthSessionStatus.ACTIVE);
        session.setExpiresAt(LocalDateTime.now().minusMinutes(5));

        when(authSessionRepository.findTop100ByStatusAndExpiresAtBefore(eq(AuthSessionStatus.ACTIVE), any(LocalDateTime.class)))
                .thenReturn(List.of(session));
        when(stringRedisTemplate.opsForSet()).thenReturn(setOperations);

        authSessionService.cleanupExpiredSessions();

        ArgumentCaptor<List<AuthSession>> captor = ArgumentCaptor.forClass(List.class);
        verify(authSessionRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).singleElement().extracting(AuthSession::getStatus).isEqualTo(AuthSessionStatus.EXPIRED);
        verify(stringRedisTemplate).delete("auth:refresh:" + sessionId);
        verify(setOperations).remove("auth:user:sessions:" + userId, sessionId.toString());
    }

    @Test
    @DisplayName("没有过期会话时不应触发保存")
    void cleanupExpiredSessions_whenNoExpiredSession_shouldDoNothing() {
        when(authSessionRepository.findTop100ByStatusAndExpiresAtBefore(eq(AuthSessionStatus.ACTIVE), any(LocalDateTime.class)))
                .thenReturn(List.of());

        authSessionService.cleanupExpiredSessions();

        verify(authSessionRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("更新最近访问时间时应回写活动会话")
    void touchSession_shouldUpdateLastAccessTime() {
        UUID sessionId = UUID.randomUUID();
        LocalDateTime previousAccessTime = LocalDateTime.now(ZoneOffset.UTC).minusHours(1);
        AuthSession session = new AuthSession();
        session.setId(sessionId);
        session.setStatus(AuthSessionStatus.ACTIVE);
        session.setLastAccessAt(previousAccessTime);
        when(authSessionRepository.findById(sessionId)).thenReturn(java.util.Optional.of(session));

        authSessionService.touchSession(sessionId);

        verify(authSessionRepository).save(session);
        assertThat(session.getLastAccessAt()).isAfter(previousAccessTime);
    }
}
