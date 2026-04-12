package com.aps.api.controller;

import com.aps.api.dto.AjaxResult;
import com.aps.api.dto.MoldDto;
import com.aps.domain.entity.Mold;
import com.aps.service.MoldService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/molds")
@RequiredArgsConstructor
public class MoldController {

    private final MoldService moldService;

    @GetMapping
    @PreAuthorize("hasAuthority('basedata:mold:list')")
    public AjaxResult<List<MoldDto>> getAllMolds() {
        List<Mold> molds = moldService.getAllMolds();
        return AjaxResult.success(molds.stream().map(MoldDto::fromEntity).toList());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('basedata:mold:query')")
    public AjaxResult<MoldDto> getMold(@PathVariable UUID id) {
        Mold mold = moldService.getMoldById(id);
        return AjaxResult.success(MoldDto.fromEntity(mold));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('basedata:mold:add')")
    public AjaxResult<MoldDto> createMold(@Valid @RequestBody CreateMoldRequest request) {
        Mold mold = moldService.createMold(
                request.moldCode(), request.moldName(), request.cavityCount(),
                request.status(), request.enabled(), request.remark(),
                request.requiredTonnage(), request.maxShotWeight(), request.maintenanceState());
        return AjaxResult.success(MoldDto.fromEntity(mold));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('basedata:mold:edit')")
    public AjaxResult<MoldDto> updateMold(@PathVariable UUID id,
                                          @Valid @RequestBody UpdateMoldRequest request) {
        Mold mold = moldService.updateMold(
                id,
                request.moldName(),
                request.cavityCount(),
                request.status(),
                request.enabled(),
                request.remark(),
                request.requiredTonnage(),
                request.maxShotWeight(),
                request.maintenanceState()
        );
        return AjaxResult.success(MoldDto.fromEntity(mold));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('basedata:mold:remove')")
    public AjaxResult<Void> deleteMold(@PathVariable UUID id) {
        moldService.deleteMold(id);
        return AjaxResult.success(null);
    }

    public record CreateMoldRequest(
            @NotBlank(message = "模具编码不能为空") @Size(max = 64, message = "模具编码长度不能超过64") String moldCode,
            @NotBlank(message = "模具名称不能为空") @Size(max = 120, message = "模具名称长度不能超过120") String moldName,
            @Min(value = 1, message = "模穴数必须大于0") Integer cavityCount,
            @Size(max = 20, message = "状态长度不能超过20") String status,
            Boolean enabled,
            @Size(max = 500, message = "备注长度不能超过500") String remark,
            @Min(value = 0, message = "需求吨位不能小于0") Integer requiredTonnage,
            BigDecimal maxShotWeight,
            @Size(max = 32, message = "保养状态长度不能超过32") String maintenanceState) {
    }

    public record UpdateMoldRequest(
            @Size(max = 120, message = "模具名称长度不能超过120") String moldName,
            @Min(value = 1, message = "模穴数必须大于0") Integer cavityCount,
            @Size(max = 20, message = "状态长度不能超过20") String status,
            Boolean enabled,
            @Size(max = 500, message = "备注长度不能超过500") String remark,
            @Min(value = 0, message = "需求吨位不能小于0") Integer requiredTonnage,
            BigDecimal maxShotWeight,
            @Size(max = 32, message = "保养状态长度不能超过32") String maintenanceState) {
    }
}
