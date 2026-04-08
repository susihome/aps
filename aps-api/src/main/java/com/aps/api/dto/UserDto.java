package com.aps.api.dto;

import com.aps.domain.entity.Permission;
import com.aps.domain.entity.Role;
import com.aps.domain.entity.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public record UserDto(
    UUID id,
    String username,
    String email,
    List<String> roles,
    List<String> permissions,
    Boolean enabled,
    LocalDateTime createTime,
    LocalDateTime lastLoginAt
) {
    public static UserDto fromEntity(User user) {
        List<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList());

        List<String> permissions = user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(Permission::getCode)
                .distinct()
                .collect(Collectors.toList());

        return new UserDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                roles,
                permissions,
                user.getEnabled(),
                user.getCreateTime(),
                user.getLastLoginAt()
        );
    }
}
