package com.aps.api.controller;

import com.aps.api.dto.AjaxResult;
import com.aps.api.dto.MaterialDto;
import com.aps.domain.entity.Material;
import com.aps.service.MaterialService;
import com.aps.service.exception.ValidationException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/materials")
@RequiredArgsConstructor
public class MaterialController {

    private final MaterialService materialService;

    @GetMapping
    @PreAuthorize("hasAuthority('basedata:material:list')")
    public AjaxResult<List<MaterialDto>> getAllMaterials(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) Integer limit) {
        List<Material> materials = keyword == null || keyword.trim().isEmpty()
                ? materialService.getAllMaterials()
                : materialService.searchMaterials(keyword, limit);
        return AjaxResult.success(materials.stream().map(MaterialDto::fromEntity).toList());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('basedata:material:query')")
    public AjaxResult<MaterialDto> getMaterial(@PathVariable UUID id) {
        Material material = materialService.getMaterialById(id);
        return AjaxResult.success(MaterialDto.fromEntity(material));
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('basedata:material:add') and hasAuthority('basedata:material:edit')")
    public AjaxResult<MaterialImportResponse> importMaterials(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new ValidationException("导入文件不能为空");
        }
        try {
            MaterialService.MaterialImportResult result = materialService.importMaterials(file.getOriginalFilename(), file.getInputStream());
            return AjaxResult.success(new MaterialImportResponse(
                    result.totalCount(),
                    result.createdCount(),
                    result.updatedCount(),
                    result.failedCount(),
                    result.failures(),
                    result.errorFileName(),
                    result.errorFileToken()
            ));
        } catch (java.io.IOException exception) {
            throw new ValidationException("读取导入文件失败");
        }
    }

    @GetMapping("/import-errors/{token}")
    @PreAuthorize("hasAuthority('basedata:material:add') and hasAuthority('basedata:material:edit')")
    public ResponseEntity<byte[]> downloadImportErrorFile(@PathVariable String token) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + materialService.getImportErrorFileName(token) + "\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(materialService.loadImportErrorFile(token));
    }

    @PostMapping("/export-files")
    @PreAuthorize("hasAuthority('basedata:material:list')")
    public AjaxResult<ExportFileResponse> createExportFile(@RequestParam(defaultValue = "xlsx") String format) {
        MaterialService.ExportFileResult result = materialService.exportMaterialsToFile(format);
        return AjaxResult.success(new ExportFileResponse(result.fileName(), result.fileToken()));
    }

    @GetMapping("/exports/{token}")
    @PreAuthorize("hasAuthority('basedata:material:list')")
    public ResponseEntity<byte[]> downloadExportFile(@PathVariable String token) {
        String fileName = materialService.getExportFileName(token);
        MediaType contentType = fileName.endsWith(".csv")
                ? MediaType.parseMediaType("text/csv; charset=UTF-8")
                : MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(contentType)
                .body(materialService.loadExportFile(token));
    }

    @PostMapping("/template-files")
    @PreAuthorize("hasAuthority('basedata:material:list')")
    public AjaxResult<ExportFileResponse> createTemplateFile(@RequestParam(defaultValue = "xlsx") String format) {
        MaterialService.ExportFileResult result = materialService.exportTemplateToFile(format);
        return AjaxResult.success(new ExportFileResponse(result.fileName(), result.fileToken()));
    }

    @GetMapping("/templates/{token}")
    @PreAuthorize("hasAuthority('basedata:material:list')")
    public ResponseEntity<byte[]> downloadTemplateFile(@PathVariable String token) {
        String fileName = materialService.getTemplateFileName(token);
        MediaType contentType = fileName.endsWith(".csv")
                ? MediaType.parseMediaType("text/csv; charset=UTF-8")
                : MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(contentType)
                .body(materialService.loadTemplateFile(token));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('basedata:material:add')")
    public AjaxResult<MaterialDto> createMaterial(@Valid @RequestBody CreateMaterialRequest request) {
        Material material = materialService.createMaterial(
                request.materialCode(), request.materialName(), request.specification(),
                request.unit(), request.enabled(), request.remark(),
                request.colorCode(), request.rawMaterialType(),
                request.defaultLotSize(), request.minLotSize(), request.maxLotSize(),
                request.allowDelay(), request.abcClassification(), request.productGroup());
        return AjaxResult.success(MaterialDto.fromEntity(material));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('basedata:material:edit')")
    public AjaxResult<MaterialDto> updateMaterial(@PathVariable UUID id,
                                                  @Valid @RequestBody UpdateMaterialRequest request) {
        Material material = materialService.updateMaterial(
                id,
                request.materialName(),
                request.specification(),
                request.unit(),
                request.enabled(),
                request.remark(),
                request.colorCode(),
                request.rawMaterialType(),
                request.defaultLotSize(),
                request.minLotSize(),
                request.maxLotSize(),
                request.allowDelay(),
                request.abcClassification(),
                request.productGroup()
        );
        return AjaxResult.success(MaterialDto.fromEntity(material));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('basedata:material:remove')")
    public AjaxResult<Void> deleteMaterial(@PathVariable UUID id) {
        materialService.deleteMaterial(id);
        return AjaxResult.success(null);
    }

    public record CreateMaterialRequest(
            @NotBlank(message = "物料编码不能为空") @Size(max = 64, message = "物料编码长度不能超过64") String materialCode,
            @NotBlank(message = "物料名称不能为空") @Size(max = 120, message = "物料名称长度不能超过120") String materialName,
            @Size(max = 255, message = "规格长度不能超过255") String specification,
            @Size(max = 32, message = "单位长度不能超过32") String unit,
            Boolean enabled,
            @Size(max = 500, message = "备注长度不能超过500") String remark,
            @Size(max = 32, message = "颜色编码长度不能超过32") String colorCode,
            @Size(max = 32, message = "原料类型长度不能超过32") String rawMaterialType,
            Integer defaultLotSize,
            Integer minLotSize,
            Integer maxLotSize,
            Boolean allowDelay,
            @Size(max = 1, message = "ABC分类只能为单个字母") String abcClassification,
            @Size(max = 32, message = "产品组长度不能超过32") String productGroup) {
    }

    public record UpdateMaterialRequest(
            @Size(max = 120, message = "物料名称长度不能超过120") String materialName,
            @Size(max = 255, message = "规格长度不能超过255") String specification,
            @Size(max = 32, message = "单位长度不能超过32") String unit,
            Boolean enabled,
            @Size(max = 500, message = "备注长度不能超过500") String remark,
            @Size(max = 32, message = "颜色编码长度不能超过32") String colorCode,
            @Size(max = 32, message = "原料类型长度不能超过32") String rawMaterialType,
            Integer defaultLotSize,
            Integer minLotSize,
            Integer maxLotSize,
            Boolean allowDelay,
            @Size(max = 1, message = "ABC分类只能为单个字母") String abcClassification,
            @Size(max = 32, message = "产品组长度不能超过32") String productGroup) {
    }

    public record MaterialImportResponse(
            int totalCount,
            int createdCount,
            int updatedCount,
            int failedCount,
            List<MaterialService.MaterialImportFailure> failures,
            String errorFileName,
            String errorFileToken
    ) {
        public MaterialImportResponse {
            failures = List.copyOf(failures);
        }
    }

    public record ExportFileResponse(String fileName, String fileToken) {
    }
}
