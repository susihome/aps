package com.aps.api.controller;

import ai.timefold.solver.core.api.solver.SolverStatus;
import com.aps.api.annotation.Audited;
import com.aps.api.dto.AjaxResult;
import com.aps.domain.entity.Schedule;
import com.aps.domain.entity.ScheduleSolverTask;
import com.aps.domain.enums.AuditAction;
import com.aps.domain.enums.TriggerSource;
import com.aps.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
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
    public AjaxResult<SolverTaskResponse> solveSchedule(@PathVariable UUID id) {
        ScheduleSolverTask task = scheduleService.submitSolveTask(id, null, TriggerSource.MANUAL);
        return AjaxResult.success(SolverTaskResponse.fromEntity(task));
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

    @GetMapping("/{id}/solver-tasks/latest")
    @PreAuthorize("hasAnyRole('ADMIN', 'PLANNER', 'SUPERVISOR')")
    public AjaxResult<SolverTaskResponse> getLatestSolverTask(@PathVariable UUID id) {
        return AjaxResult.success(SolverTaskResponse.fromEntity(scheduleService.getLatestSolverTask(id)));
    }

    @GetMapping("/{id}/solver-tasks")
    @PreAuthorize("hasAnyRole('ADMIN', 'PLANNER', 'SUPERVISOR')")
    public AjaxResult<List<SolverTaskResponse>> listSolverTasks(@PathVariable UUID id) {
        return AjaxResult.success(scheduleService.listSolverTasks(id).stream()
                .map(SolverTaskResponse::fromEntity)
                .toList());
    }

    @GetMapping("/solver-tasks/{taskId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PLANNER', 'SUPERVISOR')")
    public AjaxResult<SolverTaskResponse> getSolverTask(@PathVariable UUID taskId) {
        return AjaxResult.success(SolverTaskResponse.fromEntity(scheduleService.getSolverTask(taskId)));
    }

    @PostMapping("/solver-tasks/{taskId}/retry")
    @PreAuthorize("hasAnyRole('ADMIN', 'PLANNER')")
    @Audited(action = AuditAction.UPDATE, resource = "schedule")
    public AjaxResult<SolverTaskResponse> retrySolverTask(@PathVariable UUID taskId) {
        return AjaxResult.success(SolverTaskResponse.fromEntity(scheduleService.retryFailedTask(taskId)));
    }

    record SolverStatusResponse(String status) {}

    record SolverTaskResponse(
            UUID taskId,
            UUID scheduleId,
            String taskType,
            String triggerSource,
            String status,
            String score,
            Integer progress,
            String errorMessage,
            LocalDateTime startedAt,
            LocalDateTime finishedAt
    ) {
        static SolverTaskResponse fromEntity(ScheduleSolverTask task) {
            return new SolverTaskResponse(
                    task.getId(),
                    task.getScheduleId(),
                    task.getTaskType().name(),
                    task.getTriggerSource().name(),
                    task.getStatus().name(),
                    task.getScore(),
                    task.getProgress(),
                    task.getErrorMessage(),
                    task.getStartedAt(),
                    task.getFinishedAt()
            );
        }
    }
}
