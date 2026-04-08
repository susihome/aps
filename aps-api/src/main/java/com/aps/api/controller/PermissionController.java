package com.aps.api.controller;

import com.aps.api.dto.AjaxResult;
import com.aps.api.dto.PermissionDto;
import com.aps.domain.entity.Permission;
import com.aps.service.PermissionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    /**
     * 获取权限树
     */
    @GetMapping("/tree")
    @PreAuthorize("hasAuthority('system:permission:list')")
    public AjaxResult<List<PermissionDto>> getPermissionTree() {
        List<Permission> tree = permissionService.getPermissionTree();
        return AjaxResult.success(tree.stream().map(PermissionDto::fromEntity).toList());
    }

    /**
     * 获取单个权限
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('system:permission:query')")
    public AjaxResult<PermissionDto> getPermission(@PathVariable UUID id) {
        Permission permission = permissionService.getPermission(id);
        return AjaxResult.success(PermissionDto.fromEntity(permission));
    }

    /**
     * 创建权限
     */
    @PostMapping
    @PreAuthorize("hasAuthority('system:permission:add')")
    public AjaxResult<PermissionDto> createPermission(@Valid @RequestBody PermissionRequest request) {
        Permission permission = toEntity(request);
        Permission created = permissionService.createPermission(permission);
        return AjaxResult.success(PermissionDto.fromEntity(created));
    }

    /**
     * 更新权限
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('system:permission:edit')")
    public AjaxResult<PermissionDto> updatePermission(@PathVariable UUID id, @Valid @RequestBody PermissionRequest request) {
        Permission updates = toEntity(request);
        Permission updated = permissionService.updatePermission(id, updates);
        return AjaxResult.success(PermissionDto.fromEntity(updated));
    }

    /**
     * 删除权限
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:permission:remove')")
    public AjaxResult<Void> deletePermission(@PathVariable UUID id) {
        permissionService.deletePermission(id);
        return AjaxResult.success(null);
    }

    /**
     * 启用/禁用权限
     */
    @PatchMapping("/{id}/toggle")
    @PreAuthorize("hasAuthority('system:permission:edit')")
    public AjaxResult<PermissionDto> togglePermission(@PathVariable UUID id, @RequestBody Map<String, Boolean> body) {
        Boolean enabled = body.get("enabled");
        if (enabled == null) {
            return AjaxResult.error(400, "enabled 参数不能为空", null);
        }
        Permission updated = permissionService.togglePermission(id, enabled);
        return AjaxResult.success(PermissionDto.fromEntity(updated));
    }

    /**
     * 批量更新排序
     */
    @PostMapping("/batch-sort")
    @PreAuthorize("hasAuthority('system:permission:edit')")
    public AjaxResult<Void> updateSort(@RequestBody BatchSortRequest request) {
        permissionService.updateSort(request.getUpdates());
        return AjaxResult.success(null);
    }

    /**
     * 获取所有启用的权限
     */
    @GetMapping("/enabled")
    @PreAuthorize("hasAuthority('system:permission:list')")
    public AjaxResult<List<PermissionDto>> getAllEnabled() {
        List<Permission> permissions = permissionService.getAllEnabled();
        return AjaxResult.success(permissions.stream().map(PermissionDto::fromEntity).toList());
    }

    // DTO转Entity
    private Permission toEntity(PermissionRequest request) {
        Permission permission = new Permission();
        permission.setCode(request.getCode());
        permission.setName(request.getName());
        permission.setDescription(request.getDescription());
        permission.setType(request.getType());
        permission.setRoutePath(request.getRoutePath());
        permission.setIcon(request.getIcon());
        permission.setSort(request.getSort() != null ? request.getSort() : 0);
        permission.setEnabled(request.getEnabled() != null ? request.getEnabled() : true);
        permission.setVisible(request.getVisible() != null ? request.getVisible() : true);

        if (request.getParentId() != null) {
            Permission parent = new Permission();
            parent.setId(request.getParentId());
            permission.setParent(parent);
        }

        return permission;
    }

    // 请求DTO
    public static class PermissionRequest {
        @NotBlank(message = "权限编码不能为空")
        @Size(min = 3, max = 100, message = "权限编码长度3-100字符")
        @Pattern(regexp = "^[a-zA-Z0-9:_-]+$", message = "权限编码只能包含字母、数字、冒号、下划线和连字符")
        private String code;

        @NotBlank(message = "权限名称不能为空")
        @Size(min = 1, max = 100, message = "权限名称长度1-100字符")
        private String name;

        @Size(max = 255, message = "权限描述长度不能超过255字符")
        private String description;

        @NotNull(message = "权限类型不能为空")
        private Permission.PermissionType type;

        @Size(max = 255, message = "路由路径长度不能超过255字符")
        private String routePath;

        @NotBlank(message = "图标不能为空")
        @Size(max = 50, message = "图标名称长度不能超过50字符")
        private String icon;

        @NotNull(message = "排序号不能为空")
        private Integer sort;

        private Boolean enabled;
        private Boolean visible;
        private UUID parentId;

        // Getters and Setters
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public Permission.PermissionType getType() { return type; }
        public void setType(Permission.PermissionType type) { this.type = type; }

        public String getRoutePath() { return routePath; }
        public void setRoutePath(String routePath) { this.routePath = routePath; }

        public String getIcon() { return icon; }
        public void setIcon(String icon) { this.icon = icon; }

        public Integer getSort() { return sort; }
        public void setSort(Integer sort) { this.sort = sort; }

        public Boolean getEnabled() { return enabled; }
        public void setEnabled(Boolean enabled) { this.enabled = enabled; }

        public Boolean getVisible() { return visible; }
        public void setVisible(Boolean visible) { this.visible = visible; }

        public UUID getParentId() { return parentId; }
        public void setParentId(UUID parentId) { this.parentId = parentId; }
    }

    // 批量排序请求DTO
    public static class BatchSortRequest {
        private List<PermissionService.SortUpdate> updates;

        public List<PermissionService.SortUpdate> getUpdates() { return updates; }
        public void setUpdates(List<PermissionService.SortUpdate> updates) { this.updates = updates; }
    }
}
