package com.aps.service;

import com.aps.service.exception.InvalidCredentialsException;
import com.aps.service.exception.ResourceNotFoundException;
import com.aps.service.exception.TokenInvalidException;
import com.aps.service.exception.ForbiddenException;
import com.aps.service.security.JwtTokenProvider;
import com.aps.domain.entity.Role;
import com.aps.domain.entity.User;
import com.aps.service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final UserService userService;
    private final AuthSessionService authSessionService;
    private final TokenBlacklistService tokenBlacklistService;

    /**
     * 用户登录
     */
    @Transactional
    public LoginResult login(String username, String password) {
        try {
            // 验证用户名和密码
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );

            // 获取用户信息
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));

            // 提取角色列表
            List<String> roles = user.getRoles().stream()
                    .map(Role::getName)
                    .collect(Collectors.toList());

            long sessionVersion = authSessionService.getOrCreateSessionVersion(user.getId());
            UUID sessionId = UUID.randomUUID();

            // 生成 JWT tokens
            String accessToken = tokenProvider.generateAccessToken(user.getId(), username, roles, sessionId, sessionVersion);
            String refreshToken = tokenProvider.generateRefreshToken(user.getId(), username, sessionId, sessionVersion);
            authSessionService.createSession(sessionId, user.getId(), username, tokenProvider.getTokenId(refreshToken),
                    sessionVersion, tokenProvider.getExpiration(refreshToken));

            // 更新最后登录时间
            userService.updateLastLoginTime(user.getId());

            log.info("用户登录成功: {}", username);
            return new LoginResult(accessToken, refreshToken, user);

        } catch (AuthenticationException e) {
            log.warn("用户登录失败: {} - {}", username, e.getMessage());
            throw new InvalidCredentialsException("用户名或密码错误");
        }
    }

    /**
     * 刷新访问令牌
     */
    @Transactional(readOnly = true)
    public LoginResult refreshToken(String refreshToken) {
        if (!tokenProvider.validateToken(refreshToken)) {
            throw new TokenInvalidException("刷新令牌无效");
        }

        if (!tokenProvider.isRefreshToken(refreshToken)) {
            throw new TokenInvalidException("不是有效的刷新令牌");
        }

        String username = tokenProvider.getUsernameFromToken(refreshToken);
        UUID userId = tokenProvider.getUserIdFromToken(refreshToken);
        UUID sessionId = tokenProvider.getSessionIdFromToken(refreshToken);
        long sessionVersion = tokenProvider.getSessionVersionFromToken(refreshToken);
        authSessionService.validateRefreshSession(userId, sessionId, tokenProvider.getTokenId(refreshToken), sessionVersion);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));

        if (!user.getEnabled()) {
            throw new ForbiddenException("用户已被禁用");
        }

        // 提取角色列表
        List<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        // 生成新的访问令牌
        String newAccessToken = tokenProvider.generateAccessToken(userId, username, roles, sessionId, sessionVersion);

        log.debug("刷新令牌成功: {}", username);
        return new LoginResult(newAccessToken, refreshToken, user);
    }

    /**
     * 验证令牌
     */
    public boolean validateToken(String token) {
        return tokenProvider.validateToken(token);
    }

    @Transactional
    public void logout(String accessToken, String refreshToken) {
        blacklistAccessToken(accessToken);
        revokeRefreshSession(refreshToken);
    }

    @Transactional
    public void logoutAll(UUID userId, String accessToken) {
        blacklistAccessToken(accessToken);
        authSessionService.revokeAllUserSessions(userId);
    }

    @Transactional(readOnly = true)
    public List<SessionView> listSessions(UUID userId, String accessToken) {
        UUID currentSessionId = extractCurrentSessionId(accessToken);
        return authSessionService.listActiveSessions(userId, currentSessionId);
    }

    @Transactional
    public void revokeSession(UUID userId, UUID sessionId) {
        authSessionService.revokeSession(userId, sessionId);
    }

    public boolean isCurrentSession(String accessToken, UUID sessionId) {
        UUID currentSessionId = extractCurrentSessionId(accessToken);
        return sessionId.equals(currentSessionId);
    }

    private void blacklistAccessToken(String accessToken) {
        if (accessToken == null || !tokenProvider.validateToken(accessToken) || tokenProvider.isRefreshToken(accessToken)) {
            return;
        }
        Duration remainingValidity = tokenProvider.getRemainingValidity(accessToken);
        tokenBlacklistService.blacklist(tokenProvider.getTokenId(accessToken), remainingValidity);
    }

    private void revokeRefreshSession(String refreshToken) {
        if (refreshToken == null || !tokenProvider.validateToken(refreshToken) || !tokenProvider.isRefreshToken(refreshToken)) {
            return;
        }
        authSessionService.revokeSession(tokenProvider.getUserIdFromToken(refreshToken),
                tokenProvider.getSessionIdFromToken(refreshToken));
    }

    private UUID extractCurrentSessionId(String accessToken) {
        if (accessToken == null || !tokenProvider.validateToken(accessToken) || tokenProvider.isRefreshToken(accessToken)) {
            return null;
        }
        return tokenProvider.getSessionIdFromToken(accessToken);
    }

    /**
     * 登录结果
     */
    public record LoginResult(String accessToken, String refreshToken, User user) {
    }

    public record SessionView(
            UUID sessionId,
            String username,
            String clientType,
            String clientIp,
            String userAgent,
            LocalDateTime createTime,
            LocalDateTime expiresAt,
            LocalDateTime lastAccessAt,
            boolean current
    ) {
    }
}
