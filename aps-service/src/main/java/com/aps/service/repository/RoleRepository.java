package com.aps.service.repository;

import com.aps.domain.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {

    /**
     * 根据角色名称查询
     */
    Optional<Role> findByName(String name);

    /**
     * 批量查询角色及其权限（用于用户认证）
     */
    @Query("SELECT DISTINCT r FROM Role r LEFT JOIN FETCH r.permissions WHERE r.id IN :roleIds")
    List<Role> findAllWithPermissionsByIdIn(@Param("roleIds") List<UUID> roleIds);

    /**
     * 查询所有角色及其权限
     */
    @Query("SELECT DISTINCT r FROM Role r LEFT JOIN FETCH r.permissions ORDER BY r.name")
    List<Role> findAllWithPermissions();

    /**
     * 根据 ID 查询角色及其权限
     */
    @Query("SELECT DISTINCT r FROM Role r LEFT JOIN FETCH r.permissions WHERE r.id = :id")
    Optional<Role> findByIdWithPermissions(@Param("id") UUID id);

    /**
     * 检查角色名称是否存在
     */
    boolean existsByName(String name);

    /**
     * 统计使用该角色的用户数量
     */
    @Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r.id = :roleId")
    long countUsersByRoleId(@Param("roleId") UUID roleId);
}
