package com.aps.api.dto;

public record LoginResponse(
    UserDto user,
    long expiresIn  // 访问令牌过期时间（毫秒）
) {
}
