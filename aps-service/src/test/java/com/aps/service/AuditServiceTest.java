package com.aps.service;

import com.aps.domain.entity.AuditLog;
import com.aps.domain.enums.AuditAction;
import com.aps.service.repository.AuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditService auditService;

    private AuditLog testAuditLog;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testAuditLog = new AuditLog();
        testAuditLog.setId(UUID.randomUUID());
        testAuditLog.setUserId(testUserId);
        testAuditLog.setUsername("testuser");
        testAuditLog.setAction(AuditAction.LOGIN);
        testAuditLog.setResource("Auth");
        testAuditLog.setDetails("{\"test\":\"data\"}");
        testAuditLog.setIpAddress("192.168.1.100");
        testAuditLog.setTimestamp(LocalDateTime.now());
    }

    @Test
    void testLogAudit() {
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(testAuditLog);

        auditService.logAudit(testUserId, "testuser", AuditAction.LOGIN, "Auth", "{\"test\":\"data\"}", "192.168.1.100");

        verify(auditLogRepository, timeout(1000).times(1)).save(any(AuditLog.class));
    }

    @Test
    void testGetUserAuditLogs() {
        when(auditLogRepository.findByUserIdOrderByTimestampDesc(testUserId)).thenReturn(Arrays.asList(testAuditLog));

        List<AuditLog> result = auditService.getUserAuditLogs(testUserId);

        assertEquals(1, result.size());
        verify(auditLogRepository, times(1)).findByUserIdOrderByTimestampDesc(testUserId);
    }

    @Test
    void testGetAuditLogs() {
        Pageable pageable = PageRequest.of(0, 10);
        when(auditLogRepository.findAll(pageable)).thenReturn(new PageImpl<>(Arrays.asList(testAuditLog)));

        Page<AuditLog> result = auditService.getAuditLogs(pageable);

        assertEquals(1, result.getTotalElements());
        verify(auditLogRepository, times(1)).findAll(pageable);
    }

    @Test
    void testSearchAuditLogs() {
        Pageable pageable = PageRequest.of(0, 10);
        LocalDateTime startTime = LocalDateTime.now().minusDays(7);
        LocalDateTime endTime = LocalDateTime.now();

        when(auditLogRepository.searchAuditLogs(any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(new PageImpl<>(Arrays.asList(testAuditLog)));

        Page<AuditLog> result = auditService.searchAuditLogs(testUserId, "testuser", AuditAction.LOGIN, "Auth", startTime, endTime, pageable);

        assertEquals(1, result.getTotalElements());
        verify(auditLogRepository, times(1)).searchAuditLogs(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void testSearchAuditLogsWithDefaultTimeRange() {
        when(auditLogRepository.searchAuditLogs(any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(new PageImpl<>(Arrays.asList(testAuditLog)));

        Page<AuditLog> result = auditService.searchAuditLogs(null, null, null, null, null, null, PageRequest.of(0, 10));

        assertNotNull(result);
        verify(auditLogRepository, times(1)).searchAuditLogs(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void testGetActionStatistics() {
        LocalDateTime startTime = LocalDateTime.now().minusDays(7);
        LocalDateTime endTime = LocalDateTime.now();
        when(auditLogRepository.countByAction(startTime, endTime)).thenReturn(Arrays.asList(
            new Object[]{AuditAction.LOGIN, 10L},
            new Object[]{AuditAction.LOGOUT, 8L}
        ));

        Map<AuditAction, Long> result = auditService.getActionStatistics(startTime, endTime);

        assertEquals(2, result.size());
        assertEquals(10L, result.get(AuditAction.LOGIN));
    }

    @Test
    void testGetUserActivityStatistics() {
        LocalDateTime startTime = LocalDateTime.now().minusDays(7);
        LocalDateTime endTime = LocalDateTime.now();
        when(auditLogRepository.countByUser(startTime, endTime)).thenReturn(Arrays.asList(
            new Object[]{"admin", 50L},
            new Object[]{"planner", 30L}
        ));

        Map<String, Long> result = auditService.getUserActivityStatistics(startTime, endTime);

        assertEquals(2, result.size());
        assertEquals(50L, result.get("admin"));
    }

    @Test
    void testExportAuditLogs() {
        when(auditLogRepository.searchAuditLogs(any(), any(), any(), any(), any(), any(), any()))
            .thenReturn(new PageImpl<>(Arrays.asList(testAuditLog)));

        byte[] result = auditService.exportAuditLogs(LocalDateTime.now().minusDays(7), LocalDateTime.now());

        assertTrue(result.length > 0);
        String csvContent = new String(result, java.nio.charset.StandardCharsets.UTF_8);
        assertTrue(csvContent.contains("时间"));
        assertTrue(csvContent.contains("testuser"));
    }

    @Test
    void testGetAuditLogsByActionAndTimeRange() {
        LocalDateTime start = LocalDateTime.now().minusDays(7);
        LocalDateTime end = LocalDateTime.now();
        when(auditLogRepository.findByActionAndTimestampBetween(AuditAction.LOGIN, start, end))
            .thenReturn(Arrays.asList(testAuditLog));

        List<AuditLog> result = auditService.getAuditLogsByActionAndTimeRange(AuditAction.LOGIN, start, end);

        assertEquals(1, result.size());
        assertEquals(AuditAction.LOGIN, result.get(0).getAction());
    }

    @Test
    void testGetAllAuditLogs() {
        when(auditLogRepository.findAll()).thenReturn(Arrays.asList(testAuditLog));

        List<AuditLog> result = auditService.getAllAuditLogs();

        assertEquals(1, result.size());
    }

    @Test
    void testGetById() {
        when(auditLogRepository.findById(testAuditLog.getId())).thenReturn(Optional.of(testAuditLog));

        Optional<AuditLog> result = auditService.getById(testAuditLog.getId());

        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
    }

    @Test
    void testGetByIdNotFound() {
        UUID unknownId = UUID.randomUUID();
        when(auditLogRepository.findById(unknownId)).thenReturn(Optional.empty());

        Optional<AuditLog> result = auditService.getById(unknownId);

        assertTrue(result.isEmpty());
    }
}
