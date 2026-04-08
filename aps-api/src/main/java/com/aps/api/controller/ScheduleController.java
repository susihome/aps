package com.aps.api.controller;

import ai.timefold.solver.core.api.solver.SolverStatus;
import com.aps.api.annotation.Audited;
import com.aps.api.dto.AjaxResult;
import com.aps.domain.entity.Schedule;
import com.aps.domain.enums.AuditAction;
import com.aps.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PLANNER')")
    @Audited(action = AuditAction.CREATE, resource = "schedule")
    public AjaxResult<Schedule> createSchedule(@RequestBody Schedule schedule) {
        Schedule created = scheduleService.createSchedule(schedule);
        return AjaxResult.success(created);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PLANNER', 'SUPERVISOR')")
    public AjaxResult<Schedule> getSchedule(@PathVariable UUID id) {
        Schedule schedule = scheduleService.getScheduleById(id);
        return AjaxResult.success(schedule);
    }

    /**
     * 开始异步求解
     */
    @PostMapping("/{id}/solve")
    @PreAuthorize("hasAnyRole('ADMIN', 'PLANNER')")
    @Audited(action = AuditAction.UPDATE, resource = "schedule")
    public AjaxResult<Void> solveSchedule(@PathVariable UUID id) {
        scheduleService.solveAsync(id);
        return AjaxResult.success();
    }

    /**
     * 停止求解
     */
    @PostMapping("/{id}/stop")
    @PreAuthorize("hasAnyRole('ADMIN', 'PLANNER')")
    @Audited(action = AuditAction.UPDATE, resource = "schedule")
    public AjaxResult<Void> stopSolving(@PathVariable UUID id) {
        scheduleService.stopSolving(id);
        return AjaxResult.success();
    }

    /**
     * 获取求解状态
     */
    @GetMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'PLANNER', 'SUPERVISOR')")
    public AjaxResult<SolverStatusResponse> getSolverStatus(@PathVariable UUID id) {
        SolverStatus status = scheduleService.getSolverStatus(id);
        return AjaxResult.success(new SolverStatusResponse(status.name()));
    }

    record SolverStatusResponse(String status) {}
}
