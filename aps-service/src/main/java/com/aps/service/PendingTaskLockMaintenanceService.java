package com.aps.service;

import com.aps.domain.entity.ScheduleSolverTask;
import com.aps.domain.enums.SolverTaskStatus;
import com.aps.service.mq.ScheduleNotificationDispatcher;
import com.aps.service.repository.ScheduleSolverTaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PendingTaskLockMaintenanceService {

    static final Duration PENDING_TASK_TIMEOUT = Duration.ofMinutes(15);

    private final ScheduleSolverTaskRepository scheduleSolverTaskRepository;
    private final ScheduleLockService scheduleLockService;
    private final ScheduleNotificationDispatcher scheduleNotificationDispatcher;

    public void renewPendingTaskLocks() {
        failTimedOutPendingTasks();

        List<ScheduleSolverTask> pendingTasks =
                scheduleSolverTaskRepository.findTop100ByStatusOrderByCreateTimeAsc(SolverTaskStatus.PENDING);

        for (ScheduleSolverTask pendingTask : pendingTasks) {
            String ownerToken = pendingTask.getLockOwnerToken();
            if (ownerToken == null || ownerToken.isBlank()) {
                continue;
            }

            boolean renewed = scheduleLockService.renewLock(pendingTask.getScheduleId(), ownerToken);
            if (!renewed) {
                log.debug("待执行排产任务锁续期未生效: taskId={}, scheduleId={}",
                        pendingTask.getId(), pendingTask.getScheduleId());
            }
        }
    }

    void failTimedOutPendingTasks() {
        LocalDateTime expiredBefore = LocalDateTime.now().minus(PENDING_TASK_TIMEOUT);
        List<ScheduleSolverTask> expiredPendingTasks =
                scheduleSolverTaskRepository.findTop100ByStatusAndCreateTimeBeforeOrderByCreateTimeAsc(
                        SolverTaskStatus.PENDING,
                        expiredBefore);

        for (ScheduleSolverTask expiredTask : expiredPendingTasks) {
            String errorMessage = "排产任务等待执行超时，已自动终止";
            expiredTask.setStatus(SolverTaskStatus.FAILED);
            expiredTask.setErrorMessage(errorMessage);
            expiredTask.setFinishedAt(LocalDateTime.now());
            scheduleSolverTaskRepository.save(expiredTask);

            String ownerToken = expiredTask.getLockOwnerToken();
            if (ownerToken != null && !ownerToken.isBlank()) {
                scheduleLockService.unlock(expiredTask.getScheduleId(), ownerToken);
            }
            scheduleNotificationDispatcher.publishFailed(expiredTask.getScheduleId(), errorMessage);
        }
    }
}
