package com.aps.service;

import ai.timefold.solver.core.api.solver.SolverJob;
import ai.timefold.solver.core.api.solver.SolverManager;
import ai.timefold.solver.core.api.solver.SolverStatus;
import com.aps.domain.annotation.Audited;
import com.aps.domain.entity.Schedule;
import com.aps.domain.enums.AuditAction;
import com.aps.service.exception.ResourceNotFoundException;
import com.aps.service.exception.BusinessException;
import com.aps.service.repository.ScheduleRepository;
import com.aps.solver.converter.ScheduleModelConverter;
import com.aps.solver.model.SchedulePlanningModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleService {

    private final SolverManager<SchedulePlanningModel, UUID> solverManager;
    private final ScheduleRepository scheduleRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final ScheduleModelConverter modelConverter;

    // 存储正在运行的求解任务
    private final Map<UUID, SolverJob<SchedulePlanningModel, UUID>> activeSolverJobs = new ConcurrentHashMap<>();

    @Transactional
    @Audited(action = AuditAction.SCHEDULE_CREATE, resource = "Schedule")
    public Schedule createSchedule(Schedule schedule) {
        String name = "SCH-" + LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        schedule.setName(name);
        return scheduleRepository.save(schedule);
    }

    /**
     * 异步开始求解
     */
    @Audited(action = AuditAction.SCHEDULE_SOLVE, resource = "Schedule")
    public void solveAsync(UUID scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("排产方案不存在"));

        // 检查是否已有求解任务在运行（原子操作）
        SolverJob<SchedulePlanningModel, UUID> existingJob = activeSolverJobs.get(scheduleId);
        if (existingJob != null && existingJob.getSolverStatus() == SolverStatus.SOLVING_ACTIVE) {
            throw new BusinessException("排产方案正在求解中，请勿重复提交");
        }

        log.info("开始异步求解排产方案: {}", scheduleId);

        // 发布求解开始事件
        eventPublisher.publishEvent(new SolveStartedEvent(scheduleId));

        // 转换为 Planning Model
        SchedulePlanningModel planningModel = modelConverter.toPlanningModel(schedule);

        // 启动异步求解
        SolverJob<SchedulePlanningModel, UUID> solverJob = solverManager.solve(scheduleId, planningModel);

        activeSolverJobs.put(scheduleId, solverJob);

        // 异步监听求解完成
        CompletableFuture.runAsync(() -> {
            try {
                SchedulePlanningModel finalBestSolution = solverJob.getFinalBestSolution();
                String finalScore = finalBestSolution.getScore() != null
                    ? finalBestSolution.getScore().toString()
                    : "N/A";

                log.info("求解完成 - Schedule: {}, Final Score: {}", scheduleId, finalScore);

                // 在新事务中重新加载 Schedule 并更新
                updateScheduleWithSolution(scheduleId, finalBestSolution, finalScore);
                activeSolverJobs.remove(scheduleId);

                // 发布求解完成事件
                eventPublisher.publishEvent(new SolveCompletedEvent(scheduleId, finalScore));
            } catch (Exception e) {
                log.error("求解失败或保存结果失败", e);
                activeSolverJobs.remove(scheduleId);
                eventPublisher.publishEvent(new SolveFailedEvent(scheduleId, e.getMessage()));
            }
        });
    }

    /**
     * 在新事务中更新排产结果（避免 LazyInitializationException）
     */
    @Transactional
    public void updateScheduleWithSolution(UUID scheduleId, SchedulePlanningModel finalBestSolution, String finalScore) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("排产方案不存在"));

        // 更新 Schedule Entity
        modelConverter.updateScheduleFromModel(schedule, finalBestSolution);
        schedule.setFinalScore(finalScore);

        // 保存最终结果
        scheduleRepository.save(schedule);
    }

    /**
     * 停止求解
     */
    @Audited(action = AuditAction.SCHEDULE_STOP, resource = "Schedule")
    public void stopSolving(UUID scheduleId) {
        SolverJob<SchedulePlanningModel, UUID> solverJob = activeSolverJobs.get(scheduleId);

        if (solverJob == null) {
            throw new ResourceNotFoundException("未找到正在运行的求解任务");
        }

        log.info("停止求解排产方案: {}", scheduleId);
        solverManager.terminateEarly(scheduleId);
        activeSolverJobs.remove(scheduleId);

        // 发布求解停止事件
        eventPublisher.publishEvent(new SolveStoppedEvent(scheduleId));
    }

    /**
     * 获取求解状态
     */
    public SolverStatus getSolverStatus(UUID scheduleId) {
        SolverJob<SchedulePlanningModel, UUID> solverJob = activeSolverJobs.get(scheduleId);

        if (solverJob == null) {
            return SolverStatus.NOT_SOLVING;
        }

        return solverJob.getSolverStatus();
    }

    @Transactional
    @Audited(action = AuditAction.SCHEDULE_UPDATE, resource = "Schedule")
    public Schedule saveSchedule(Schedule schedule) {
        return scheduleRepository.save(schedule);
    }

    @Transactional(readOnly = true)
    public Schedule getScheduleById(UUID scheduleId) {
        return scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("排产方案不存在"));
    }

    // 求解事件类
    public record SolveStartedEvent(UUID scheduleId) {}
    public record SolveProgressEvent(UUID scheduleId, String currentScore) {}
    public record SolveCompletedEvent(UUID scheduleId, String finalScore) {}
    public record SolveStoppedEvent(UUID scheduleId) {}
    public record SolveFailedEvent(UUID scheduleId, String errorMessage) {}
}
