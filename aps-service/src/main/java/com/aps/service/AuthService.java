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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    /**
     * 用户登录
     */
    @Transactional
    public LoginResult login(String username, String password) {
        try {
            // 验证用户名和密码
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );

            // 获取用户信息
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));

            // 提取角色列表
            List<String> roles = user.getRoles().stream()
                    .map(Role::getName)
                    .collect(Collectors.toList());

            // 生成 JWT tokens
            String accessToken = tokenProvider.generateAccessToken(user.getId(), username, roles);
            String refreshToken = tokenProvider.generateRefreshToken(user.getId(), username);

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
        String newAccessToken = tokenProvider.generateAccessToken(userId, username, roles);

        log.debug("刷新令牌成功: {}", username);
        return new LoginResult(newAccessToken, refreshToken, user);
    }

    /**
     * 验证令牌
     */
    public boolean validateToken(String token) {
        return tokenProvider.validateToken(token);
    }

    /**
     * 登录结果
     */
    public record LoginResult(String accessToken, String refreshToken, User user) {
    }
}
