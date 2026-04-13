package com.aps.api.security;

import com.aps.service.AuthSessionService;
import com.aps.service.TokenBlacklistService;
import com.aps.service.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtTokenProvider tokenProvider;
    private final AuthSessionService authSessionService;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = accessor.getFirstNativeHeader("Authorization");

            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);

                if (tokenProvider.validateToken(token) && !tokenProvider.isRefreshToken(token)) {
                    String username = tokenProvider.getUsernameFromToken(token);
                    UUID userId = tokenProvider.getUserIdFromToken(token);
                    long sessionVersion = tokenProvider.getSessionVersionFromToken(token);
                    if (tokenBlacklistService.isBlacklisted(tokenProvider.getTokenId(token))
                            || !authSessionService.isSessionVersionValid(userId, sessionVersion)) {
                        log.warn("WebSocket 连接认证失败: 会话已失效");
                        return message;
                    }
                    List<String> roles = tokenProvider.getRolesFromToken(token);

                    List<SimpleGrantedAuthority> authorities = roles.stream()
                            .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                            .collect(Collectors.toList());

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(username, null, authorities);

                    accessor.setUser(authentication);
                    log.debug("WebSocket 连接认证成功: {}", username);
                } else {
                    log.warn("WebSocket 连接认证失败: 无效的 token");
                }
            }
        }

        return message;
    }
}
