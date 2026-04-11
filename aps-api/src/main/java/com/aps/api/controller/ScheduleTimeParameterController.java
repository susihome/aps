package com.aps.api.controller;

import com.aps.api.dto.AjaxResult;
import com.aps.api.dto.ScheduleTimeParameterDto;
import com.aps.domain.entity.ScheduleTimeParameter;
import com.aps.service.ScheduleTimeParameterService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/schedule-time-parameters")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'PLANNER')")
public class ScheduleTimeParameterController {

    private final ScheduleTimeParameterService service;

    @GetMapping
    public AjaxResult<List<ScheduleTimeParameterDto>> list() {
        List<ScheduleTimeParameterDto> result = service.findAll().stream()
                .map(ScheduleTimeParameterDto::fromEntity)
                .toList();
        return AjaxResult.success(result);
    }

    @GetMapping("/{id}")
    public AjaxResult<ScheduleTimeParameterDto> getById(@PathVariable UUID id) {
        ScheduleTimeParameter param = service.findById(id);
        return AjaxResult.success(ScheduleTimeParameterDto.fromEntity(param));
    }

    @PostMapping
    public AjaxResult<ScheduleTimeParameterDto> create(@Valid @RequestBody CreateRequest req) {
        ScheduleTimeParameter param = service.create(
                req.resourceId(),
                req.orderFilterStartDays(), req.orderFilterStartTime(),
                req.orderFilterEndDays(), req.orderFilterEndTime(),
                req.planningStartDays(), req.planningStartTime(),
                req.displayStartDays(), req.displayEndDays(),
                req.completionDays(), req.timeScale(), req.factor(), req.exceedPeriod(),
                req.isDefault(), req.enabled(), req.remark());
        return AjaxResult.success(ScheduleTimeParameterDto.fromEntity(param));
    }

    @PutMapping("/{id}")
    public AjaxResult<ScheduleTimeParameterDto> update(@PathVariable UUID id,
                                                        @RequestBody UpdateRequest req) {
        ScheduleTimeParameter param = service.update(
                id,
                req.orderFilterStartDays(), req.orderFilterStartTime(),
                req.orderFilterEndDays(), req.orderFilterEndTime(),
                req.planningStartDays(), req.planningStartTime(),
                req.displayStartDays(), req.displayEndDays(),
                req.completionDays(), req.timeScale(), req.factor(), req.exceedPeriod(),
                req.isDefault(), req.enabled(), req.remark());
        return AjaxResult.success(ScheduleTimeParameterDto.fromEntity(param));
    }

    @DeleteMapping("/{id}")
    public AjaxResult<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return AjaxResult.success(null);
    }

    /**
     * 预览接口：根据参数计算实际日期时间结果
     */
    @GetMapping("/preview")
    public AjaxResult<ScheduleTimeParameterDto> preview(
            @RequestParam(required = false) UUID resourceId) {
        ScheduleTimeParameterService.PreviewResult preview = service.buildPreview(resourceId);
        ScheduleTimeParameterDto dto = ScheduleTimeParameterDto.fromEntity(preview.param());

        dto.setCalculatedOrderFilterStart(preview.orderFilterWindow().start());
        dto.setCalculatedOrderFilterEnd(preview.orderFilterWindow().end());
        dto.setCalculatedPlanningStart(preview.planningStart());
        dto.setCalculatedDisplayStart(preview.displayWindow().start());
        dto.setCalculatedDisplayEnd(preview.displayWindow().end());

        return AjaxResult.success(dto);
    }

    // ===== Request Records =====

    public record CreateRequest(
            UUID resourceId,
            Integer orderFilterStartDays,
            LocalTime orderFilterStartTime,
            Integer orderFilterEndDays,
            LocalTime orderFilterEndTime,
            Integer planningStartDays,
            LocalTime planningStartTime,
            Integer displayStartDays,
            Integer displayEndDays,
            Integer completionDays,
            @Min(1) Integer timeScale,
            Integer factor,
            Integer exceedPeriod,
            Boolean isDefault,
            Boolean enabled,
            String remark) {}

    public record UpdateRequest(
            Integer orderFilterStartDays,
            LocalTime orderFilterStartTime,
            Integer orderFilterEndDays,
            LocalTime orderFilterEndTime,
            Integer planningStartDays,
            LocalTime planningStartTime,
            Integer displayStartDays,
            Integer displayEndDays,
            Integer completionDays,
            Integer timeScale,
            Integer factor,
            Integer exceedPeriod,
            Boolean isDefault,
            Boolean enabled,
            String remark) {}
}
