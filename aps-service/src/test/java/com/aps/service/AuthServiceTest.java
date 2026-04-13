package com.aps.service;

import com.aps.domain.entity.Role;
import com.aps.domain.entity.User;
import com.aps.service.exception.TokenInvalidException;
import com.aps.service.repository.UserRepository;
import com.aps.service.security.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("认证服务测试")
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserService userService;

    @Mock
    private AuthSessionService authSessionService;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("登录成功时应创建多实例会话并返回双令牌")
    void login_whenValidCredentials_shouldCreateSessionAwareTokens() {
        UUID userId = UUID.randomUUID();
        User user = buildUser(userId, "planner");
        Authentication authentication = new UsernamePasswordAuthenticationToken("planner", "secret");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(userRepository.findByUsername("planner")).thenReturn(Optional.of(user));
        when(authSessionService.getOrCreateSessionVersion(userId)).thenReturn(3L);
        when(tokenProvider.generateAccessToken(eq(userId), eq("planner"), eq(List.of("PLANNER")), any(UUID.class), eq(3L)))
                .thenReturn("access-token");
        when(tokenProvider.generateRefreshToken(eq(userId), eq("planner"), any(UUID.class), eq(3L)))
                .thenReturn("refresh-token");
        when(tokenProvider.getTokenId("refresh-token")).thenReturn("refresh-jti");
        when(tokenProvider.getExpiration("refresh-token")).thenReturn(Instant.parse("2026-04-20T00:00:00Z"));

        AuthService.LoginResult result = authService.login("planner", "secret");

        assertThat(result.accessToken()).isEqualTo("access-token");
        assertThat(result.refreshToken()).isEqualTo("refresh-token");
        assertThat(result.user()).isSameAs(user);

        ArgumentCaptor<UUID> sessionIdCaptor = ArgumentCaptor.forClass(UUID.class);
        verify(tokenProvider).generateAccessToken(eq(userId), eq("planner"), eq(List.of("PLANNER")), sessionIdCaptor.capture(), eq(3L));
        UUID sessionId = sessionIdCaptor.getValue();
        verify(tokenProvider).generateRefreshToken(userId, "planner", sessionId, 3L);
        verify(authSessionService).createSession(sessionId, userId, "planner", "refresh-jti", 3L,
                Instant.parse("2026-04-20T00:00:00Z"));
        verify(userService).updateLastLoginTime(userId);
    }

    @Test
    @DisplayName("刷新令牌会话失效时应拒绝刷新")
    void refreshToken_whenSessionInvalid_shouldThrowUnauthorized() {
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();

        when(tokenProvider.validateToken("refresh-token")).thenReturn(true);
        when(tokenProvider.isRefreshToken("refresh-token")).thenReturn(true);
        when(tokenProvider.getUsernameFromToken("refresh-token")).thenReturn("planner");
        when(tokenProvider.getUserIdFromToken("refresh-token")).thenReturn(userId);
        when(tokenProvider.getSessionIdFromToken("refresh-token")).thenReturn(sessionId);
        when(tokenProvider.getSessionVersionFromToken("refresh-token")).thenReturn(4L);
        when(tokenProvider.getTokenId("refresh-token")).thenReturn("refresh-jti");
        doThrow(new TokenInvalidException("刷新令牌会话已失效"))
                .when(authSessionService).validateRefreshSession(userId, sessionId, "refresh-jti", 4L);

        assertThatThrownBy(() -> authService.refreshToken("refresh-token"))
                .isInstanceOf(TokenInvalidException.class)
                .hasMessageContaining("会话已失效");
    }

    @Test
    @DisplayName("登出时应撤销会话并拉黑当前访问令牌")
    void logout_shouldRevokeSessionAndBlacklistAccessToken() {
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();

        when(tokenProvider.validateToken("access-token")).thenReturn(true);
        when(tokenProvider.isRefreshToken("access-token")).thenReturn(false);
        when(tokenProvider.getTokenId("access-token")).thenReturn("access-jti");
        when(tokenProvider.getRemainingValidity("access-token")).thenReturn(Duration.ofMinutes(5));
        when(tokenProvider.validateToken("refresh-token")).thenReturn(true);
        when(tokenProvider.isRefreshToken("refresh-token")).thenReturn(true);
        when(tokenProvider.getUserIdFromToken("refresh-token")).thenReturn(userId);
        when(tokenProvider.getSessionIdFromToken("refresh-token")).thenReturn(sessionId);

        authService.logout("access-token", "refresh-token");

        verify(tokenBlacklistService).blacklist("access-jti", Duration.ofMinutes(5));
        verify(authSessionService).revokeSession(userId, sessionId);
    }

    @Test
    @DisplayName("登出全部设备时应提升会话版本并拉黑当前访问令牌")
    void logoutAll_shouldRevokeAllSessionsAndBlacklistCurrentToken() {
        UUID userId = UUID.randomUUID();

        when(tokenProvider.validateToken("access-token")).thenReturn(true);
        when(tokenProvider.isRefreshToken("access-token")).thenReturn(false);
        when(tokenProvider.getTokenId("access-token")).thenReturn("access-jti");
        when(tokenProvider.getRemainingValidity("access-token")).thenReturn(Duration.ofMinutes(10));

        authService.logoutAll(userId, "access-token");

        verify(authSessionService).revokeAllUserSessions(userId);
        verify(tokenBlacklistService).blacklist("access-jti", Duration.ofMinutes(10));
    }

    @Test
    @DisplayName("查询会话列表时应返回当前用户活动会话")
    void listSessions_shouldReturnActiveSessions() {
        UUID userId = UUID.randomUUID();
        UUID currentSessionId = UUID.randomUUID();
        AuthService.SessionView session = new AuthService.SessionView(
                currentSessionId,
                "planner",
                "WEB",
                "127.0.0.1",
                "Mozilla",
                LocalDateTime.of(2026, 4, 13, 16, 0),
                LocalDateTime.of(2026, 4, 20, 16, 0),
                LocalDateTime.of(2026, 4, 13, 16, 10),
                true
        );
        when(authSessionService.listActiveSessions(userId, currentSessionId)).thenReturn(List.of(session));
        when(tokenProvider.validateToken("access-token")).thenReturn(true);
        when(tokenProvider.isRefreshToken("access-token")).thenReturn(false);
        when(tokenProvider.getSessionIdFromToken("access-token")).thenReturn(currentSessionId);

        List<AuthService.SessionView> result = authService.listSessions(userId, "access-token");

        assertThat(result).containsExactly(session);
        verify(authSessionService).listActiveSessions(userId, currentSessionId);
    }

    @Test
    @DisplayName("撤销单设备会话时应撤销指定会话")
    void revokeSession_shouldRevokeTargetSession() {
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();

        authService.revokeSession(userId, sessionId);

        verify(authSessionService).revokeSession(userId, sessionId);
    }

    private User buildUser(UUID userId, String username) {
        Role role = new Role();
        role.setName("PLANNER");

        User user = new User();
        user.setId(userId);
        user.setUsername(username);
        user.setEnabled(true);
        user.setRoles(List.of(role));
        return user;
    }
}
