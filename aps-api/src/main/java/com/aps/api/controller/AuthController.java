package com.aps.api.controller;

import com.aps.api.dto.AjaxResult;
import com.aps.api.dto.LoginRequest;
import com.aps.api.dto.LoginResponse;
import com.aps.api.dto.UserDto;
import com.aps.api.security.UserPrincipal;
import com.aps.domain.entity.User;
import com.aps.domain.enums.AuditAction;
import com.aps.service.AuditService;
import com.aps.service.AuthService;
import com.aps.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final AuditService auditService;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Value("${jwt.cookie.secure:false}")
    private boolean cookieSecure;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public AjaxResult<LoginResponse> login(@Valid @RequestBody LoginRequest request,
                                           HttpServletRequest httpRequest,
                                           HttpServletResponse httpResponse) {
        AuthService.LoginResult result = authService.login(request.username(), request.password());

        // 设置 httpOnly cookies
        setAuthCookies(httpResponse, result.accessToken(), result.refreshToken());

        // 记录审计日志
        String ipAddress = getClientIpAddress(httpRequest);
        auditService.logAudit(
                result.user().getId(),
                result.user().getUsername(),
                AuditAction.LOGIN,
                "auth",
                "用户登录成功",
                ipAddress
        );

        UserDto userDto = UserDto.fromEntity(result.user());
        LoginResponse response = new LoginResponse(userDto, jwtExpiration);

        return AjaxResult.success(response);
    }

    /**
     * 刷新访问令牌
     */
    @PostMapping("/refresh")
    public AjaxResult<LoginResponse> refresh(@CookieValue(name = "refreshToken", required = false) String refreshToken,
                                             HttpServletResponse httpResponse) {
        if (refreshToken == null) {
            return AjaxResult.error(401, "刷新令牌缺失", null);
        }

        AuthService.LoginResult result = authService.refreshToken(refreshToken);

        // 设置新的访问令牌 cookie
        setAccessTokenCookie(httpResponse, result.accessToken());

        UserDto userDto = UserDto.fromEntity(result.user());
        LoginResponse response = new LoginResponse(userDto, jwtExpiration);

        return AjaxResult.success(response);
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public AjaxResult<Void> logout(HttpServletRequest httpRequest,
                                   HttpServletResponse httpResponse) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        authService.logout(getCookieValue(httpRequest, "accessToken"), getCookieValue(httpRequest, "refreshToken"));
        clearAuthCookies(httpResponse);

        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();

            // 记录审计日志
            String ipAddress = getClientIpAddress(httpRequest);
            auditService.logAudit(
                    null,
                    username,
                    AuditAction.LOGOUT,
                    "auth",
                    "用户登出",
                    ipAddress
            );

            log.info("用户登出: {}", username);
        }

        return AjaxResult.success();
    }

    @PostMapping("/logout-all")
    public AjaxResult<Void> logoutAll(HttpServletRequest httpRequest,
                                      HttpServletResponse httpResponse) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        clearAuthCookies(httpResponse);

        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserPrincipal userPrincipal) {
                authService.logoutAll(userPrincipal.getId(), getCookieValue(httpRequest, "accessToken"));

                String ipAddress = getClientIpAddress(httpRequest);
                auditService.logAudit(
                        userPrincipal.getId(),
                        userPrincipal.getUsername(),
                        AuditAction.LOGOUT,
                        "auth",
                        "用户登出全部设备",
                        ipAddress
                );

                log.info("用户登出全部设备: {}", userPrincipal.getUsername());
            }
        }

        return AjaxResult.success();
    }

    @GetMapping("/sessions")
    public AjaxResult<List<SessionResponse>> listSessions(HttpServletRequest httpRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return AjaxResult.error(401, "未认证", null);
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UserPrincipal userPrincipal)) {
            return AjaxResult.error(401, "未认证", null);
        }

        List<SessionResponse> sessions = authService.listSessions(
                        userPrincipal.getId(),
                        getCookieValue(httpRequest, "accessToken"))
                .stream()
                .map(SessionResponse::from)
                .toList();
        return AjaxResult.success(sessions);
    }

    @DeleteMapping("/sessions/{sessionId}")
    public AjaxResult<Void> revokeSession(@PathVariable UUID sessionId,
                                          HttpServletRequest httpRequest,
                                          HttpServletResponse httpResponse) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return AjaxResult.error(401, "未认证", null);
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UserPrincipal userPrincipal)) {
            return AjaxResult.error(401, "未认证", null);
        }

        String accessToken = getCookieValue(httpRequest, "accessToken");
        authService.revokeSession(userPrincipal.getId(), sessionId);
        if (authService.isCurrentSession(accessToken, sessionId)) {
            clearAuthCookies(httpResponse);
        }

        return AjaxResult.success();
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/me")
    public AjaxResult<UserDto> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return AjaxResult.error(401, "未认证", null);
        }

        String username = authentication.getName();
        User user = userService.getUserByUsername(username);
        UserDto userDto = UserDto.fromEntity(user);

        return AjaxResult.success(userDto);
    }

    /**
     * 设置认证 cookies
     */
    private void setAuthCookies(HttpServletResponse response, String accessToken, String refreshToken) {
        setAccessTokenCookie(response, accessToken);
        setRefreshTokenCookie(response, refreshToken);
    }

    /**
     * 设置访问令牌 cookie
     */
    private void setAccessTokenCookie(HttpServletResponse response, String accessToken) {
        Cookie cookie = new Cookie("accessToken", accessToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(cookieSecure);
        cookie.setPath("/");
        cookie.setMaxAge((int) (jwtExpiration / 1000)); // 转换为秒
        response.addCookie(cookie);
    }

    /**
     * 设置刷新令牌 cookie
     */
    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(cookieSecure);
        cookie.setPath("/");
        cookie.setMaxAge((int) (refreshExpiration / 1000));
        response.addCookie(cookie);
    }

    /**
     * 清除认证 cookies
     */
    private void clearAuthCookies(HttpServletResponse response) {
        Cookie accessTokenCookie = new Cookie("accessToken", null);
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(0);
        response.addCookie(accessTokenCookie);

        Cookie refreshTokenCookie = new Cookie("refreshToken", null);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(0);
        response.addCookie(refreshTokenCookie);
    }

    private String getCookieValue(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (cookieName.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    /**
     * 获取客户端 IP 地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if ("0:0:0:0:0:0:0:1".equals(ip)) {
            return "127.0.0.1";
        }
        return ip;
    }

    public record SessionResponse(
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
        private static SessionResponse from(AuthService.SessionView session) {
            return new SessionResponse(
                    session.sessionId(),
                    session.username(),
                    session.clientType(),
                    session.clientIp(),
                    session.userAgent(),
                    session.createTime(),
                    session.expiresAt(),
                    session.lastAccessAt(),
                    session.current()
            );
        }
    }
}
