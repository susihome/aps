package com.aps.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("待执行任务锁保活调度器测试")
class PendingTaskLockMaintenanceSchedulerTest {

    @Mock
    private PendingTaskLockMaintenanceService pendingTaskLockMaintenanceService;

    @Mock
    private ScheduledTaskLockService scheduledTaskLockService;

    @InjectMocks
    private PendingTaskLockMaintenanceScheduler pendingTaskLockMaintenanceScheduler;

    @Test
    @DisplayName("获取到分布式锁时应执行待处理任务续期")
    void renewPendingTaskLocks_shouldRenewAndUnlockWhenLockAcquired() {
        when(scheduledTaskLockService.tryLock("schedule:pending-lock-renewal", "pending-task-lock-maintenance-scheduler"))
                .thenReturn(true);

        pendingTaskLockMaintenanceScheduler.renewPendingTaskLocks();

        verify(pendingTaskLockMaintenanceService).renewPendingTaskLocks();
        verify(scheduledTaskLockService).unlock("schedule:pending-lock-renewal", "pending-task-lock-maintenance-scheduler");
    }

    @Test
    @DisplayName("未获取到分布式锁时应跳过待处理任务续期")
    void renewPendingTaskLocks_shouldSkipWhenLockNotAcquired() {
        when(scheduledTaskLockService.tryLock("schedule:pending-lock-renewal", "pending-task-lock-maintenance-scheduler"))
                .thenReturn(false);

        pendingTaskLockMaintenanceScheduler.renewPendingTaskLocks();

        verify(pendingTaskLockMaintenanceService, never()).renewPendingTaskLocks();
        verify(scheduledTaskLockService, never()).unlock("schedule:pending-lock-renewal", "pending-task-lock-maintenance-scheduler");
    }
}
