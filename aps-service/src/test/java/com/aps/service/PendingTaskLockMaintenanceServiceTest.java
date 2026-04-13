package com.aps.service;

import com.aps.domain.entity.ScheduleSolverTask;
import com.aps.domain.enums.SolverTaskStatus;
import com.aps.service.repository.ScheduleSolverTaskRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.any;

@ExtendWith(MockitoExtension.class)
@DisplayName("待执行任务锁保活服务测试")
class PendingTaskLockMaintenanceServiceTest {

    @Mock
    private ScheduleSolverTaskRepository scheduleSolverTaskRepository;

    @Mock
    private ScheduleLockService scheduleLockService;

    @Mock
    private com.aps.service.mq.ScheduleNotificationDispatcher scheduleNotificationDispatcher;

    @InjectMocks
    private PendingTaskLockMaintenanceService pendingTaskLockMaintenanceService;

    @Test
    @DisplayName("应续期待执行任务的排产锁")
    void renewPendingTaskLocks_shouldRenewLocks() {
        ScheduleSolverTask firstTask = pendingTask(UUID.randomUUID(), "owner-1");
        ScheduleSolverTask secondTask = pendingTask(UUID.randomUUID(), "owner-2");
        when(scheduleSolverTaskRepository.findTop100ByStatusAndCreateTimeBeforeOrderByCreateTimeAsc(
                eq(SolverTaskStatus.PENDING),
                any(LocalDateTime.class)))
                .thenReturn(List.of());
        when(scheduleSolverTaskRepository.findTop100ByStatusOrderByCreateTimeAsc(SolverTaskStatus.PENDING))
                .thenReturn(List.of(firstTask, secondTask));

        pendingTaskLockMaintenanceService.renewPendingTaskLocks();

        verify(scheduleLockService).renewLock(firstTask.getScheduleId(), "owner-1");
        verify(scheduleLockService).renewLock(secondTask.getScheduleId(), "owner-2");
    }

    @Test
    @DisplayName("owner token 为空时应跳过续期")
    void renewPendingTaskLocks_whenOwnerTokenMissing_shouldSkip() {
        ScheduleSolverTask pendingTask = pendingTask(UUID.randomUUID(), " ");
        when(scheduleSolverTaskRepository.findTop100ByStatusAndCreateTimeBeforeOrderByCreateTimeAsc(
                eq(SolverTaskStatus.PENDING),
                any(LocalDateTime.class)))
                .thenReturn(List.of());
        when(scheduleSolverTaskRepository.findTop100ByStatusOrderByCreateTimeAsc(SolverTaskStatus.PENDING))
                .thenReturn(List.of(pendingTask));

        pendingTaskLockMaintenanceService.renewPendingTaskLocks();

        verify(scheduleLockService, never()).renewLock(pendingTask.getScheduleId(), " ");
    }

    @Test
    @DisplayName("超时的待执行任务应自动失败并释放锁")
    void renewPendingTaskLocks_whenPendingTaskTimedOut_shouldFailTask() {
        ScheduleSolverTask expiredTask = pendingTask(UUID.randomUUID(), "owner-1");
        expiredTask.setCreateTime(LocalDateTime.now().minusMinutes(16));
        when(scheduleSolverTaskRepository.findTop100ByStatusAndCreateTimeBeforeOrderByCreateTimeAsc(
                eq(SolverTaskStatus.PENDING),
                any(LocalDateTime.class)))
                .thenReturn(List.of(expiredTask));
        when(scheduleSolverTaskRepository.findTop100ByStatusOrderByCreateTimeAsc(SolverTaskStatus.PENDING))
                .thenReturn(List.of());
        when(scheduleSolverTaskRepository.save(any(ScheduleSolverTask.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        pendingTaskLockMaintenanceService.renewPendingTaskLocks();

        verify(scheduleSolverTaskRepository).save(expiredTask);
        verify(scheduleLockService).unlock(expiredTask.getScheduleId(), "owner-1");
        verify(scheduleNotificationDispatcher).publishFailed(expiredTask.getScheduleId(), "排产任务等待执行超时，已自动终止");
    }

    private ScheduleSolverTask pendingTask(UUID scheduleId, String ownerToken) {
        ScheduleSolverTask pendingTask = new ScheduleSolverTask();
        pendingTask.setId(UUID.randomUUID());
        pendingTask.setScheduleId(scheduleId);
        pendingTask.setStatus(SolverTaskStatus.PENDING);
        pendingTask.setLockOwnerToken(ownerToken);
        return pendingTask;
    }
}
