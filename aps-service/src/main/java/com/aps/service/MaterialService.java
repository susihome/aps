package com.aps.service;

import com.aps.domain.annotation.Audited;
import com.aps.domain.entity.Material;
import com.aps.domain.enums.AuditAction;
import com.aps.service.exception.ResourceConflictException;
import com.aps.service.exception.ResourceNotFoundException;
import com.aps.service.repository.MaterialRepository;
import com.aps.service.repository.OperationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MaterialService {

    private final MaterialRepository materialRepository;
    private final OperationRepository operationRepository;

    @Transactional(readOnly = true)
    public List<Material> getAllMaterials() {
        return materialRepository.findAllByOrderByMaterialCodeAsc();
    }

    @Transactional(readOnly = true)
    public Material getMaterialById(UUID id) {
        return materialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("物料不存在: " + id));
    }

    @Transactional
    @Audited(action = AuditAction.MATERIAL_CREATE, resource = "Material")
    public Material createMaterial(String materialCode, String materialName, String specification,
                                   String unit, Boolean enabled, String remark,
                                   String colorCode, String rawMaterialType,
                                   Integer defaultLotSize, Integer minLotSize, Integer maxLotSize,
                                   Boolean allowDelay, String abcClassification, String productGroup) {
        String normalizedCode = normalizeCode(materialCode, "物料编码");
        if (materialRepository.existsByMaterialCode(normalizedCode)) {
            throw new ResourceConflictException("物料编码已存在: " + normalizedCode);
        }

        Material material = new Material();
        material.setMaterialCode(normalizedCode);
        material.setMaterialName(normalizeRequiredText(materialName, "物料名称"));
        material.setSpecification(normalizeNullableText(specification));
        material.setUnit(normalizeNullableText(unit));
        material.setEnabled(enabled == null || enabled);
        material.setRemark(normalizeNullableText(remark));
        material.setColorCode(normalizeNullableText(colorCode));
        material.setRawMaterialType(normalizeNullableText(rawMaterialType));
        material.setDefaultLotSize(defaultLotSize);
        material.setMinLotSize(minLotSize);
        material.setMaxLotSize(maxLotSize);
        material.setAllowDelay(allowDelay);
        material.setAbcClassification(normalizeNullableText(abcClassification));
        material.setProductGroup(normalizeNullableText(productGroup));
        return materialRepository.save(material);
    }

    @Transactional
    @Audited(action = AuditAction.MATERIAL_UPDATE, resource = "Material")
    public Material updateMaterial(UUID id, String materialName, String specification,
                                   String unit, Boolean enabled, String remark,
                                   String colorCode, String rawMaterialType,
                                   Integer defaultLotSize, Integer minLotSize, Integer maxLotSize,
                                   Boolean allowDelay, String abcClassification, String productGroup) {
        Material material = getMaterialById(id);
        if (materialName != null) {
            material.setMaterialName(normalizeRequiredText(materialName, "物料名称"));
        }
        if (specification != null) {
            material.setSpecification(normalizeNullableText(specification));
        }
        if (unit != null) {
            material.setUnit(normalizeNullableText(unit));
        }
        if (enabled != null) {
            material.setEnabled(enabled);
        }
        if (remark != null) {
            material.setRemark(normalizeNullableText(remark));
        }
        if (colorCode != null) {
            material.setColorCode(normalizeNullableText(colorCode));
        }
        if (rawMaterialType != null) {
            material.setRawMaterialType(normalizeNullableText(rawMaterialType));
        }
        if (defaultLotSize != null) {
            material.setDefaultLotSize(defaultLotSize);
        }
        if (minLotSize != null) {
            material.setMinLotSize(minLotSize);
        }
        if (maxLotSize != null) {
            material.setMaxLotSize(maxLotSize);
        }
        material.setAllowDelay(allowDelay);
        if (abcClassification != null) {
            material.setAbcClassification(normalizeNullableText(abcClassification));
        }
        if (productGroup != null) {
            material.setProductGroup(normalizeNullableText(productGroup));
        }
        return materialRepository.save(material);
    }

    @Transactional
    @Audited(action = AuditAction.MATERIAL_DELETE, resource = "Material")
    public void deleteMaterial(UUID id) {
        if (!materialRepository.existsById(id)) {
            throw new ResourceNotFoundException("物料不存在: " + id);
        }
        if (operationRepository.existsByRequiredMaterial_Id(id)) {
            throw new ResourceConflictException("该物料已被工序引用，无法删除");
        }
        materialRepository.deleteById(id);
    }

    private String normalizeCode(String value, String fieldName) {
        String normalized = value == null ? "" : value.trim().toUpperCase();
        if (normalized.isEmpty()) {
            throw new ResourceConflictException(fieldName + "不能为空");
        }
        return normalized;
    }

    private String normalizeRequiredText(String value, String fieldName) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.isEmpty()) {
            throw new ResourceConflictException(fieldName + "不能为空");
        }
        return normalized;
    }

    private String normalizeNullableText(String value) {
        String normalized = value == null ? "" : value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
