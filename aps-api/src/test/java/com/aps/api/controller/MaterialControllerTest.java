package com.aps.api.controller;

import com.aps.api.exception.GlobalExceptionHandler;
import com.aps.domain.entity.Material;
import com.aps.service.MaterialService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("物料控制器测试")
class MaterialControllerTest {

    @Mock
    private MaterialService materialService;

    @InjectMocks
    private MaterialController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("查询物料列表应返回200")
    void getMaterials_whenAuthorized_shouldReturnOk() throws Exception {
        Material material = new Material();
        material.setId(UUID.randomUUID());
        material.setMaterialCode("MAT-001");
        material.setMaterialName("PP树脂");

        when(materialService.getAllMaterials()).thenReturn(List.of(material));

        mockMvc.perform(get("/api/materials").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("新增物料参数非法时应返回400")
    void createMaterial_whenInvalidPayload_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/materials")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"materialCode\":\"\",\"materialName\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("导入物料文件应返回导入统计和错误文件")
    void importMaterials_shouldReturnSummary() throws Exception {
        when(materialService.importMaterials(any(String.class), any(java.io.InputStream.class)))
                .thenReturn(new MaterialService.MaterialImportResult(
                        3, 2, 1, 1,
                        java.util.List.of(new MaterialService.MaterialImportFailure(3, "materialName", "物料名称不能为空")),
                        "materials-import-errors.csv",
                        "token-123"
                ));

        mockMvc.perform(multipart("/api/materials/import")
                .file(new org.springframework.mock.web.MockMultipartFile("file", "materials.xlsx",
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                new byte[] {1, 2, 3}))
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.failedCount").value(1))
                .andExpect(jsonPath("$.data.failures[0].rowNumber").value(3))
                .andExpect(jsonPath("$.data.failures[0].columnName").value("materialName"))
                .andExpect(jsonPath("$.data.failures[0].message").value("物料名称不能为空"))
                .andExpect(jsonPath("$.data.errorFileName").value("materials-import-errors.csv"))
                .andExpect(jsonPath("$.data.errorFileToken").value("token-123"));
    }

    @Test
    @DisplayName("下载导入错误文件应返回附件")
    void downloadImportErrorFile_shouldReturnAttachment() throws Exception {
        when(materialService.loadImportErrorFile("token-123"))
                .thenReturn("materialCode,errorColumn,errorMessage\nMAT-001,materialName,不能为空".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        when(materialService.getImportErrorFileName("token-123")).thenReturn("materials-import-errors.csv");

        mockMvc.perform(get("/api/materials/import-errors/token-123"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", org.hamcrest.Matchers.containsString("text/csv")))
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("materials-import-errors.csv")));
    }

    @Test
    @DisplayName("创建导出文件应返回文件token")
    void createExportFile_shouldReturnToken() throws Exception {
        when(materialService.exportMaterialsToFile("csv"))
                .thenReturn(new MaterialService.ExportFileResult("materials-export.csv", "export-token"));

        mockMvc.perform(post("/api/materials/export-files")
                        .param("format", "csv"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fileName").value("materials-export.csv"))
                .andExpect(jsonPath("$.data.fileToken").value("export-token"));
    }

    @Test
    @DisplayName("下载导出文件应返回附件")
    void downloadExportFile_shouldReturnAttachment() throws Exception {
        when(materialService.loadExportFile("export-token"))
                .thenReturn("materialCode\nMAT-001".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        when(materialService.getExportFileName("export-token")).thenReturn("materials-export.csv");

        mockMvc.perform(get("/api/materials/exports/export-token"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", org.hamcrest.Matchers.containsString("text/csv")))
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("materials-export.csv")));
    }

    @Test
    @DisplayName("创建模板文件应返回文件token")
    void createTemplateFile_shouldReturnToken() throws Exception {
        when(materialService.exportTemplateToFile("xlsx"))
                .thenReturn(new MaterialService.ExportFileResult("materials-template.xlsx", "template-token"));

        mockMvc.perform(post("/api/materials/template-files")
                        .param("format", "xlsx"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.fileName").value("materials-template.xlsx"))
                .andExpect(jsonPath("$.data.fileToken").value("template-token"));
    }

    @Test
    @DisplayName("下载模板文件应返回附件")
    void downloadTemplateFile_shouldReturnAttachment() throws Exception {
        when(materialService.loadTemplateFile("template-token"))
                .thenReturn(new byte[] {1, 2, 3});
        when(materialService.getTemplateFileName("template-token")).thenReturn("materials-template.xlsx");

        mockMvc.perform(get("/api/materials/templates/template-token"))
                .andExpect(status().isOk())
                .andExpect(header().string(
                        "Content-Type",
                        org.hamcrest.Matchers.containsString("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")))
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("materials-template.xlsx")));
    }
}
