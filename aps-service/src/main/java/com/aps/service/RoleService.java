package com.aps.service;

import com.aps.domain.annotation.Audited;
import com.aps.domain.entity.Permission;
import com.aps.domain.entity.Role;
import com.aps.domain.enums.AuditAction;
import com.aps.service.exception.ResourceConflictException;
import com.aps.service.exception.ResourceNotFoundException;
import com.aps.service.repository.PermissionRepository;
import com.aps.service.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    @Transactional(readOnly = true)
    public Page<Role> getAllRoles(Pageable pageable) {
        return roleRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public List<Role> getAllRolesWithPermissions() {
        return roleRepository.findAllWithPermissions();
    }

    @Transactional(readOnly = true)
    public Role getRoleById(UUID id) {
        return roleRepository.findByIdWithPermissions(id)
                .orElseThrow(() -> new ResourceNotFoundException("角色不存在: " + id));
    }

    @Transactional
    @Audited(action = AuditAction.CREATE, resource = "Role")
    public Role createRole(String name, String description) {
        String normalizedName = name == null ? "" : name.trim();

        if (normalizedName.isEmpty()) {
            throw new ResourceConflictException("角色名称不能为空");
        }

        if (roleRepository.existsByName(normalizedName)) {
            throw new ResourceConflictException("角色已存在: " + normalizedName);
        }

        Role role = new Role();
        role.setName(normalizedName);
        role.setDescription(description);
        return roleRepository.save(role);
    }

    @Transactional
    @Audited(action = AuditAction.UPDATE, resource = "Role")
    public Role updateRole(UUID id, String description) {
        Role role = getRoleById(id);
        role.setDescription(description);
        return roleRepository.save(role);
    }

    @Transactional
    @Audited(action = AuditAction.DELETE, resource = "Role")
    public void deleteRole(UUID id) {
        Role role = getRoleById(id);
        long userCount = roleRepository.countUsersByRoleId(id);
        if (userCount > 0) {
            throw new ResourceConflictException("角色已被用户使用，无法删除");
        }
        roleRepository.delete(role);
    }

    @Transactional
    @Audited(action = AuditAction.PERMISSION_GRANT, resource = "Role")
    public void assignPermissions(UUID roleId, List<UUID> permissionIds) {
        Role role = getRoleById(roleId);
        List<Permission> permissions = permissionRepository.findAllById(permissionIds);
        role.setPermissions(permissions);
        roleRepository.save(role);
    }

    @Transactional(readOnly = true)
    public List<Permission> getRolePermissions(UUID roleId) {
        Role role = getRoleById(roleId);
        return role.getPermissions();
    }

    @Transactional
    public void deleteRoles(List<UUID> roleIds) {
        for (UUID roleId : roleIds) {
            deleteRole(roleId);
        }
    }

    @Transactional
    public void assignPermissionsToRoles(List<UUID> roleIds, List<UUID> permissionIds) {
        for (UUID roleId : roleIds) {
            assignPermissions(roleId, permissionIds);
        }
    }

    @Transactional(readOnly = true)
    public long getUserCountByRole(UUID roleId) {
        return roleRepository.countUsersByRoleId(roleId);
    }
}
