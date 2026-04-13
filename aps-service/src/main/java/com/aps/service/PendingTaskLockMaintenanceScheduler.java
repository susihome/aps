package com.aps.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PendingTaskLockMaintenanceScheduler {

    private static final String TASK_NAME = "schedule:pending-lock-renewal";
    private static final String OWNER_TOKEN = "pending-task-lock-maintenance-scheduler";

    private final PendingTaskLockMaintenanceService pendingTaskLockMaintenanceService;
    private final ScheduledTaskLockService scheduledTaskLockService;

    @Scheduled(fixedDelayString = "${app.schedule.pending-lock-renewal-interval-ms:60000}")
    public void renewPendingTaskLocks() {
        if (!scheduledTaskLockService.tryLock(TASK_NAME, OWNER_TOKEN)) {
            return;
        }
        try {
            pendingTaskLockMaintenanceService.renewPendingTaskLocks();
        } finally {
            scheduledTaskLockService.unlock(TASK_NAME, OWNER_TOKEN);
        }
    }
}
