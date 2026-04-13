package com.aps.api.controller;

import com.aps.api.exception.GlobalExceptionHandler;
import com.aps.api.security.UserPrincipal;
import com.aps.domain.entity.User;
import com.aps.domain.enums.AuditAction;
import com.aps.service.AuditService;
import com.aps.service.AuthService;
import com.aps.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.core.authority.AuthorityUtils.createAuthorityList;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("认证控制器测试")
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private UserService userService;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private AuthController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(controller, "jwtExpiration", 3600000L);
        ReflectionTestUtils.setField(controller, "refreshExpiration", 604800000L);
        ReflectionTestUtils.setField(controller, "cookieSecure", false);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("登录成功时应写入访问令牌和刷新令牌 Cookie")
    void login_shouldSetAuthCookies() throws Exception {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("planner");
        user.setEnabled(true);
        user.setRoles(List.of());
        when(authService.login("planner", "secret"))
                .thenReturn(new AuthService.LoginResult("access-token", "refresh-token", user));

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("""
                                {"username":"planner","password":"secret"}
                                """))
                .andExpect(status().isOk())
                .andExpect(cookie().value("accessToken", "access-token"))
                .andExpect(cookie().value("refreshToken", "refresh-token"))
                .andExpect(jsonPath("$.data.expiresIn").value(3600000));

        verify(auditService).logAudit(user.getId(), "planner", AuditAction.LOGIN, "auth", "用户登录成功", "127.0.0.1");
    }

    @Test
    @DisplayName("登出时应清理 Cookie 并撤销当前会话")
    void logout_shouldClearCookiesAndRevokeCurrentSession() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("planner", null, "ROLE_PLANNER"));

        mockMvc.perform(post("/api/auth/logout")
                        .cookie(new jakarta.servlet.http.Cookie("accessToken", "access-token"))
                        .cookie(new jakarta.servlet.http.Cookie("refreshToken", "refresh-token")))
                .andExpect(status().isOk())
                .andExpect(cookie().maxAge("accessToken", 0))
                .andExpect(cookie().maxAge("refreshToken", 0));

        verify(authService).logout("access-token", "refresh-token");
        verify(auditService).logAudit(null, "planner", AuditAction.LOGOUT, "auth", "用户登出", "127.0.0.1");
    }

    @Test
    @DisplayName("登出全部设备时应撤销用户全部会话")
    void logoutAll_shouldRevokeAllSessions() throws Exception {
        UUID userId = UUID.randomUUID();
        UserPrincipal principal = new UserPrincipal(userId, "planner", "", createAuthorityList("ROLE_PLANNER"));
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken(principal, null, principal.getAuthorities()));

        mockMvc.perform(post("/api/auth/logout-all")
                        .cookie(new jakarta.servlet.http.Cookie("accessToken", "access-token")))
                .andExpect(status().isOk())
                .andExpect(cookie().maxAge("accessToken", 0))
                .andExpect(cookie().maxAge("refreshToken", 0));

        verify(authService).logoutAll(userId, "access-token");
        verify(auditService).logAudit(userId, "planner", AuditAction.LOGOUT, "auth", "用户登出全部设备", "127.0.0.1");
    }

    @Test
    @DisplayName("获取当前用户信息时应返回当前用户")
    void getCurrentUser_shouldReturnUser() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("planner", null, "ROLE_PLANNER"));
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("planner");
        user.setEnabled(true);
        user.setRoles(List.of());
        when(userService.getUserByUsername("planner")).thenReturn(user);

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value("planner"));
    }

    @Test
    @DisplayName("查询当前用户会话列表时应返回活动会话")
    void listSessions_shouldReturnActiveSessions() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        UserPrincipal principal = new UserPrincipal(userId, "planner", "", createAuthorityList("ROLE_PLANNER"));
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken(principal, null, principal.getAuthorities()));
        when(authService.listSessions(userId, "access-token")).thenReturn(List.of(
                new AuthService.SessionView(
                        sessionId,
                        "planner",
                        "WEB",
                        "127.0.0.1",
                        "Mozilla",
                        java.time.LocalDateTime.of(2026, 4, 13, 16, 0),
                        java.time.LocalDateTime.of(2026, 4, 20, 16, 0),
                        java.time.LocalDateTime.of(2026, 4, 13, 16, 10),
                        true
                )
        ));

        mockMvc.perform(get("/api/auth/sessions")
                        .cookie(new jakarta.servlet.http.Cookie("accessToken", "access-token")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].sessionId").value(sessionId.toString()))
                .andExpect(jsonPath("$.data[0].current").value(true));
    }

    @Test
    @DisplayName("撤销指定会话时应调用服务并在撤销当前会话时清理 Cookie")
    void revokeSession_shouldClearCookiesWhenRevokingCurrentSession() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        UserPrincipal principal = new UserPrincipal(userId, "planner", "", createAuthorityList("ROLE_PLANNER"));
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken(principal, null, principal.getAuthorities()));
        when(authService.isCurrentSession("access-token", sessionId)).thenReturn(true);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/api/auth/sessions/{sessionId}", sessionId)
                        .cookie(new jakarta.servlet.http.Cookie("accessToken", "access-token")))
                .andExpect(status().isOk())
                .andExpect(cookie().maxAge("accessToken", 0))
                .andExpect(cookie().maxAge("refreshToken", 0));

        verify(authService).revokeSession(userId, sessionId);
    }
}
