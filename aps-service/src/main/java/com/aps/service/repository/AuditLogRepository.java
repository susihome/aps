package com.aps.service.repository;

import com.aps.domain.entity.AuditLog;
import com.aps.domain.enums.AuditAction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
    List<AuditLog> findByUserIdOrderByTimestampDesc(UUID userId);
    List<AuditLog> findByActionAndTimestampBetween(AuditAction action, LocalDateTime start, LocalDateTime end);

    /**
     * 多条件搜索审计日志（支持username模糊搜索）
     */
    @Query("SELECT a FROM AuditLog a WHERE " +
           "(:userId IS NULL OR a.userId = :userId) AND " +
           "(:username IS NULL OR :username = '' OR LOWER(a.username) LIKE LOWER(CONCAT('%', :username, '%'))) AND " +
           "(:action IS NULL OR a.action = :action) AND " +
           "(:resource IS NULL OR :resource = '' OR LOWER(a.resource) LIKE LOWER(CONCAT('%', :resource, '%'))) AND " +
           "a.timestamp BETWEEN :startTime AND :endTime " +
           "ORDER BY a.timestamp DESC")
    Page<AuditLog> searchAuditLogs(
        @Param("userId") UUID userId,
        @Param("username") String username,
        @Param("action") AuditAction action,
        @Param("resource") String resource,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime,
        Pageable pageable
    );

    /**
     * 按操作类型统计
     */
    @Query("SELECT a.action, COUNT(a) FROM AuditLog a " +
           "WHERE a.timestamp BETWEEN :startTime AND :endTime " +
           "GROUP BY a.action")
    List<Object[]> countByAction(
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );

    /**
     * 按用户统计
     */
    @Query("SELECT a.username, COUNT(a) FROM AuditLog a " +
           "WHERE a.timestamp BETWEEN :startTime AND :endTime " +
           "GROUP BY a.username " +
           "ORDER BY COUNT(a) DESC")
    List<Object[]> countByUser(
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
}
