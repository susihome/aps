package com.aps.service;

import com.aps.domain.entity.AuditLog;
import com.aps.domain.enums.AuditAction;
import com.aps.service.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private static final DateTimeFormatter CSV_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 记录审计日志（异步，独立事务）
     * 使用 REQUIRES_NEW 确保异步线程中有独立事务上下文
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAudit(UUID userId, String username, AuditAction action,
                         String resource, String details, String ipAddress) {
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setUserId(userId);
            auditLog.setUsername(username);
            auditLog.setAction(action);
            auditLog.setResource(resource);
            auditLog.setDetails(details);
            auditLog.setIpAddress(ipAddress);
            auditLog.setTimestamp(LocalDateTime.now());

            auditLogRepository.save(auditLog);
            log.debug("审计日志已记录: {} - {} - {}", username, action, resource);
        } catch (Exception e) {
            log.error("记录审计日志失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 根据ID查询单条审计日志
     */
    @Transactional(readOnly = true)
    public Optional<AuditLog> getById(UUID id) {
        return auditLogRepository.findById(id);
    }

    /**
     * 获取用户的审计日志
     */
    @Transactional(readOnly = true)
    public List<AuditLog> getUserAuditLogs(UUID userId) {
        return auditLogRepository.findByUserIdOrderByTimestampDesc(userId);
    }

    /**
     * 获取指定时间范围内的审计日志
     */
    @Transactional(readOnly = true)
    public List<AuditLog> getAuditLogsByActionAndTimeRange(AuditAction action,
                                                            LocalDateTime start,
                                                            LocalDateTime end) {
        return auditLogRepository.findByActionAndTimestampBetween(action, start, end);
    }

    /**
     * 获取所有审计日志
     */
    @Transactional(readOnly = true)
    public List<AuditLog> getAllAuditLogs() {
        return auditLogRepository.findAll();
    }

    /**
     * 分页查询审计日志
     */
    @Transactional(readOnly = true)
    public Page<AuditLog> getAuditLogs(Pageable pageable) {
        return auditLogRepository.findAll(pageable);
    }

    /**
     * 多条件搜索审计日志（支持username模糊搜索）
     */
    @Transactional(readOnly = true)
    public Page<AuditLog> searchAuditLogs(UUID userId, String username, AuditAction action,
                                          String resource, LocalDateTime startTime, LocalDateTime endTime,
                                          Pageable pageable) {
        if (startTime == null) {
            startTime = LocalDateTime.now().minusDays(30);
        }
        if (endTime == null) {
            endTime = LocalDateTime.now();
        }

        return auditLogRepository.searchAuditLogs(userId, username, action, resource, startTime, endTime, pageable);
    }

    /**
     * 按操作类型统计
     */
    @Transactional(readOnly = true)
    public Map<AuditAction, Long> getActionStatistics(LocalDateTime startTime, LocalDateTime endTime) {
        List<Object[]> results = auditLogRepository.countByAction(startTime, endTime);
        Map<AuditAction, Long> statistics = new HashMap<>();
        for (Object[] result : results) {
            statistics.put((AuditAction) result[0], (Long) result[1]);
        }
        return statistics;
    }

    /**
     * 按用户统计
     */
    @Transactional(readOnly = true)
    public Map<String, Long> getUserActivityStatistics(LocalDateTime startTime, LocalDateTime endTime) {
        List<Object[]> results = auditLogRepository.countByUser(startTime, endTime);
        Map<String, Long> statistics = new HashMap<>();
        for (Object[] result : results) {
            statistics.put((String) result[0], (Long) result[1]);
        }
        return statistics;
    }

    /**
     * 导出审计日志为CSV格式
     */
    @Transactional(readOnly = true)
    public byte[] exportAuditLogs(LocalDateTime startTime, LocalDateTime endTime) {
        try {
            List<AuditLog> logs = auditLogRepository.searchAuditLogs(
                null, null, null, null, startTime, endTime,
                org.springframework.data.domain.PageRequest.of(0, 100000)
            ).getContent();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(baos, StandardCharsets.UTF_8));

            // 写入CSV头部（带BOM以支持Excel正确显示中文）
            writer.write('\ufeff');
            writer.println("时间,用户名,操作类型,资源类型,IP地址,详情");

            // 写入数据行
            for (AuditLog log : logs) {
                writer.printf("%s,\"%s\",%s,\"%s\",\"%s\",\"%s\"%n",
                    log.getTimestamp().format(CSV_DATE_FORMATTER),
                    escapeCsv(log.getUsername()),
                    log.getAction(),
                    escapeCsv(log.getResource()),
                    escapeCsv(log.getIpAddress()),
                    escapeCsv(log.getDetails())
                );
            }

            writer.flush();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("导出审计日志失败", e);
            throw new RuntimeException("导出审计日志失败: " + e.getMessage());
        }
    }

    /**
     * CSV字段转义（所有字段都用双引号包裹以安全处理逗号）
     */
    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\"", "\"\"").replace("\n", " ").replace("\r", "");
    }
}
