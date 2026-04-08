package com.aps.api.dto;

import com.aps.domain.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleDto {
    private UUID id;
    private String name;
    private String description;
    private List<PermissionDto> permissions;
    private Long userCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public static RoleDto fromEntity(Role role, Long userCount) {
        return RoleDto.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .permissions(role.getPermissions() != null ?
                        role.getPermissions().stream()
                                .map(PermissionDto::fromEntity)
                                .toList() : List.of())
                .userCount(userCount)
                .createTime(role.getCreateTime())
                .updateTime(role.getUpdateTime())
                .build();
    }

    public static RoleDto fromEntity(Role role) {
        return fromEntity(role, 0L);
    }
}
