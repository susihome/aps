package com.aps.api.controller;

import com.aps.api.dto.AjaxResult;
import com.aps.api.dto.DictItemDto;
import com.aps.api.dto.DictItemRequest;
import com.aps.api.dto.DictTypeDto;
import com.aps.api.dto.DictTypeRequest;
import com.aps.domain.entity.DictItem;
import com.aps.domain.entity.DictType;
import com.aps.service.DictItemService;
import com.aps.service.DictTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/dictionaries")
@RequiredArgsConstructor
public class DictionaryController {

    private final DictTypeService dictTypeService;
    private final DictItemService dictItemService;

    @GetMapping("/types")
    @PreAuthorize("hasAuthority('system:dict:type:list')")
    public AjaxResult<Page<DictTypeDto>> getTypes(
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean enabled
    ) {
        Pageable pageable = PageRequest.of(Math.max(pageNo - 1, 0), Math.min(Math.max(pageSize, 1), 100),
                Sort.by(Sort.Direction.ASC, "sortOrder").and(Sort.by(Sort.Direction.ASC, "id")));
        Page<DictType> page = dictTypeService.getTypes(keyword, enabled, pageable);
        return AjaxResult.success(page.map(DictTypeDto::fromEntity));
    }

    @PostMapping("/types")
    @PreAuthorize("hasAuthority('system:dict:type:add')")
    public AjaxResult<DictTypeDto> createType(@Valid @RequestBody DictTypeRequest request) {
        DictType created = dictTypeService.createType(
                request.code(), request.name(), request.description(), request.enabled(), request.sortOrder());
        return AjaxResult.success(DictTypeDto.fromEntity(created));
    }

    @PutMapping("/types/{id}")
    @PreAuthorize("hasAuthority('system:dict:type:edit')")
    public AjaxResult<DictTypeDto> updateType(@PathVariable UUID id, @Valid @RequestBody DictTypeRequest request) {
        DictType updated = dictTypeService.updateType(
                id, request.code(), request.name(), request.description(), request.enabled(), request.sortOrder());
        return AjaxResult.success(DictTypeDto.fromEntity(updated));
    }

    @PatchMapping("/types/{id}/enabled")
    @PreAuthorize("hasAuthority('system:dict:type:edit')")
    public AjaxResult<DictTypeDto> toggleTypeEnabled(@PathVariable UUID id, @RequestBody Map<String, Boolean> body) {
        Boolean enabled = body.get("enabled");
        if (enabled == null) {
            return AjaxResult.error(400, "enabled 参数不能为空", null);
        }
        DictType updated = dictTypeService.toggleTypeEnabled(id, enabled);
        return AjaxResult.success(DictTypeDto.fromEntity(updated));
    }

    @DeleteMapping("/types/{id}")
    @PreAuthorize("hasAuthority('system:dict:type:remove')")
    public AjaxResult<Void> deleteType(@PathVariable UUID id) {
        dictTypeService.deleteType(id);
        return AjaxResult.success(null);
    }

    @GetMapping("/types/{typeId}/items")
    @PreAuthorize("hasAuthority('system:dict:item:list')")
    public AjaxResult<Page<DictItemDto>> getItemsByType(
            @PathVariable UUID typeId,
            @RequestParam(defaultValue = "1") int pageNo,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean enabled
    ) {
        Pageable pageable = PageRequest.of(Math.max(pageNo - 1, 0), Math.min(Math.max(pageSize, 1), 100),
                Sort.by(Sort.Direction.ASC, "sortOrder").and(Sort.by(Sort.Direction.ASC, "id")));
        Page<DictItem> page = dictItemService.getItemsByType(typeId, keyword, enabled, pageable);
        return AjaxResult.success(page.map(DictItemDto::fromEntity));
    }

    @PostMapping("/types/{typeId}/items")
    @PreAuthorize("hasAuthority('system:dict:item:add')")
    public AjaxResult<DictItemDto> createItem(@PathVariable UUID typeId, @Valid @RequestBody DictItemRequest request) {
        DictItem created = dictItemService.createItem(
                typeId,
                request.itemCode(),
                request.itemName(),
                request.itemValue(),
                request.description(),
                request.enabled(),
                request.sortOrder(),
                request.isSystem());
        return AjaxResult.success(DictItemDto.fromEntity(created));
    }

    @PutMapping("/items/{id}")
    @PreAuthorize("hasAuthority('system:dict:item:edit')")
    public AjaxResult<DictItemDto> updateItem(@PathVariable UUID id, @Valid @RequestBody DictItemRequest request) {
        DictItem updated = dictItemService.updateItem(
                id,
                request.itemCode(),
                request.itemName(),
                request.itemValue(),
                request.description(),
                request.enabled(),
                request.sortOrder(),
                request.isSystem());
        return AjaxResult.success(DictItemDto.fromEntity(updated));
    }

    @PatchMapping("/items/{id}/enabled")
    @PreAuthorize("hasAuthority('system:dict:item:edit')")
    public AjaxResult<DictItemDto> toggleItemEnabled(@PathVariable UUID id, @RequestBody Map<String, Boolean> body) {
        Boolean enabled = body.get("enabled");
        if (enabled == null) {
            return AjaxResult.error(400, "enabled 参数不能为空", null);
        }
        DictItem updated = dictItemService.toggleItemEnabled(id, enabled);
        return AjaxResult.success(DictItemDto.fromEntity(updated));
    }

    @DeleteMapping("/items/{id}")
    @PreAuthorize("hasAuthority('system:dict:item:remove')")
    public AjaxResult<Void> deleteItem(@PathVariable UUID id) {
        dictItemService.deleteItem(id);
        return AjaxResult.success(null);
    }

    @GetMapping("/{typeCode}/enabled-items")
    @PreAuthorize("hasAuthority('system:dict:query')")
    public AjaxResult<List<DictItemDto>> getEnabledItemsByTypeCode(@PathVariable String typeCode) {
        List<DictItem> items = dictItemService.getEnabledItemsByTypeCode(typeCode);
        return AjaxResult.success(items.stream().map(DictItemDto::fromEntity).toList());
    }
}
