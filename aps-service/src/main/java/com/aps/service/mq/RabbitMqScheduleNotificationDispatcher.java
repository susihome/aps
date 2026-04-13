package com.aps.service.mq;

import com.aps.service.config.ScheduleNotificationRabbitConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RabbitMqScheduleNotificationDispatcher implements ScheduleNotificationDispatcher {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publishStarted(UUID scheduleId) {
        send(new ScheduleNotificationMessage("STARTED", scheduleId, 0, null, "开始求解", LocalDateTime.now()));
    }

    @Override
    public void publishProgress(UUID scheduleId, int progress, String message, String score) {
        send(new ScheduleNotificationMessage("PROGRESS", scheduleId, progress, score, message, LocalDateTime.now()));
    }

    @Override
    public void publishCompleted(UUID scheduleId, String finalScore) {
        send(new ScheduleNotificationMessage("COMPLETED", scheduleId, 100, finalScore, null, LocalDateTime.now()));
    }

    @Override
    public void publishFailed(UUID scheduleId, String errorMessage) {
        send(new ScheduleNotificationMessage("FAILED", scheduleId, null, null, errorMessage, LocalDateTime.now()));
    }

    @Override
    public void publishStopped(UUID scheduleId) {
        send(new ScheduleNotificationMessage("STOPPED", scheduleId, null, null, "求解已手动停止", LocalDateTime.now()));
    }

    private void send(ScheduleNotificationMessage message) {
        rabbitTemplate.convertAndSend(ScheduleNotificationRabbitConfig.SCHEDULE_NOTIFICATION_EXCHANGE, "", message);
    }
}
