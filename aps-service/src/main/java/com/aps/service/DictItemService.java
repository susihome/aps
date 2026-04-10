package com.aps.service;

import com.aps.domain.annotation.Audited;
import com.aps.domain.entity.DictItem;
import com.aps.domain.entity.DictType;
import com.aps.domain.enums.AuditAction;
import com.aps.service.exception.ResourceConflictException;
import com.aps.service.exception.ResourceNotFoundException;
import com.aps.service.repository.DictItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DictItemService {

    private final DictItemRepository dictItemRepository;
    private final DictTypeService dictTypeService;

    @Transactional(readOnly = true)
    public Page<DictItem> getItemsByType(UUID typeId, String keyword, Boolean enabled, Pageable pageable) {
        dictTypeService.getTypeById(typeId);
        return dictItemRepository.searchByTypeId(typeId, keyword, enabled, pageable);
    }

    @Transactional(readOnly = true)
    public List<DictItem> getEnabledItemsByTypeCode(String typeCode) {
        dictTypeService.getTypeByCode(typeCode.toUpperCase());
        return dictItemRepository.findEnabledItemsByTypeCode(typeCode.toUpperCase());
    }

    @Transactional(readOnly = true)
    public DictItem getItemById(UUID id) {
        return dictItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("字典项不存在: " + id));
    }

    @Transactional
    @Audited(action = AuditAction.CREATE, resource = "DictItem")
    public DictItem createItem(UUID typeId, String itemCode, String itemName, String itemValue,
                               String description, Boolean enabled, Integer sortOrder, Boolean isSystem) {
        DictType dictType = dictTypeService.getTypeById(typeId);
        String normalizedCode = normalizeCode(itemCode);
        if (dictItemRepository.existsByDictTypeIdAndItemCode(typeId, normalizedCode)) {
            throw new ResourceConflictException("DICT_ITEM_CODE_DUPLICATE: 字典项编码已存在");
        }

        DictItem item = new DictItem();
        item.setDictType(dictType);
        item.setItemCode(normalizedCode);
        item.setItemName(normalizeRequiredText(itemName, "字典项名称"));
        item.setItemValue(normalizeRequiredText(itemValue, "字典项值"));
        item.setDescription(normalizeNullableText(description));
        item.setEnabled(enabled != null ? enabled : true);
        item.setSortOrder(sortOrder != null ? sortOrder : 0);
        item.setIsSystem(isSystem != null ? isSystem : false);

        DictItem saved = dictItemRepository.save(item);
        log.info("字典项已创建: {} / {}", dictType.getCode(), saved.getItemCode());
        return saved;
    }

    @Transactional
    @Audited(action = AuditAction.UPDATE, resource = "DictItem")
    public DictItem updateItem(UUID id, String itemCode, String itemName, String itemValue,
                               String description, Boolean enabled, Integer sortOrder, Boolean isSystem) {
        DictItem item = getItemById(id);
        String normalizedCode = normalizeCode(itemCode);
        UUID dictTypeId = item.getDictType().getId();
        if (dictItemRepository.existsByDictTypeIdAndItemCodeAndIdNot(dictTypeId, normalizedCode, id)) {
            throw new ResourceConflictException("DICT_ITEM_CODE_DUPLICATE: 字典项编码已存在");
        }

        item.setItemCode(normalizedCode);
        item.setItemName(normalizeRequiredText(itemName, "字典项名称"));
        item.setItemValue(normalizeRequiredText(itemValue, "字典项值"));
        item.setDescription(normalizeNullableText(description));
        if (enabled != null) {
            item.setEnabled(enabled);
        }
        if (sortOrder != null) {
            item.setSortOrder(sortOrder);
        }
        if (isSystem != null) {
            item.setIsSystem(isSystem);
        }

        DictItem saved = dictItemRepository.save(item);
        log.info("字典项已更新: {}", saved.getItemCode());
        return saved;
    }

    @Transactional
    @Audited(action = AuditAction.UPDATE, resource = "DictItem")
    public DictItem toggleItemEnabled(UUID id, boolean enabled) {
        DictItem item = getItemById(id);
        if (Boolean.valueOf(enabled).equals(item.getEnabled())) {
            return item;
        }
        item.setEnabled(enabled);
        DictItem saved = dictItemRepository.save(item);
        log.info("字典项状态已更新: {} -> {}", saved.getItemCode(), enabled);
        return saved;
    }

    @Transactional
    @Audited(action = AuditAction.DELETE, resource = "DictItem")
    public void deleteItem(UUID id) {
        DictItem item = getItemById(id);
        if (Boolean.TRUE.equals(item.getIsSystem())) {
            throw new ResourceConflictException("系统预置字典项不允许删除");
        }
        dictItemRepository.delete(item);
        log.info("字典项已删除: {}", item.getItemCode());
    }

    private String normalizeCode(String value) {
        String normalized = value == null ? "" : value.trim().toUpperCase();
        if (normalized.isEmpty()) {
            throw new ResourceConflictException("字典项编码不能为空");
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
