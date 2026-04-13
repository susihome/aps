package com.aps.api.websocket;

import com.aps.service.ScheduleService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("排产事件监听器测试")
class SolveEventListenerTest {

    @Mock
    private ScheduleProgressPublisher progressPublisher;

    @InjectMocks
    private SolveEventListener solveEventListener;

    @Test
    @DisplayName("开始事件应推送初始进度")
    void handleSolveStarted_shouldPublishInitialProgress() {
        UUID scheduleId = UUID.randomUUID();

        solveEventListener.handleSolveStarted(new ScheduleService.SolveStartedEvent(scheduleId));

        verify(progressPublisher).publishProgress(scheduleId, 0, "开始求解", null);
    }

    @Test
    @DisplayName("完成事件应推送完成消息")
    void handleSolveCompleted_shouldPublishComplete() {
        UUID scheduleId = UUID.randomUUID();

        solveEventListener.handleSolveCompleted(new ScheduleService.SolveCompletedEvent(scheduleId, "0hard/-10soft"));

        verify(progressPublisher).publishComplete(scheduleId, "0hard/-10soft");
    }

    @Test
    @DisplayName("失败事件应推送错误通知")
    void handleSolveFailed_shouldPublishConflict() {
        UUID scheduleId = UUID.randomUUID();

        solveEventListener.handleSolveFailed(new ScheduleService.SolveFailedEvent(scheduleId, "solver failed"));

        verify(progressPublisher).publishConflict(scheduleId, "ERROR", "solver failed");
    }
}
