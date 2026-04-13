package com.aps.api.websocket;

import com.aps.service.mq.ScheduleNotificationMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
@DisplayName("排产通知消费者测试")
class ScheduleNotificationConsumerTest {

    @Mock
    private ScheduleProgressPublisher scheduleProgressPublisher;

    @InjectMocks
    private ScheduleNotificationConsumer scheduleNotificationConsumer;

    @Test
    @DisplayName("开始通知应转换为进度推送")
    void consumeStarted_shouldPublishProgress() {
        UUID scheduleId = UUID.randomUUID();

        scheduleNotificationConsumer.consume(new ScheduleNotificationMessage(
                "STARTED", scheduleId, 0, null, "开始求解", LocalDateTime.now()
        ));

        verify(scheduleProgressPublisher).publishProgress(scheduleId, 0, "开始求解", null);
    }

    @Test
    @DisplayName("进度通知应转换为进度推送")
    void consumeProgress_shouldPublishProgress() {
        UUID scheduleId = UUID.randomUUID();

        scheduleNotificationConsumer.consume(new ScheduleNotificationMessage(
                "PROGRESS", scheduleId, 45, "0hard/-20soft", "正在构建求解模型", LocalDateTime.now()
        ));

        verify(scheduleProgressPublisher).publishProgress(scheduleId, 45, "正在构建求解模型", "0hard/-20soft");
    }

    @Test
    @DisplayName("完成通知应转换为完成推送")
    void consumeCompleted_shouldPublishComplete() {
        UUID scheduleId = UUID.randomUUID();

        scheduleNotificationConsumer.consume(new ScheduleNotificationMessage(
                "COMPLETED", scheduleId, 100, "0hard/-10soft", null, LocalDateTime.now()
        ));

        verify(scheduleProgressPublisher).publishComplete(scheduleId, "0hard/-10soft");
    }

    @Test
    @DisplayName("失败通知应转换为错误推送")
    void consumeFailed_shouldPublishConflict() {
        UUID scheduleId = UUID.randomUUID();

        scheduleNotificationConsumer.consume(new ScheduleNotificationMessage(
                "FAILED", scheduleId, null, null, "solver failed", LocalDateTime.now()
        ));

        verify(scheduleProgressPublisher).publishConflict(scheduleId, "ERROR", "solver failed");
    }

    @Test
    @DisplayName("未知通知类型应忽略")
    void consumeUnknown_shouldIgnore() {
        UUID scheduleId = UUID.randomUUID();

        scheduleNotificationConsumer.consume(new ScheduleNotificationMessage(
                "UNKNOWN", scheduleId, null, null, null, LocalDateTime.now()
        ));

        verifyNoMoreInteractions(scheduleProgressPublisher);
    }
}
