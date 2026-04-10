package com.aps.api.controller;

import com.aps.api.dto.AjaxResult;
import com.aps.api.dto.ResourceCapacityRequests.ResourceCapacityBatchUpdateRequest;
import com.aps.api.dto.ResourceCapacityRequests.ResourceCapacityUpdateRequest;
import com.aps.api.dto.ResourceDto;
import com.aps.domain.entity.Resource;
import com.aps.service.ResourceCapacityService;
import com.aps.service.ResourceCapacityService.ResourceCapacityMonthResult;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/resource-capacities")
@RequiredArgsConstructor
@Validated
public class ResourceCapacityController {

    private final ResourceCapacityService resourceCapacityService;

    @GetMapping("/resources")
    @PreAuthorize("hasAuthority('basedata:resource-capacity:list')")
    public AjaxResult<List<ResourceDto>> getResources() {
        List<Resource> resources = resourceCapacityService.getResources();
        return AjaxResult.success(resources.stream().map(ResourceDto::fromEntity).toList());
    }

    @GetMapping
    @PreAuthorize("hasAuthority('basedata:resource-capacity:list')")
    public AjaxResult<ResourceCapacityMonthResult> getMonthCapacity(
            @RequestParam UUID resourceId,
            @RequestParam @Min(value = 2020, message = "年份不能早于2020年") @Max(value = 2099, message = "年份不能晚于2099年") Integer year,
            @RequestParam @Min(value = 1, message = "月份不能小于1") @Max(value = 12, message = "月份不能大于12") Integer month
    ) {
        return AjaxResult.success(resourceCapacityService.getMonthCapacity(resourceId, year, month));
    }

    @PutMapping("/resources/{resourceId}/days/{date}")
    @PreAuthorize("hasAuthority('basedata:resource-capacity:edit')")
    public AjaxResult<?> updateDay(@PathVariable UUID resourceId,
                                   @PathVariable LocalDate date,
                                   @Valid @RequestBody ResourceCapacityUpdateRequest request) {
        return AjaxResult.success(resourceCapacityService.updateDay(
                resourceId,
                date,
                request.shiftMinutesOverride(),
                request.utilizationRate(),
                request.remark()
        ));
    }

    @PutMapping("/resources/{resourceId}/days/batch")
    @PreAuthorize("hasAuthority('basedata:resource-capacity:batch-edit')")
    public AjaxResult<Void> batchUpdateDays(@PathVariable UUID resourceId,
                                            @Valid @RequestBody ResourceCapacityBatchUpdateRequest request) {
        resourceCapacityService.batchUpdateDays(
                resourceId,
                request.dates(),
                request.shiftMinutesOverride(),
                request.utilizationRate(),
                request.remark()
        );
        return AjaxResult.success();
    }
}
