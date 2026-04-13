package com.aps.api.websocket;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("排产进度推送器测试")
class ScheduleProgressPublisherTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private ScheduleProgressPublisher scheduleProgressPublisher;

    @Test
    @DisplayName("推送进度时应发送到排产主题")
    void publishProgress_shouldSendProgressMessage() {
        UUID scheduleId = UUID.randomUUID();

        scheduleProgressPublisher.publishProgress(scheduleId, 25, "正在构建求解模型", "SOLVING");

        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);
        verify(messagingTemplate).convertAndSend(org.mockito.ArgumentMatchers.eq("/topic/schedule/" + scheduleId), payloadCaptor.capture());
        assertThat(payloadCaptor.getValue()).extracting("type").isEqualTo("PROGRESS");
        assertThat(payloadCaptor.getValue()).extracting("progress").isEqualTo(25);
        assertThat(payloadCaptor.getValue()).extracting("currentScore").isEqualTo("SOLVING");
        assertThat(payloadCaptor.getValue()).extracting("message").isEqualTo("正在构建求解模型");
    }

    @Test
    @DisplayName("推送完成时应发送完成消息")
    void publishComplete_shouldSendCompleteMessage() {
        UUID scheduleId = UUID.randomUUID();

        scheduleProgressPublisher.publishComplete(scheduleId, "0hard/-10soft");

        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);
        verify(messagingTemplate).convertAndSend(org.mockito.ArgumentMatchers.eq("/topic/schedule/" + scheduleId), payloadCaptor.capture());
        assertThat(payloadCaptor.getValue()).extracting("type").isEqualTo("COMPLETE");
        assertThat(payloadCaptor.getValue()).extracting("finalScore").isEqualTo("0hard/-10soft");
    }
}
