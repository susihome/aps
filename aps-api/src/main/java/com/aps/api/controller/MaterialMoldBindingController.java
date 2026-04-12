package com.aps.api.controller;

import com.aps.api.dto.AjaxResult;
import com.aps.api.dto.MaterialMoldBindingDto;
import com.aps.domain.entity.MaterialMoldBinding;
import com.aps.service.MaterialMoldBindingService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/material-mold-bindings")
@RequiredArgsConstructor
public class MaterialMoldBindingController {

    private final MaterialMoldBindingService bindingService;

    @GetMapping
    @PreAuthorize("hasAuthority('basedata:materialmold:list')")
    public AjaxResult<List<MaterialMoldBindingDto>> getAllBindings(
            @RequestParam(required = false) UUID materialId,
            @RequestParam(required = false) UUID moldId) {
        List<MaterialMoldBinding> bindings;
        if (materialId != null) {
            bindings = bindingService.getBindingsByMaterial(materialId);
        } else if (moldId != null) {
            bindings = bindingService.getBindingsByMold(moldId);
        } else {
            bindings = bindingService.getAllBindings();
        }
        return AjaxResult.success(bindings.stream().map(MaterialMoldBindingDto::fromEntity).toList());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('basedata:materialmold:query')")
    public AjaxResult<MaterialMoldBindingDto> getBinding(@PathVariable UUID id) {
        return AjaxResult.success(MaterialMoldBindingDto.fromEntity(bindingService.getBindingById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('basedata:materialmold:add')")
    public AjaxResult<MaterialMoldBindingDto> createBinding(@Valid @RequestBody CreateMaterialMoldBindingRequest request) {
        MaterialMoldBinding binding = bindingService.createBinding(
                request.materialId(),
                request.moldId(),
                request.priority(),
                request.isDefault(),
                request.isPreferred(),
                request.cycleTimeMinutes(),
                request.setupTimeMinutes(),
                request.changeoverTimeMinutes(),
                request.enabled(),
                request.validFrom(),
                request.validTo(),
                request.remark()
        );
        return AjaxResult.success(MaterialMoldBindingDto.fromEntity(binding));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('basedata:materialmold:edit')")
    public AjaxResult<MaterialMoldBindingDto> updateBinding(@PathVariable UUID id,
                                                            @Valid @RequestBody UpdateMaterialMoldBindingRequest request) {
        MaterialMoldBinding binding = bindingService.updateBinding(
                id,
                request.priority(),
                request.isDefault(),
                request.isPreferred(),
                request.cycleTimeMinutes(),
                request.setupTimeMinutes(),
                request.changeoverTimeMinutes(),
                request.enabled(),
                request.validFrom(),
                request.validTo(),
                request.remark()
        );
        return AjaxResult.success(MaterialMoldBindingDto.fromEntity(binding));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('basedata:materialmold:remove')")
    public AjaxResult<Void> deleteBinding(@PathVariable UUID id) {
        bindingService.deleteBinding(id);
        return AjaxResult.success(null);
    }

    public record CreateMaterialMoldBindingRequest(
            @NotNull(message = "物料不能为空") UUID materialId,
            @NotNull(message = "模具不能为空") UUID moldId,
            @Min(value = 0, message = "优先级不能小于0") Integer priority,
            Boolean isDefault,
            Boolean isPreferred,
            @Min(value = 1, message = "节拍时间必须大于0") Integer cycleTimeMinutes,
            @Min(value = 0, message = "上模时间不能小于0") Integer setupTimeMinutes,
            @Min(value = 0, message = "换模时间不能小于0") Integer changeoverTimeMinutes,
            Boolean enabled,
            LocalDateTime validFrom,
            LocalDateTime validTo,
            @Size(max = 500, message = "备注长度不能超过500") String remark) {
    }

    public record UpdateMaterialMoldBindingRequest(
            @Min(value = 0, message = "优先级不能小于0") Integer priority,
            Boolean isDefault,
            Boolean isPreferred,
            @Min(value = 1, message = "节拍时间必须大于0") Integer cycleTimeMinutes,
            @Min(value = 0, message = "上模时间不能小于0") Integer setupTimeMinutes,
            @Min(value = 0, message = "换模时间不能小于0") Integer changeoverTimeMinutes,
            Boolean enabled,
            LocalDateTime validFrom,
            LocalDateTime validTo,
            @Size(max = 500, message = "备注长度不能超过500") String remark) {
    }
}
