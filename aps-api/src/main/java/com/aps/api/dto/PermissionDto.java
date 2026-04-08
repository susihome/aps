package com.aps.api.dto;

import com.aps.domain.entity.Permission;

import java.util.List;
import java.util.UUID;

public record PermissionDto(
        UUID id,
        String code,
        String name,
        String description,
        String type,
        String routePath,
        String icon,
        Integer sort,
        Boolean enabled,
        Boolean visible,
        UUID parentId,
        List<PermissionDto> children
) {
    public static PermissionDto fromEntity(Permission permission) {
        return new PermissionDto(
                permission.getId(),
                permission.getCode(),
                permission.getName(),
                permission.getDescription(),
                permission.getType() != null ? permission.getType().name() : null,
                permission.getRoutePath(),
                permission.getIcon(),
                permission.getSort(),
                permission.getEnabled(),
                permission.getVisible(),
                permission.getParent() != null ? permission.getParent().getId() : null,
                permission.getChildren() != null
                        ? permission.getChildren().stream().map(PermissionDto::fromEntity).toList()
                        : List.of()
        );
    }
}
