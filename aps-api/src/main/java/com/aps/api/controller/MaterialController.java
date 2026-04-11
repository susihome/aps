package com.aps.api.controller;

import com.aps.api.dto.AjaxResult;
import com.aps.api.dto.MaterialDto;
import com.aps.domain.entity.Material;
import com.aps.service.MaterialService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
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
    public AjaxResult<List<MaterialDto>> getAllMaterials() {
        List<Material> materials = materialService.getAllMaterials();
        return AjaxResult.success(materials.stream().map(MaterialDto::fromEntity).toList());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('basedata:material:query')")
    public AjaxResult<MaterialDto> getMaterial(@PathVariable UUID id) {
        Material material = materialService.getMaterialById(id);
        return AjaxResult.success(MaterialDto.fromEntity(material));
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
}
