package com.aps.service;

import com.aps.domain.entity.Permission;
import com.aps.service.exception.ResourceConflictException;
import com.aps.service.exception.ResourceNotFoundException;
import com.aps.service.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PermissionService {

    private final PermissionRepository permissionRepository;

    /**
     * 获取权限树 - 使用优化的查询避免 LazyInitializationException
     */
    @Transactional(readOnly = true)
    public List<Permission> getPermissionTree() {
        // 分别查询根权限和子权限，使用 LEFT JOIN FETCH 预加载 parent
        List<Permission> roots = permissionRepository.findRootPermissionsWithParent();
        List<Permission> children = permissionRepository.findChildPermissionsWithParent();

        // 构建 ID 映射
        Map<UUID, Permission> idMap = new HashMap<>();
        roots.forEach(p -> idMap.put(p.getId(), p));
        children.forEach(p -> idMap.put(p.getId(), p));

        // 构建树形结构
        for (Permission child : children) {
            if (child.getParent() != null) {
                Permission parent = idMap.get(child.getParent().getId());
                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }
                    parent.getChildren().add(child);
                }
            }
        }

        sortTree(roots);
        return roots;
    }

    private void sortTree(List<Permission> nodes) {
        if (nodes == null) return;
        nodes.sort(Comparator.comparingInt(p -> p.getSort() != null ? p.getSort() : 0));
        for (Permission node : nodes) {
            sortTree(node.getChildren());
        }
    }

    /**
     * 获取单个权限
     */
    @Transactional(readOnly = true)
    public Permission getPermission(UUID id) {
        return permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("权限不存在: " + id));
    }

    /**
     * 创建权限
     */
    @Transactional
    public Permission createPermission(Permission permission) {
        if (permissionRepository.existsByCode(permission.getCode())) {
            throw new ResourceConflictException("权限编码已存在: " + permission.getCode());
        }

        if (permission.getParent() != null && permission.getParent().getId() != null) {
            Permission parent = getPermission(permission.getParent().getId());
            permission.setParent(parent);
        }

        Permission saved = permissionRepository.save(permission);
        log.info("权限已创建: {} ({})", saved.getName(), saved.getCode());
        return saved;
    }

    /**
     * 更新权限
     */
    @Transactional
    public Permission updatePermission(UUID id, Permission updates) {
        Permission permission = getPermission(id);

        if (updates.getName() != null) {
            permission.setName(updates.getName());
        }
        if (updates.getDescription() != null) {
            permission.setDescription(updates.getDescription());
        }
        if (updates.getType() != null) {
            permission.setType(updates.getType());
        }
        if (updates.getRoutePath() != null) {
            permission.setRoutePath(updates.getRoutePath());
        }
        if (updates.getIcon() != null) {
            permission.setIcon(updates.getIcon());
        }
        if (updates.getSort() != null) {
            permission.setSort(updates.getSort());
        }
        if (updates.getEnabled() != null) {
            permission.setEnabled(updates.getEnabled());
        }
        if (updates.getVisible() != null) {
            permission.setVisible(updates.getVisible());
        }

        // 更新父权限（检查循环引用）
        if (updates.getParent() != null && updates.getParent().getId() != null) {
            UUID parentId = updates.getParent().getId();
            // 防止权限成为其自身或其子权限的父权限
            if (parentId.equals(id)) {
                throw new ResourceConflictException("不能将权限设置为其自身的父权限");
            }
            if (isAncestor(id, parentId)) {
                throw new ResourceConflictException("不能将权限设置为其子权限的父权限");
            }
            Permission parent = getPermission(parentId);
            permission.setParent(parent);
        } else if (updates.getParent() == null) {
            permission.setParent(null);
        }

        Permission saved = permissionRepository.save(permission);
        log.info("权限已更新: {} ({})", saved.getName(), saved.getCode());
        return saved;
    }

    /**
     * 检查是否为祖先权限（防止循环引用）
     */
    private boolean isAncestor(UUID permissionId, UUID potentialAncestorId) {
        Permission current = permissionRepository.findById(potentialAncestorId).orElse(null);
        while (current != null) {
            if (current.getId().equals(permissionId)) {
                return true;
            }
            current = current.getParent();
        }
        return false;
    }

    /**
     * 删除权限
     */
    @Transactional
    public void deletePermission(UUID id) {
        Permission permission = getPermission(id);

        // 检查是否有子权限
        if (permission.getChildren() != null && !permission.getChildren().isEmpty()) {
            throw new ResourceConflictException("权限存在子权限，无法删除");
        }

        permissionRepository.delete(permission);
        log.info("权限已删除: {} ({})", permission.getName(), permission.getCode());
    }

    /**
     * 启用/禁用权限
     */
    @Transactional
    public Permission togglePermission(UUID id, boolean enabled) {
        Permission permission = getPermission(id);
        permission.setEnabled(enabled);
        Permission saved = permissionRepository.save(permission);
        log.info("权限状态已更新: {} ({}), enabled={}", saved.getName(), saved.getCode(), enabled);
        return saved;
    }

    /**
     * 批量更新排序
     */
    @Transactional
    public void updateSort(List<SortUpdate> updates) {
        if (updates == null || updates.isEmpty()) {
            return;
        }

        // 使用批量更新而不是逐个更新（避免N+1查询）
        for (SortUpdate update : updates) {
            permissionRepository.updateSortBatch(
                updates.stream().map(SortUpdate::getId).toList(),
                update.getId(),
                update.getSort()
            );
        }
        log.info("权限排序已更新: {} 条记录", updates.size());
    }

    /**
     * 获取所有启用的权限
     */
    @Transactional(readOnly = true)
    public List<Permission> getAllEnabled() {
        return permissionRepository.findAllEnabled();
    }

    /**
     * 排序更新DTO
     */
    public static class SortUpdate {
        private UUID id;
        private Integer sort;

        public SortUpdate() {}

        public SortUpdate(UUID id, Integer sort) {
            this.id = id;
            this.sort = sort;
        }

        public UUID getId() {
            return id;
        }

        public void setId(UUID id) {
            this.id = id;
        }

        public Integer getSort() {
            return sort;
        }

        public void setSort(Integer sort) {
            this.sort = sort;
        }
    }
}
