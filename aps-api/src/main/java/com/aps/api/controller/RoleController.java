package com.aps.api.controller;

import com.aps.api.dto.AjaxResult;
import com.aps.api.dto.RoleDto;
import com.aps.api.dto.RoleRequest;
import com.aps.domain.entity.Role;
import com.aps.service.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public AjaxResult<Page<RoleDto>> getAllRoles(Pageable pageable) {
        Page<Role> roles = roleService.getAllRoles(pageable);
        return AjaxResult.success(roles.map(r -> RoleDto.fromEntity(r, roleService.getUserCountByRole(r.getId()))));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public AjaxResult<RoleDto> getRole(@PathVariable UUID id) {
        Role role = roleService.getRoleById(id);
        return AjaxResult.success(RoleDto.fromEntity(role, roleService.getUserCountByRole(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public AjaxResult<RoleDto> createRole(@Valid @RequestBody RoleRequest request) {
        Role role = roleService.createRole(request.getName(), request.getDescription());
        if (request.getPermissionIds() != null && !request.getPermissionIds().isEmpty()) {
            roleService.assignPermissions(role.getId(), request.getPermissionIds());
            role = roleService.getRoleById(role.getId());
        }
        return AjaxResult.success(RoleDto.fromEntity(role));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public AjaxResult<RoleDto> updateRole(@PathVariable UUID id, @Valid @RequestBody RoleRequest request) {
        Role role = roleService.updateRole(id, request.getDescription());
        if (request.getPermissionIds() != null) {
            roleService.assignPermissions(id, request.getPermissionIds());
            role = roleService.getRoleById(id);
        }
        return AjaxResult.success(RoleDto.fromEntity(role));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public AjaxResult<Void> deleteRole(@PathVariable UUID id) {
        roleService.deleteRole(id);
        return AjaxResult.success(null);
    }

    @PostMapping("/{id}/permissions")
    @PreAuthorize("hasRole('ADMIN')")
    public AjaxResult<RoleDto> assignPermissions(@PathVariable UUID id, @RequestBody List<UUID> permissionIds) {
        roleService.assignPermissions(id, permissionIds);
        Role role = roleService.getRoleById(id);
        return AjaxResult.success(RoleDto.fromEntity(role));
    }

    @GetMapping("/{id}/permissions")
    @PreAuthorize("hasRole('ADMIN')")
    public AjaxResult<List<UUID>> getRolePermissions(@PathVariable UUID id) {
        return AjaxResult.success(roleService.getRolePermissions(id).stream().map(p -> p.getId()).toList());
    }

    @DeleteMapping("/batch")
    @PreAuthorize("hasRole('ADMIN')")
    public AjaxResult<Void> deleteRoles(@RequestBody List<UUID> roleIds) {
        roleService.deleteRoles(roleIds);
        return AjaxResult.success(null);
    }

    @PostMapping("/batch/permissions")
    @PreAuthorize("hasRole('ADMIN')")
    public AjaxResult<Void> assignPermissionsToRoles(@RequestBody BatchPermissionRequest request) {
        roleService.assignPermissionsToRoles(request.getRoleIds(), request.getPermissionIds());
        return AjaxResult.success(null);
    }

    @lombok.Data
    public static class BatchPermissionRequest {
        private List<UUID> roleIds;
        private List<UUID> permissionIds;
    }
}
