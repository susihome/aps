package com.aps.api.websocket;

import com.aps.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SolveEventListener {

    private final ScheduleProgressPublisher progressPublisher;

    @Async
    @EventListener
    public void handleSolveStarted(ScheduleService.SolveStartedEvent event) {
        log.info("求解开始事件: {}", event.scheduleId());
        progressPublisher.publishProgress(event.scheduleId(), 0, "开始求解");
    }

    @Async
    @EventListener
    public void handleSolveProgress(ScheduleService.SolveProgressEvent event) {
        log.debug("求解进度事件: {}, Score: {}", event.scheduleId(), event.currentScore());
        // 这里可以计算进度百分比，暂时使用固定值
        progressPublisher.publishProgress(event.scheduleId(), 50, event.currentScore());
    }

    @Async
    @EventListener
    public void handleSolveCompleted(ScheduleService.SolveCompletedEvent event) {
        log.info("求解完成事件: {}, Final Score: {}", event.scheduleId(), event.finalScore());
        progressPublisher.publishComplete(event.scheduleId(), event.finalScore());
    }

    @Async
    @EventListener
    public void handleSolveStopped(ScheduleService.SolveStoppedEvent event) {
        log.info("求解停止事件: {}", event.scheduleId());
        progressPublisher.publishConflict(event.scheduleId(), "STOPPED", "求解已手动停止");
    }

    @Async
    @EventListener
    public void handleSolveFailed(ScheduleService.SolveFailedEvent event) {
        log.error("求解失败事件: {}, Error: {}", event.scheduleId(), event.errorMessage());
        progressPublisher.publishConflict(event.scheduleId(), "ERROR", event.errorMessage());
    }
}
