package com.aps.service;

import com.aps.domain.annotation.Audited;
import com.aps.domain.entity.DictType;
import com.aps.domain.enums.AuditAction;
import com.aps.service.exception.ResourceConflictException;
import com.aps.service.exception.ResourceNotFoundException;
import com.aps.service.repository.DictItemRepository;
import com.aps.service.repository.DictTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DictTypeService {

    private final DictTypeRepository dictTypeRepository;
    private final DictItemRepository dictItemRepository;

    @Transactional(readOnly = true)
    public Page<DictType> getTypes(String keyword, Boolean enabled, Pageable pageable) {
        return dictTypeRepository.search(keyword, enabled, pageable);
    }

    @Transactional(readOnly = true)
    public DictType getTypeById(UUID id) {
        return dictTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("字典类型不存在: " + id));
    }

    @Transactional(readOnly = true)
    public DictType getTypeByCode(String code) {
        return dictTypeRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("字典类型不存在: " + code));
    }

    @Transactional
    @Audited(action = AuditAction.CREATE, resource = "DictType")
    public DictType createType(String code, String name, String description, Boolean enabled, Integer sortOrder) {
        String normalizedCode = normalizeCode(code);
        if (dictTypeRepository.existsByCode(normalizedCode)) {
            throw new ResourceConflictException("DICT_TYPE_CODE_DUPLICATE: 字典类型编码已存在");
        }

        DictType dictType = new DictType();
        dictType.setCode(normalizedCode);
        dictType.setName(normalizeRequiredText(name, "字典类型名称"));
        dictType.setDescription(normalizeNullableText(description));
        dictType.setEnabled(enabled != null ? enabled : true);
        dictType.setSortOrder(sortOrder != null ? sortOrder : 0);

        DictType saved = dictTypeRepository.save(dictType);
        log.info("字典类型已创建: {}", saved.getCode());
        return saved;
    }

    @Transactional
    @Audited(action = AuditAction.UPDATE, resource = "DictType")
    public DictType updateType(UUID id, String code, String name, String description, Boolean enabled, Integer sortOrder) {
        DictType dictType = getTypeById(id);

        String normalizedCode = normalizeCode(code);
        if (dictTypeRepository.existsByCodeAndIdNot(normalizedCode, id)) {
            throw new ResourceConflictException("DICT_TYPE_CODE_DUPLICATE: 字典类型编码已存在");
        }

        dictType.setCode(normalizedCode);
        dictType.setName(normalizeRequiredText(name, "字典类型名称"));
        dictType.setDescription(normalizeNullableText(description));
        if (enabled != null) {
            dictType.setEnabled(enabled);
        }
        if (sortOrder != null) {
            dictType.setSortOrder(sortOrder);
        }

        DictType saved = dictTypeRepository.save(dictType);
        log.info("字典类型已更新: {}", saved.getCode());
        return saved;
    }

    @Transactional
    @Audited(action = AuditAction.UPDATE, resource = "DictType")
    public DictType toggleTypeEnabled(UUID id, boolean enabled) {
        DictType dictType = getTypeById(id);
        if (Boolean.valueOf(enabled).equals(dictType.getEnabled())) {
            return dictType;
        }
        dictType.setEnabled(enabled);
        DictType saved = dictTypeRepository.save(dictType);
        log.info("字典类型状态已更新: {} -> {}", saved.getCode(), enabled);
        return saved;
    }

    @Transactional
    @Audited(action = AuditAction.DELETE, resource = "DictType")
    public void deleteType(UUID id) {
        DictType dictType = getTypeById(id);
        long itemCount = dictItemRepository.countByDictTypeId(id);
        if (itemCount > 0) {
            throw new ResourceConflictException("DICT_TYPE_HAS_ITEMS: 字典类型下存在字典项，无法删除");
        }
        dictTypeRepository.delete(dictType);
        log.info("字典类型已删除: {}", dictType.getCode());
    }

    private String normalizeCode(String value) {
        String normalized = value == null ? "" : value.trim().toUpperCase();
        if (normalized.isEmpty()) {
            throw new ResourceConflictException("字典类型编码不能为空");
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
