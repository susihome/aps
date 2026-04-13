package com.aps.api.security;

import com.aps.service.AuthSessionService;
import com.aps.service.TokenBlacklistService;
import com.aps.service.security.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;

import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("JWT 认证过滤器测试")
class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private AuthSessionService authSessionService;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("访问令牌有效时应刷新会话最近访问时间")
    void doFilterInternal_shouldTouchSessionWhenTokenIsValid() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("accessToken", "access-token"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(tokenProvider.validateToken("access-token")).thenReturn(true);
        when(tokenProvider.isRefreshToken("access-token")).thenReturn(false);
        when(tokenProvider.getUserIdFromToken("access-token")).thenReturn(userId);
        when(tokenProvider.getSessionVersionFromToken("access-token")).thenReturn(1L);
        when(tokenProvider.getTokenId("access-token")).thenReturn("access-jti");
        when(tokenBlacklistService.isBlacklisted("access-jti")).thenReturn(false);
        when(authSessionService.isSessionVersionValid(userId, 1L)).thenReturn(true);
        when(tokenProvider.getUsernameFromToken("access-token")).thenReturn("planner");
        when(tokenProvider.getSessionIdFromToken("access-token")).thenReturn(sessionId);
        when(userDetailsService.loadUserByUsername("planner"))
                .thenReturn(new User("planner", "", AuthorityUtils.createAuthorityList("ROLE_PLANNER")));

        jwtAuthenticationFilter.doFilter(
                request,
                response,
                new org.springframework.mock.web.MockFilterChain()
        );

        verify(authSessionService).touchSession(sessionId);
    }
}
