package com.aps.service;

import ai.timefold.solver.core.api.solver.SolverJob;
import ai.timefold.solver.core.api.solver.SolverManager;
import ai.timefold.solver.core.api.solver.SolverStatus;
import com.aps.domain.annotation.Audited;
import com.aps.domain.entity.Schedule;
import com.aps.domain.entity.ScheduleSolverTask;
import com.aps.domain.enums.AuditAction;
import com.aps.domain.enums.SolverTaskStatus;
import com.aps.domain.enums.SolverTaskType;
import com.aps.domain.enums.TriggerSource;
import com.aps.service.exception.ResourceNotFoundException;
import com.aps.service.exception.BusinessException;
import com.aps.service.mq.ScheduleNotificationDispatcher;
import com.aps.service.mq.ScheduleTaskDispatcher;
import com.aps.service.repository.ScheduleRepository;
import com.aps.service.repository.ScheduleSolverTaskRepository;
import com.aps.solver.converter.ScheduleModelConverter;
import com.aps.solver.model.SchedulePlanningModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleService {

    private final SolverManager<SchedulePlanningModel, UUID> solverManager;
    private final ScheduleRepository scheduleRepository;
    private final ScheduleSolverTaskRepository scheduleSolverTaskRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final ScheduleModelConverter modelConverter;
    private final ScheduleLockService scheduleLockService;
    private final ScheduleLockRenewalService scheduleLockRenewalService;
    private final ScheduleTaskDispatcher scheduleTaskDispatcher;
    private final ScheduleNotificationDispatcher scheduleNotificationDispatcher;

    @Qualifier("scheduleTaskExecutor")
    private final Executor scheduleTaskExecutor;

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
        submitSolveTask(scheduleId, null, TriggerSource.MANUAL);
    }

    @Audited(action = AuditAction.SCHEDULE_SOLVE, resource = "Schedule")
    public ScheduleSolverTask submitSolveTask(UUID scheduleId, UUID triggeredBy, TriggerSource triggerSource) {
        scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("排产方案不存在"));
        String lockOwnerToken = UUID.randomUUID().toString();

        if (!scheduleLockService.tryLock(scheduleId, lockOwnerToken)) {
            throw new BusinessException("排产方案正在求解中，请勿重复提交");
        }

        // 检查是否已有求解任务在运行（单机内存保护，作为补充而非主防线）
        SolverJob<SchedulePlanningModel, UUID> existingJob = activeSolverJobs.get(scheduleId);
        if (existingJob != null && existingJob.getSolverStatus() == SolverStatus.SOLVING_ACTIVE) {
            scheduleLockService.unlock(scheduleId, lockOwnerToken);
            throw new BusinessException("排产方案正在求解中，请勿重复提交");
        }

        ScheduleSolverTask task = new ScheduleSolverTask();
        task.setId(UUID.randomUUID());
        task.setScheduleId(scheduleId);
        task.setTaskType(SolverTaskType.PLAN);
        task.setTriggeredBy(triggeredBy);
        task.setTriggerSource(triggerSource);
        task.setStatus(SolverTaskStatus.PENDING);
        task.setProgress(0);
        task.setLockOwnerToken(lockOwnerToken);
        ScheduleSolverTask savedTask = scheduleSolverTaskRepository.save(task);
        scheduleTaskDispatcher.dispatch(savedTask.getId(), scheduleId);
        return savedTask;
    }

    public void executeSolveTask(UUID taskId) {
        ScheduleSolverTask task = scheduleSolverTaskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("排产任务不存在: " + taskId));
        UUID scheduleId = task.getScheduleId();
        String lockOwnerToken = task.getLockOwnerToken();
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("排产方案不存在"));
        if (!scheduleLockService.isOwnedBy(scheduleId, lockOwnerToken)) {
            String errorMessage = "排产锁已失效，任务已取消执行";
            log.warn("排产任务启动前锁归属校验失败: taskId={}, scheduleId={}", taskId, scheduleId);
            markTaskFailed(taskId, errorMessage);
            eventPublisher.publishEvent(new SolveFailedEvent(scheduleId, errorMessage));
            scheduleNotificationDispatcher.publishFailed(scheduleId, errorMessage);
            return;
        }

        task.setStatus(SolverTaskStatus.RUNNING);
        task.setStartedAt(LocalDateTime.now());
        scheduleSolverTaskRepository.save(task);

        log.info("开始异步求解排产方案: {}", scheduleId);
        eventPublisher.publishEvent(new SolveStartedEvent(scheduleId));
        scheduleNotificationDispatcher.publishStarted(scheduleId);
        scheduleLockRenewalService.start(
                scheduleId,
                lockOwnerToken,
                () -> handleLockLostDuringSolve(taskId, scheduleId, lockOwnerToken));

        updateTaskProgress(taskId, 20);
        scheduleNotificationDispatcher.publishProgress(scheduleId, 20, "正在构建求解模型", null);
        SchedulePlanningModel planningModel = modelConverter.toPlanningModel(schedule);
        AtomicInteger solvingProgress = new AtomicInteger(40);
        SolverJob<SchedulePlanningModel, UUID> solverJob = solverManager.solveAndListen(
                scheduleId,
                planningModel,
                bestSolution -> handleIntermediateBestSolution(taskId, scheduleId, bestSolution, solvingProgress));
        activeSolverJobs.put(scheduleId, solverJob);
        updateTaskProgress(taskId, 40);
        scheduleNotificationDispatcher.publishProgress(scheduleId, 40, "求解任务已提交，等待结果", null);

        CompletableFuture.runAsync(() -> {
            try {
                SchedulePlanningModel finalBestSolution = solverJob.getFinalBestSolution();
                if (finalBestSolution == null) {
                    throw new BusinessException("求解结果为空");
                }
                String finalScore = finalBestSolution.getScore() != null
                        ? finalBestSolution.getScore().toString()
                        : "N/A";

                log.info("求解完成 - Schedule: {}, Final Score: {}", scheduleId, finalScore);
                updateTaskProgress(taskId, 80);
                scheduleNotificationDispatcher.publishProgress(scheduleId, 80, "求解完成，正在保存结果", finalScore);
                updateScheduleWithSolution(scheduleId, finalBestSolution, finalScore);
                markTaskCompleted(taskId, finalScore);
                activeSolverJobs.remove(scheduleId);
                scheduleLockRenewalService.stop(scheduleId);
                scheduleLockService.unlock(scheduleId, lockOwnerToken);
                eventPublisher.publishEvent(new SolveCompletedEvent(scheduleId, finalScore));
                scheduleNotificationDispatcher.publishCompleted(scheduleId, finalScore);
            } catch (Exception e) {
                log.error("求解失败或保存结果失败", e);
                markTaskFailed(taskId, e.getMessage());
                activeSolverJobs.remove(scheduleId);
                scheduleLockRenewalService.stop(scheduleId);
                scheduleLockService.unlock(scheduleId, lockOwnerToken);
                eventPublisher.publishEvent(new SolveFailedEvent(scheduleId, e.getMessage()));
                scheduleNotificationDispatcher.publishFailed(scheduleId, e.getMessage());
            }
        }, scheduleTaskExecutor);
    }

    void handleLockLostDuringSolve(UUID taskId, UUID scheduleId, String lockOwnerToken) {
        String errorMessage = "排产锁已失效，求解已终止";
        log.warn("排产过程中检测到锁丢失，准备终止求解: taskId={}, scheduleId={}", taskId, scheduleId);
        solverManager.terminateEarly(scheduleId);
        activeSolverJobs.remove(scheduleId);
        markTaskFailed(taskId, errorMessage);
        scheduleLockService.unlock(scheduleId, lockOwnerToken);
        eventPublisher.publishEvent(new SolveFailedEvent(scheduleId, errorMessage));
        scheduleNotificationDispatcher.publishFailed(scheduleId, errorMessage);
    }

    void handleIntermediateBestSolution(UUID taskId,
                                        UUID scheduleId,
                                        SchedulePlanningModel bestSolution,
                                        AtomicInteger solvingProgress) {
        if (bestSolution == null || bestSolution.getScore() == null) {
            return;
        }
        int progress = Math.min(solvingProgress.updateAndGet(current -> Math.min(current + 10, 75)), 75);
        String currentScore = bestSolution.getScore().toString();
        updateTaskProgress(taskId, progress);
        eventPublisher.publishEvent(new SolveProgressEvent(scheduleId, currentScore));
        scheduleNotificationDispatcher.publishProgress(scheduleId, progress, "求解中，发现更优解", currentScore);
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
        scheduleLockRenewalService.stop(scheduleId);
        scheduleSolverTaskRepository.findFirstByScheduleIdAndStatusOrderByCreateTimeDesc(scheduleId, SolverTaskStatus.RUNNING)
                .map(ScheduleSolverTask::getLockOwnerToken)
                .ifPresent(lockOwnerToken -> scheduleLockService.unlock(scheduleId, lockOwnerToken));

        // 发布求解停止事件
        eventPublisher.publishEvent(new SolveStoppedEvent(scheduleId));
        scheduleNotificationDispatcher.publishStopped(scheduleId);
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

    @Transactional(readOnly = true)
    public ScheduleSolverTask getLatestSolverTask(UUID scheduleId) {
        return scheduleSolverTaskRepository.findFirstByScheduleIdOrderByCreateTimeDesc(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("未找到排产任务: " + scheduleId));
    }

    @Transactional(readOnly = true)
    public List<ScheduleSolverTask> listSolverTasks(UUID scheduleId) {
        return scheduleSolverTaskRepository.findTop10ByScheduleIdOrderByCreateTimeDesc(scheduleId);
    }

    @Transactional(readOnly = true)
    public ScheduleSolverTask getSolverTask(UUID taskId) {
        return scheduleSolverTaskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("排产任务不存在: " + taskId));
    }

    @Transactional
    public ScheduleSolverTask retryFailedTask(UUID taskId) {
        ScheduleSolverTask failedTask = scheduleSolverTaskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("排产任务不存在: " + taskId));

        if (failedTask.getStatus() != SolverTaskStatus.FAILED) {
            throw new BusinessException("仅允许重试失败任务");
        }

        return submitSolveTask(failedTask.getScheduleId(), failedTask.getTriggeredBy(), failedTask.getTriggerSource());
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

    @Transactional
    public void markTaskCompleted(UUID taskId, String score) {
        ScheduleSolverTask task = scheduleSolverTaskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("排产任务不存在: " + taskId));
        task.setStatus(SolverTaskStatus.SUCCESS);
        task.setScore(score);
        task.setProgress(100);
        task.setFinishedAt(LocalDateTime.now());
        scheduleSolverTaskRepository.save(task);
    }

    @Transactional
    public void markTaskFailed(UUID taskId, String errorMessage) {
        ScheduleSolverTask task = scheduleSolverTaskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("排产任务不存在: " + taskId));
        task.setStatus(SolverTaskStatus.FAILED);
        task.setErrorMessage(errorMessage);
        task.setFinishedAt(LocalDateTime.now());
        scheduleSolverTaskRepository.save(task);
    }

    @Transactional
    public void updateTaskProgress(UUID taskId, int progress) {
        ScheduleSolverTask task = scheduleSolverTaskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("排产任务不存在: " + taskId));
        task.setProgress(progress);
        scheduleSolverTaskRepository.save(task);
    }

    // 求解事件类
    public record SolveStartedEvent(UUID scheduleId) {}
    public record SolveProgressEvent(UUID scheduleId, String currentScore) {}
    public record SolveCompletedEvent(UUID scheduleId, String finalScore) {}
    public record SolveStoppedEvent(UUID scheduleId) {}
    public record SolveFailedEvent(UUID scheduleId, String errorMessage) {}
}
