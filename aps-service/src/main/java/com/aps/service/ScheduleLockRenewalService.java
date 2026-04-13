package com.aps.service;

import com.aps.service.config.ScheduleLockProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleLockRenewalService {

    static final long RENEW_INTERVAL_SECONDS = 180L;

    private final ScheduleLockService scheduleLockService;

    @Qualifier("scheduleLockRenewalExecutor")
    private final ScheduledExecutorService scheduledExecutorService;

    private final ScheduleLockProperties scheduleLockProperties;

    private final Map<UUID, ScheduledFuture<?>> renewalTasks = new ConcurrentHashMap<>();
    private final Map<UUID, AtomicInteger> renewalFailureCounters = new ConcurrentHashMap<>();

    public void start(UUID scheduleId, String ownerToken, Runnable onLockLost) {
        renewalTasks.computeIfAbsent(scheduleId, id -> scheduledExecutorService.scheduleAtFixedRate(
                () -> renewQuietly(id, ownerToken, onLockLost),
                RENEW_INTERVAL_SECONDS,
                RENEW_INTERVAL_SECONDS,
                TimeUnit.SECONDS));
    }

    public void stop(UUID scheduleId) {
        ScheduledFuture<?> renewalTask = renewalTasks.remove(scheduleId);
        renewalFailureCounters.remove(scheduleId);
        if (renewalTask != null) {
            renewalTask.cancel(false);
        }
    }

    void renewQuietly(UUID scheduleId, String ownerToken, Runnable onLockLost) {
        try {
            if (!scheduleLockService.renewLock(scheduleId, ownerToken)) {
                log.warn("排产锁续期未生效，owner token 不匹配: {}", scheduleId);
                stop(scheduleId);
                onLockLost.run();
                return;
            }
            renewalFailureCounters.remove(scheduleId);
        } catch (Exception ex) {
            int failureCount = renewalFailureCounters
                    .computeIfAbsent(scheduleId, ignored -> new AtomicInteger())
                    .incrementAndGet();
            log.warn("排产锁续期失败: {}, failureCount={}", scheduleId, failureCount, ex);
            if (failureCount < scheduleLockProperties.getRenewFailureRetryCount()) {
                return;
            }
            stop(scheduleId);
            onLockLost.run();
        }
    }
}
