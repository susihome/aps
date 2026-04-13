package com.aps.service;

import com.aps.domain.entity.Material;
import com.aps.service.exception.ResourceConflictException;
import com.aps.service.exception.ResourceNotFoundException;
import com.aps.service.exception.ValidationException;
import com.aps.service.repository.MaterialMoldBindingRepository;
import com.aps.service.repository.MaterialRepository;
import com.aps.service.repository.OperationRepository;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("物料服务测试")
class MaterialServiceTest {

    @Mock
    private MaterialRepository materialRepository;

    @Mock
    private OperationRepository operationRepository;

    @Mock
    private MaterialMoldBindingRepository materialMoldBindingRepository;

    @Mock
    private FileObjectService fileObjectService;

    @Mock
    private ScheduledTaskLockService scheduledTaskLockService;

    @InjectMocks
    private MaterialService materialService;

    @Test
    @DisplayName("创建物料时应标准化编码并保存")
    void createMaterial_whenValidInput_shouldNormalizeAndSave() {
        when(materialRepository.existsByMaterialCode("MAT-001")).thenReturn(false);
        when(materialRepository.save(any(Material.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Material result = materialService.createMaterial(" mat-001 ", " PP树脂 ", " 25kg/袋 ", " kg ", null, " 重点物料 ",
                null, null, null, null, null, null, null, null);

        assertThat(result.getMaterialCode()).isEqualTo("MAT-001");
        assertThat(result.getMaterialName()).isEqualTo("PP树脂");
        assertThat(result.getEnabled()).isTrue();
    }

    @Test
    @DisplayName("创建重复编码物料应抛出冲突异常")
    void createMaterial_whenCodeDuplicated_shouldThrowConflict() {
        when(materialRepository.existsByMaterialCode("MAT-001")).thenReturn(true);

        assertThatThrownBy(() -> materialService.createMaterial("MAT-001", "PP", null, null, true, null,
                null, null, null, null, null, null, null, null))
                .isInstanceOf(ResourceConflictException.class)
                .hasMessageContaining("物料编码已存在");

        verify(materialRepository, never()).save(any(Material.class));
    }

    @Test
    @DisplayName("删除被工序引用的物料应抛出冲突异常")
    void deleteMaterial_whenReferenced_shouldThrowConflict() {
        UUID id = UUID.randomUUID();
        when(materialRepository.existsById(id)).thenReturn(true);
        when(operationRepository.existsByRequiredMaterial_Id(id)).thenReturn(true);

        assertThatThrownBy(() -> materialService.deleteMaterial(id))
                .isInstanceOf(ResourceConflictException.class)
                .hasMessageContaining("已被工序引用");
    }

    @Test
    @DisplayName("删除被物料模具关系引用的物料应抛出冲突异常")
    void deleteMaterial_whenReferencedByBinding_shouldThrowConflict() {
        UUID id = UUID.randomUUID();
        when(materialRepository.existsById(id)).thenReturn(true);
        when(operationRepository.existsByRequiredMaterial_Id(id)).thenReturn(false);
        when(materialMoldBindingRepository.existsByMaterial_Id(id)).thenReturn(true);

        assertThatThrownBy(() -> materialService.deleteMaterial(id))
                .isInstanceOf(ResourceConflictException.class)
                .hasMessageContaining("物料模具关系引用");
    }

    @Test
    @DisplayName("删除不存在物料应抛出未找到异常")
    void deleteMaterial_whenNotFound_shouldThrowNotFound() {
        UUID id = UUID.randomUUID();
        when(materialRepository.existsById(id)).thenReturn(false);

        assertThatThrownBy(() -> materialService.deleteMaterial(id))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("物料不存在");
    }

    @Test
    @DisplayName("创建物料时应保存排产属性")
    void createMaterial_whenSchedulingFieldsProvided_shouldPersistThem() {
        when(materialRepository.existsByMaterialCode("MAT-002")).thenReturn(false);
        when(materialRepository.save(any(Material.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Material result = materialService.createMaterial("MAT-002", "ABS封盖", null, "个", true, null,
                "BLACK", "ABS", 200, 50, 500, false, "A", "GROUP-A");

        assertThat(result.getColorCode()).isEqualTo("BLACK");
        assertThat(result.getRawMaterialType()).isEqualTo("ABS");
        assertThat(result.getDefaultLotSize()).isEqualTo(200);
        assertThat(result.getMinLotSize()).isEqualTo(50);
        assertThat(result.getMaxLotSize()).isEqualTo(500);
        assertThat(result.getAllowDelay()).isFalse();
        assertThat(result.getAbcClassification()).isEqualTo("A");
        assertThat(result.getProductGroup()).isEqualTo("GROUP-A");
    }

    @Test
    @DisplayName("更新物料时仅更新传入字段")
    void updateMaterial_whenPartialUpdate_shouldUpdateProvidedFields() {
        UUID id = UUID.randomUUID();
        Material material = new Material();
        material.setId(id);
        material.setMaterialCode("MAT-001");
        material.setMaterialName("原名称");
        material.setUnit("kg");

        when(materialRepository.findById(id)).thenReturn(Optional.of(material));
        when(materialRepository.save(any(Material.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Material result = materialService.updateMaterial(id, " 新名称 ", null, null, false, null,
                null, null, null, null, null, null, null, null);

        assertThat(result.getMaterialName()).isEqualTo("新名称");
        assertThat(result.getUnit()).isEqualTo("kg");
        assertThat(result.getEnabled()).isFalse();
    }

    @Test
    @DisplayName("导出物料时应返回可回填的CSV模板")
    void exportMaterials_shouldReturnCsvTemplate() {
        Material material = new Material();
        material.setMaterialCode("MAT-001");
        material.setMaterialName("PP树脂");
        material.setSpecification("25kg/袋");
        material.setUnit("kg");
        material.setEnabled(true);
        material.setRemark("重点物料");
        material.setColorCode("BLACK");
        material.setRawMaterialType("ABS");
        material.setDefaultLotSize(200);
        material.setMinLotSize(50);
        material.setMaxLotSize(500);
        material.setAllowDelay(false);
        material.setAbcClassification("A");
        material.setProductGroup("GROUP-A");
        when(materialRepository.findAllByOrderByMaterialCodeAsc(any(PageRequest.class)))
                .thenReturn(new PageImpl<>(java.util.List.of(material), PageRequest.of(0, 2000), 1));

        byte[] result = materialService.exportMaterials();

        String csv = new String(result, java.nio.charset.StandardCharsets.UTF_8);
        assertThat(csv).contains("materialCode,materialName,specification,unit,enabled,remark,colorCode,rawMaterialType,defaultLotSize,minLotSize,maxLotSize,allowDelay,abcClassification,productGroup");
        assertThat(csv).contains("MAT-001,PP树脂,25kg/袋,kg,true,重点物料,BLACK,ABS,200,50,500,false,A,GROUP-A");
    }

    @Test
    @DisplayName("导出物料为XLSX时应返回可回填工作簿")
    void exportMaterialsAsExcel_shouldReturnWorkbook() throws Exception {
        Material material = new Material();
        material.setMaterialCode("MAT-001");
        material.setMaterialName("PP树脂");
        material.setEnabled(true);
        when(materialRepository.findAllByOrderByMaterialCodeAsc(any(PageRequest.class)))
                .thenReturn(new PageImpl<>(java.util.List.of(material), PageRequest.of(0, 2000), 1));

        byte[] result = materialService.exportMaterialsAsExcel();

        try (var workbook = WorkbookFactory.create(new java.io.ByteArrayInputStream(result))) {
            assertThat(workbook.getNumberOfSheets()).isEqualTo(1);
            Row headerRow = workbook.getSheetAt(0).getRow(0);
            Row dataRow = workbook.getSheetAt(0).getRow(1);
            assertThat(headerRow.getCell(0).getStringCellValue()).isEqualTo("materialCode");
            assertThat(dataRow.getCell(0).getStringCellValue()).isEqualTo("MAT-001");
            assertThat(dataRow.getCell(1).getStringCellValue()).isEqualTo("PP树脂");
        }
    }

    @Test
    @DisplayName("导出物料到共享文件时应返回文件token")
    void exportMaterialsToFile_shouldStoreFileAndReturnToken() {
        Material material = new Material();
        material.setMaterialCode("MAT-001");
        material.setMaterialName("PP树脂");
        material.setEnabled(true);
        when(materialRepository.findAllByOrderByMaterialCodeAsc(any(PageRequest.class)))
                .thenReturn(new PageImpl<>(java.util.List.of(material), PageRequest.of(0, 2000), 1));
        when(fileObjectService.storeTemporaryFile(any(), any(), any(), any(), any())).thenReturn("export-token");

        MaterialService.ExportFileResult result = materialService.exportMaterialsToFile("csv");

        assertThat(result.fileName()).isEqualTo("materials-export.csv");
        assertThat(result.fileToken()).isEqualTo("export-token");
        verify(fileObjectService).storeTemporaryFile(
                eq("MATERIAL_EXPORT"),
                eq("materials-export.csv"),
                any(),
                any(),
                eq("text/csv"));
    }

    @Test
    @DisplayName("导出物料模板到共享文件时应返回文件token")
    void exportMaterialTemplateToFile_shouldStoreFileAndReturnToken() {
        Material material = new Material();
        material.setMaterialCode("MAT-001");
        material.setMaterialName("PP树脂");
        material.setEnabled(true);
        when(materialRepository.findAllByOrderByMaterialCodeAsc(any(PageRequest.class)))
                .thenReturn(new PageImpl<>(java.util.List.of(material), PageRequest.of(0, 2000), 1));
        when(fileObjectService.storeTemporaryFile(any(), any(), any(), any(), any())).thenReturn("template-token");

        MaterialService.ExportFileResult result = materialService.exportTemplateToFile("xlsx");

        assertThat(result.fileName()).isEqualTo("materials-template.xlsx");
        assertThat(result.fileToken()).isEqualTo("template-token");
        verify(fileObjectService).storeTemporaryFile(
                eq("MATERIAL_TEMPLATE"),
                eq("materials-template.xlsx"),
                any(),
                any(),
                eq("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
    }

    @Test
    @DisplayName("导入物料时应按物料编码更新已有数据并新增不存在数据")
    void importMaterials_whenCsvContainsExistingAndNew_shouldUpsert() {
        Material existing = new Material();
        existing.setId(UUID.randomUUID());
        existing.setMaterialCode("MAT-001");
        existing.setMaterialName("旧名称");
        existing.setEnabled(true);

        when(materialRepository.findByMaterialCodeIn(any()))
                .thenAnswer(invocation -> {
                    java.util.Collection<String> codes = invocation.getArgument(0);
                    return codes.contains("MAT-001") ? java.util.List.of(existing) : java.util.List.of();
                });
        when(materialRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        String csv = """
                materialCode,materialName,specification,unit,enabled,remark,colorCode,rawMaterialType,defaultLotSize,minLotSize,maxLotSize,allowDelay,abcClassification,productGroup
                MAT-001,新名称,新版规格,kg,false,更新备注,WHITE,PP,100,10,200,true,B,GROUP-B
                MAT-002,新物料,,,true,,,,,,,,,
                """;

        MaterialService.MaterialImportResult result = materialService.importMaterials(csv.getBytes(java.nio.charset.StandardCharsets.UTF_8));

        assertThat(result.totalCount()).isEqualTo(2);
        assertThat(result.createdCount()).isEqualTo(1);
        assertThat(result.updatedCount()).isEqualTo(1);
        assertThat(existing.getMaterialName()).isEqualTo("新名称");
        assertThat(existing.getEnabled()).isFalse();
        verify(materialRepository).saveAll(argThat(materials -> ((java.util.List<Material>) materials).stream()
                .anyMatch(material -> "MAT-002".equals(material.getMaterialCode()) && "新物料".equals(material.getMaterialName()))));
    }

    @Test
    @DisplayName("导入物料时表头不匹配应抛出校验异常")
    void importMaterials_whenHeaderInvalid_shouldThrowValidationException() {
        String csv = """
                materialCode,materialName
                MAT-001,PP树脂
                """;

        assertThatThrownBy(() -> materialService.importMaterials(csv.getBytes(java.nio.charset.StandardCharsets.UTF_8)))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("导入模板不正确");
    }

    @Test
    @DisplayName("导入物料时应先落库成功数据并返回错误文件")
    void importMaterials_whenPartiallyInvalid_shouldPersistSuccessRowsAndReturnErrorFile() throws Exception {
        Material existing = new Material();
        existing.setId(UUID.randomUUID());
        existing.setMaterialCode("MAT-001");
        existing.setMaterialName("旧名称");
        existing.setEnabled(true);
        when(materialRepository.findByMaterialCodeIn(any()))
                .thenAnswer(invocation -> {
                    java.util.Collection<String> codes = invocation.getArgument(0);
                    return codes.contains("MAT-001") ? java.util.List.of(existing) : java.util.List.of();
                });
        when(materialRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(fileObjectService.storeTemporaryFile(any(), any(), any(), any(), any())).thenReturn("token-123");
        when(fileObjectService.loadTemporaryFile("token-123", "MATERIAL_IMPORT_ERROR"))
                .thenReturn(new FileObjectService.StoredFile(
                        "materials-import-errors.csv",
                        "materialCode,materialName,specification,unit,enabled,remark,colorCode,rawMaterialType,defaultLotSize,minLotSize,maxLotSize,allowDelay,abcClassification,productGroup,errorColumn,errorMessage\nMAT-002,,25kg/袋,kg,true,,,,,,,,,,materialName,物料名称不能为空".getBytes(java.nio.charset.StandardCharsets.UTF_8),
                        "text/csv"));

        String csv = """
                materialCode,materialName,specification,unit,enabled,remark,colorCode,rawMaterialType,defaultLotSize,minLotSize,maxLotSize,allowDelay,abcClassification,productGroup
                MAT-001,更新成功,25kg/袋,kg,true,,,,,,,,,
                MAT-002,,25kg/袋,kg,true,,,,,,,,,
                """;

        MaterialService.MaterialImportResult result = materialService.importMaterials("materials.csv", csv.getBytes(java.nio.charset.StandardCharsets.UTF_8));

        assertThat(result.totalCount()).isEqualTo(2);
        assertThat(result.updatedCount()).isEqualTo(1);
        assertThat(result.createdCount()).isEqualTo(0);
        assertThat(result.failedCount()).isEqualTo(1);
        assertThat(existing.getMaterialName()).isEqualTo("更新成功");
        assertThat(result.failures()).hasSize(1);
        assertThat(result.failures().getFirst().rowNumber()).isEqualTo(3);
        assertThat(result.errorFileName()).isEqualTo("materials-import-errors.csv");
        assertThat(result.errorFileToken()).isNotBlank();

        byte[] errorFileContent = materialService.loadImportErrorFile(result.errorFileToken());
        String errorCsv = new String(errorFileContent, java.nio.charset.StandardCharsets.UTF_8);
        assertThat(errorCsv).contains("materialCode,materialName,specification,unit,enabled,remark,colorCode,rawMaterialType,defaultLotSize,minLotSize,maxLotSize,allowDelay,abcClassification,productGroup,errorColumn,errorMessage");
        assertThat(errorCsv).contains("MAT-002,,25kg/袋,kg,true,,,,,,,,,,materialName,物料名称不能为空");
    }

    @Test
    @DisplayName("导入物料为XLSX时应支持更新和新增")
    void importMaterialsFromExcel_shouldUpsert() throws Exception {
        Material existing = new Material();
        existing.setId(UUID.randomUUID());
        existing.setMaterialCode("MAT-001");
        existing.setMaterialName("旧名称");
        existing.setEnabled(true);
        when(materialRepository.findByMaterialCodeIn(any()))
                .thenAnswer(invocation -> {
                    java.util.Collection<String> codes = invocation.getArgument(0);
                    return codes.contains("MAT-001") ? java.util.List.of(existing) : java.util.List.of();
                });
        when(materialRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        byte[] workbook = createMaterialWorkbook(
                new String[] {"MAT-001", "新名称", "", "", "false", "", "", "", "", "", "", "", "", ""},
                new String[] {"MAT-002", "新物料", "", "", "true", "", "", "", "", "", "", "", "", ""}
        );

        MaterialService.MaterialImportResult result = materialService.importMaterials("materials.xlsx", workbook);

        assertThat(result.totalCount()).isEqualTo(2);
        assertThat(result.createdCount()).isEqualTo(1);
        assertThat(result.updatedCount()).isEqualTo(1);
        assertThat(existing.getMaterialName()).isEqualTo("新名称");
        assertThat(existing.getEnabled()).isFalse();
    }

    @Test
    @DisplayName("导入物料为XLSX时应支持部分成功并生成错误文件")
    void importMaterialsFromExcel_whenPartiallyInvalid_shouldPersistSuccessRowsAndReturnErrorFile() throws Exception {
        Material existing = new Material();
        existing.setId(UUID.randomUUID());
        existing.setMaterialCode("MAT-001");
        existing.setMaterialName("旧名称");
        existing.setEnabled(true);
        when(materialRepository.findByMaterialCodeIn(argThat(codes -> codes.contains("MAT-001") && codes.contains("MAT-002"))))
                .thenReturn(java.util.List.of(existing));
        when(materialRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(fileObjectService.storeTemporaryFile(any(), any(), any(), any(), any())).thenReturn("token-123");
        when(fileObjectService.loadTemporaryFile("token-123", "MATERIAL_IMPORT_ERROR"))
                .thenReturn(new FileObjectService.StoredFile(
                        "materials-import-errors.csv",
                        "materialCode,materialName,specification,unit,enabled,remark,colorCode,rawMaterialType,defaultLotSize,minLotSize,maxLotSize,allowDelay,abcClassification,productGroup,errorColumn,errorMessage\nMAT-002,,25kg/袋,kg,true,,,,,,,,,,materialName,物料名称不能为空".getBytes(java.nio.charset.StandardCharsets.UTF_8),
                        "text/csv"));

        byte[] workbook = createMaterialWorkbook(
                new String[] {"MAT-001", "更新成功", "", "", "true", "", "", "", "", "", "", "", "", ""},
                new String[] {"MAT-002", "", "", "", "true", "", "", "", "", "", "", "", "", ""}
        );

        MaterialService.MaterialImportResult result = materialService.importMaterials("materials.xlsx", workbook);

        assertThat(result.updatedCount()).isEqualTo(1);
        assertThat(result.createdCount()).isEqualTo(0);
        assertThat(result.failedCount()).isEqualTo(1);
        assertThat(existing.getMaterialName()).isEqualTo("更新成功");
        assertThat(result.errorFileToken()).isNotBlank();
        String errorCsv = new String(materialService.loadImportErrorFile(result.errorFileToken()), java.nio.charset.StandardCharsets.UTF_8);
        assertThat(errorCsv).contains("MAT-002");
        assertThat(errorCsv).contains("materialName");
    }

    @Test
    @DisplayName("下载过期错误文件时应返回未找到并清理记录")
    void loadImportErrorFile_whenExpired_shouldThrowNotFound() {
        when(fileObjectService.loadTemporaryFile("token", "MATERIAL_IMPORT_ERROR"))
                .thenThrow(new ResourceNotFoundException("导入错误文件不存在: token"));

        assertThatThrownBy(() -> materialService.loadImportErrorFile("token"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("导入错误文件不存在");
    }

    @Test
    @DisplayName("应定时清理过期错误文件")
    void cleanupExpiredImportErrorFiles_shouldDeleteExpiredRecords() {
        when(scheduledTaskLockService.tryLock("material:file-cleanup", "material-file-cleanup-scheduler")).thenReturn(true);

        materialService.cleanupExpiredImportErrorFiles();

        verify(fileObjectService).cleanupExpiredFiles("MATERIAL_IMPORT_ERROR");
        verify(fileObjectService).cleanupExpiredFiles("MATERIAL_EXPORT");
        verify(fileObjectService).cleanupExpiredFiles("MATERIAL_TEMPLATE");
        verify(scheduledTaskLockService).unlock("material:file-cleanup", "material-file-cleanup-scheduler");
    }

    @Test
    @DisplayName("未获取到清理锁时不应执行文件清理")
    void cleanupExpiredImportErrorFiles_shouldSkipWhenLockNotAcquired() {
        when(scheduledTaskLockService.tryLock("material:file-cleanup", "material-file-cleanup-scheduler")).thenReturn(false);

        materialService.cleanupExpiredImportErrorFiles();

        verify(fileObjectService, never()).cleanupExpiredFiles(any());
    }

    private byte[] createMaterialWorkbook(String[]... rows) throws Exception {
        try (var workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
             var outputStream = new java.io.ByteArrayOutputStream()) {
            var sheet = workbook.createSheet("materials");
            var header = sheet.createRow(0);
            String[] headers = {
                    "materialCode", "materialName", "specification", "unit", "enabled", "remark",
                    "colorCode", "rawMaterialType", "defaultLotSize", "minLotSize", "maxLotSize",
                    "allowDelay", "abcClassification", "productGroup"
            };
            for (int i = 0; i < headers.length; i++) {
                header.createCell(i).setCellValue(headers[i]);
            }
            for (int rowIndex = 0; rowIndex < rows.length; rowIndex++) {
                var row = sheet.createRow(rowIndex + 1);
                for (int columnIndex = 0; columnIndex < rows[rowIndex].length; columnIndex++) {
                    row.createCell(columnIndex).setCellValue(rows[rowIndex][columnIndex]);
                }
            }
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }
}
