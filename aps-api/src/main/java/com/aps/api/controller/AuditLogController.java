package com.aps.api.controller;

import com.aps.api.dto.AjaxResult;
import com.aps.domain.entity.AuditLog;
import com.aps.domain.enums.AuditAction;
import com.aps.service.AuditService;
import com.aps.service.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 审计日志控制器
 * 仅ADMIN角色可访问
 */
@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AuditLogController {

    private final AuditService auditService;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("timestamp", "username", "action", "resource");

    /**
     * 分页查询审计日志
     */
    @GetMapping
    public AjaxResult<Page<AuditLog>> getAuditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "timestamp") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection
    ) {
        // 校验分页参数
        if (size > 100) size = 100;
        if (page < 0) page = 0;

        // 校验排序字段白名单
        if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
            sortBy = "timestamp";
        }

        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<AuditLog> auditLogs = auditService.getAuditLogs(pageable);
        return AjaxResult.success(auditLogs);
    }

    /**
     * 多条件搜索审计日志
     */
    @GetMapping("/search")
    public AjaxResult<Page<AuditLog>> searchAuditLogs(
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) AuditAction action,
            @RequestParam(required = false) String resource,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        if (size > 100) size = 100;

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        Page<AuditLog> auditLogs = auditService.searchAuditLogs(userId, username, action, resource, startTime, endTime, pageable);
        return AjaxResult.success(auditLogs);
    }

    /**
     * 查询单条审计日志详情
     */
    @GetMapping("/{id}")
    public AjaxResult<AuditLog> getAuditLogById(@PathVariable UUID id) {
        AuditLog auditLog = auditService.getById(id)
                .orElseThrow(() -> new ResourceNotFoundException("审计日志不存在: " + id));
        return AjaxResult.success(auditLog);
    }

    /**
     * 统计分析
     */
    @GetMapping("/statistics")
    public AjaxResult<Map<String, Object>> getStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime
    ) {
        if (startTime == null) {
            startTime = LocalDateTime.now().minusDays(30);
        }
        if (endTime == null) {
            endTime = LocalDateTime.now();
        }

        Map<AuditAction, Long> actionStats = auditService.getActionStatistics(startTime, endTime);
        Map<String, Long> userStats = auditService.getUserActivityStatistics(startTime, endTime);

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("actionStatistics", actionStats);
        statistics.put("userStatistics", userStats);
        statistics.put("startTime", startTime);
        statistics.put("endTime", endTime);

        return AjaxResult.success(statistics);
    }

    /**
     * 导出审计日志为CSV
     */
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportAuditLogs(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime
    ) {
        if (startTime == null) {
            startTime = LocalDateTime.now().minusDays(30);
        }
        if (endTime == null) {
            endTime = LocalDateTime.now();
        }

        byte[] csvData = auditService.exportAuditLogs(startTime, endTime);

        String filename = "audit_logs_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csvData);
    }
}
