package com.aps.api.controller;

import com.aps.api.exception.GlobalExceptionHandler;
import com.aps.service.AuditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("审计日志控制器测试")
class AuditLogControllerTest {

    @Mock
    private AuditService auditService;

    @InjectMocks
    private AuditLogController auditLogController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(auditLogController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("创建审计导出文件应返回文件token")
    void createExportFile_shouldReturnToken() throws Exception {
        when(auditService.exportAuditLogsToFile(any(), any()))
                .thenReturn(new AuditService.ExportFileResult("audit_logs_20260413_162000.csv", "audit-token"));

        mockMvc.perform(post("/api/audit-logs/export-files"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fileName").value("audit_logs_20260413_162000.csv"))
                .andExpect(jsonPath("$.data.fileToken").value("audit-token"));
    }

    @Test
    @DisplayName("下载审计导出文件应返回附件")
    void downloadExportFile_shouldReturnAttachment() throws Exception {
        when(auditService.getExportFileName("audit-token")).thenReturn("audit_logs_20260413_162000.csv");
        when(auditService.loadExportFile("audit-token"))
                .thenReturn("时间,用户名\n2026-04-13 16:20:00,testuser".getBytes(java.nio.charset.StandardCharsets.UTF_8));

        mockMvc.perform(get("/api/audit-logs/exports/audit-token"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", org.hamcrest.Matchers.containsString("text/csv")))
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("audit_logs_20260413_162000.csv")));
    }
}
