package com.aps.service.repository;

import com.aps.domain.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, UUID> {

    /**
     * 查询所有顶级权限（parent为null），预加载 parent 关系
     */
    @Query("SELECT DISTINCT p FROM Permission p LEFT JOIN FETCH p.parent WHERE p.parent IS NULL ORDER BY p.sort ASC")
    List<Permission> findRootPermissionsWithParent();

    /**
     * 查询所有子权限（parent不为null），预加载 parent 关系
     */
    @Query("SELECT DISTINCT p FROM Permission p LEFT JOIN FETCH p.parent WHERE p.parent IS NOT NULL ORDER BY p.sort ASC")
    List<Permission> findChildPermissionsWithParent();

    /**
     * 查询所有顶级权限（parent为null）
     */
    @Query("SELECT p FROM Permission p WHERE p.parent IS NULL ORDER BY p.sort ASC")
    List<Permission> findRootPermissions();

    /**
     * 查询权限树（包含所有子权限）
     */
    @Query("SELECT DISTINCT p FROM Permission p LEFT JOIN FETCH p.children WHERE p.parent IS NULL ORDER BY p.sort ASC")
    List<Permission> findPermissionTree();

    /**
     * 根据权限编码查询
     */
    Optional<Permission> findByCode(String code);

    /**
     * 查询某个权限的所有子权限
     */
    List<Permission> findByParentIdOrderBySort(UUID parentId);

    /**
     * 检查权限编码是否存在
     */
    boolean existsByCode(String code);

    /**
     * 查询所有启用的权限
     */
    @Query("SELECT p FROM Permission p WHERE p.enabled = true ORDER BY p.sort ASC")
    List<Permission> findAllEnabled();

    /**
     * 批量更新排序号（避免N+1查询）
     */
    @Modifying
    @Query("UPDATE Permission p SET p.sort = CASE WHEN p.id = :id THEN :sort ELSE p.sort END WHERE p.id IN :ids")
    void updateSortBatch(@Param("ids") List<UUID> ids, @Param("id") UUID id, @Param("sort") Integer sort);
}
