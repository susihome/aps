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
@DisplayName("认证会话清理调度器测试")
class AuthSessionCleanupSchedulerTest {

    @Mock
    private AuthSessionService authSessionService;

    @Mock
    private ScheduledTaskLockService scheduledTaskLockService;

    @InjectMocks
    private AuthSessionCleanupScheduler authSessionCleanupScheduler;

    @Test
    @DisplayName("获取到分布式锁时应执行清理并释放锁")
    void cleanupExpiredSessions_shouldCleanupAndUnlockWhenLockAcquired() {
        when(scheduledTaskLockService.tryLock("auth:session-cleanup", "auth-session-cleanup-scheduler"))
                .thenReturn(true);

        authSessionCleanupScheduler.cleanupExpiredSessions();

        verify(authSessionService).cleanupExpiredSessions();
        verify(scheduledTaskLockService).unlock("auth:session-cleanup", "auth-session-cleanup-scheduler");
    }

    @Test
    @DisplayName("未获取到分布式锁时应跳过执行")
    void cleanupExpiredSessions_shouldSkipWhenLockNotAcquired() {
        when(scheduledTaskLockService.tryLock("auth:session-cleanup", "auth-session-cleanup-scheduler"))
                .thenReturn(false);

        authSessionCleanupScheduler.cleanupExpiredSessions();

        verify(authSessionService, never()).cleanupExpiredSessions();
        verify(scheduledTaskLockService, never()).unlock("auth:session-cleanup", "auth-session-cleanup-scheduler");
    }
}
