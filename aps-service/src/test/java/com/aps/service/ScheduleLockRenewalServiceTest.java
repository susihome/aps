package com.aps.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.aps.service.config.ScheduleLockProperties;

import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("排产锁续期服务测试")
class ScheduleLockRenewalServiceTest {

    @Mock
    private ScheduleLockService scheduleLockService;

    @Mock
    private ScheduledExecutorService scheduledExecutorService;

    @Mock
    private ScheduledFuture<Object> scheduledFuture;

    private ScheduleLockRenewalService createService() {
        ScheduleLockProperties properties = new ScheduleLockProperties();
        properties.setRenewFailureRetryCount(3);
        return new ScheduleLockRenewalService(scheduleLockService, scheduledExecutorService, properties);
    }

    @Test
    @DisplayName("启动续期时应创建固定频率任务")
    void start_shouldScheduleRenewalTask() {
        UUID scheduleId = UUID.randomUUID();
        String ownerToken = "owner-token";
        doAnswer(invocation -> scheduledFuture).when(scheduledExecutorService).scheduleAtFixedRate(
                org.mockito.ArgumentMatchers.any(Runnable.class),
                anyLong(),
                anyLong(),
                eq(TimeUnit.SECONDS));
        ScheduleLockRenewalService renewalService = createService();

        renewalService.start(scheduleId, ownerToken, () -> {});

        verify(scheduledExecutorService).scheduleAtFixedRate(
                org.mockito.ArgumentMatchers.any(Runnable.class),
                eq(ScheduleLockRenewalService.RENEW_INTERVAL_SECONDS),
                eq(ScheduleLockRenewalService.RENEW_INTERVAL_SECONDS),
                eq(TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("重复启动同一排产方案续期时不应重复调度")
    void start_whenAlreadyStarted_shouldNotScheduleTwice() {
        UUID scheduleId = UUID.randomUUID();
        String ownerToken = "owner-token";
        doAnswer(invocation -> scheduledFuture).when(scheduledExecutorService).scheduleAtFixedRate(
                org.mockito.ArgumentMatchers.any(Runnable.class),
                anyLong(),
                anyLong(),
                eq(TimeUnit.SECONDS));
        ScheduleLockRenewalService renewalService = createService();

        renewalService.start(scheduleId, ownerToken, () -> {});
        renewalService.start(scheduleId, ownerToken, () -> {});

        verify(scheduledExecutorService, times(1)).scheduleAtFixedRate(
                org.mockito.ArgumentMatchers.any(Runnable.class),
                anyLong(),
                anyLong(),
                eq(TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("停止续期时应取消任务")
    void stop_shouldCancelRenewalTask() {
        UUID scheduleId = UUID.randomUUID();
        String ownerToken = "owner-token";
        doAnswer(invocation -> scheduledFuture).when(scheduledExecutorService).scheduleAtFixedRate(
                org.mockito.ArgumentMatchers.any(Runnable.class),
                anyLong(),
                anyLong(),
                eq(TimeUnit.SECONDS));
        ScheduleLockRenewalService renewalService = createService();

        renewalService.start(scheduleId, ownerToken, () -> {});
        renewalService.stop(scheduleId);

        verify(scheduledFuture).cancel(false);
    }

    @Test
    @DisplayName("续期任务执行时应刷新 Redis 锁")
    void start_shouldRenewLockWhenTaskRuns() {
        UUID scheduleId = UUID.randomUUID();
        String ownerToken = "owner-token";
        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        doAnswer(invocation -> scheduledFuture).when(scheduledExecutorService).scheduleAtFixedRate(
                runnableCaptor.capture(),
                anyLong(),
                anyLong(),
                eq(TimeUnit.SECONDS));
        doAnswer(invocation -> true).when(scheduleLockService).renewLock(scheduleId, ownerToken);
        ScheduleLockRenewalService renewalService = createService();

        renewalService.start(scheduleId, ownerToken, () -> {});
        runnableCaptor.getValue().run();

        verify(scheduleLockService).renewLock(scheduleId, ownerToken);
    }

    @Test
    @DisplayName("续期失败时应停止任务并触发回调")
    void start_whenRenewalLost_shouldStopAndInvokeCallback() {
        UUID scheduleId = UUID.randomUUID();
        String ownerToken = "owner-token";
        AtomicInteger callbackCount = new AtomicInteger();
        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        doAnswer(invocation -> scheduledFuture).when(scheduledExecutorService).scheduleAtFixedRate(
                runnableCaptor.capture(),
                anyLong(),
                anyLong(),
                eq(TimeUnit.SECONDS));
        doAnswer(invocation -> false).when(scheduleLockService).renewLock(scheduleId, ownerToken);
        ScheduleLockRenewalService renewalService = createService();

        renewalService.start(scheduleId, ownerToken, callbackCount::incrementAndGet);
        runnableCaptor.getValue().run();

        verify(scheduledFuture).cancel(false);
        verify(scheduleLockService).renewLock(scheduleId, ownerToken);
        org.assertj.core.api.Assertions.assertThat(callbackCount.get()).isEqualTo(1);
    }

    @Test
    @DisplayName("续期发生瞬时异常时达到重试阈值前不应触发回调")
    void start_whenRenewThrowsTemporarily_shouldRetryBeforeCallback() {
        UUID scheduleId = UUID.randomUUID();
        String ownerToken = "owner-token";
        AtomicInteger callbackCount = new AtomicInteger();
        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        doAnswer(invocation -> scheduledFuture).when(scheduledExecutorService).scheduleAtFixedRate(
                runnableCaptor.capture(),
                anyLong(),
                anyLong(),
                eq(TimeUnit.SECONDS));
        org.mockito.Mockito.doThrow(new IllegalStateException("redis timeout"))
                .when(scheduleLockService).renewLock(scheduleId, ownerToken);
        ScheduleLockRenewalService renewalService = createService();

        renewalService.start(scheduleId, ownerToken, callbackCount::incrementAndGet);
        runnableCaptor.getValue().run();
        runnableCaptor.getValue().run();

        verify(scheduleLockService, times(2)).renewLock(scheduleId, ownerToken);
        org.assertj.core.api.Assertions.assertThat(callbackCount.get()).isZero();
        org.mockito.Mockito.verify(scheduledFuture, times(0)).cancel(false);
    }

    @Test
    @DisplayName("续期异常连续超过阈值时应停止任务并触发回调")
    void start_whenRenewThrowsRepeatedly_shouldStopAfterThreshold() {
        UUID scheduleId = UUID.randomUUID();
        String ownerToken = "owner-token";
        AtomicInteger callbackCount = new AtomicInteger();
        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        doAnswer(invocation -> scheduledFuture).when(scheduledExecutorService).scheduleAtFixedRate(
                runnableCaptor.capture(),
                anyLong(),
                anyLong(),
                eq(TimeUnit.SECONDS));
        org.mockito.Mockito.doThrow(new IllegalStateException("redis timeout"))
                .when(scheduleLockService).renewLock(scheduleId, ownerToken);
        ScheduleLockRenewalService renewalService = createService();

        renewalService.start(scheduleId, ownerToken, callbackCount::incrementAndGet);
        runnableCaptor.getValue().run();
        runnableCaptor.getValue().run();
        runnableCaptor.getValue().run();

        verify(scheduleLockService, times(3)).renewLock(scheduleId, ownerToken);
        verify(scheduledFuture).cancel(false);
        org.assertj.core.api.Assertions.assertThat(callbackCount.get()).isEqualTo(1);
    }
}
