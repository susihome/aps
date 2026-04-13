package com.aps.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthSessionCleanupScheduler {

    private static final String TASK_NAME = "auth:session-cleanup";
    private static final String OWNER_TOKEN = "auth-session-cleanup-scheduler";

    private final AuthSessionService authSessionService;
    private final ScheduledTaskLockService scheduledTaskLockService;

    @Scheduled(cron = "${app.auth.session-cleanup-cron:0 0/10 * * * *}")
    public void cleanupExpiredSessions() {
        if (!scheduledTaskLockService.tryLock(TASK_NAME, OWNER_TOKEN)) {
            return;
        }
        try {
            authSessionService.cleanupExpiredSessions();
        } finally {
            scheduledTaskLockService.unlock(TASK_NAME, OWNER_TOKEN);
        }
    }
}
