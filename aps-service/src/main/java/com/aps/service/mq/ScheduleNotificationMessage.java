package com.aps.service.mq;

import java.time.LocalDateTime;
import java.util.UUID;

public record ScheduleNotificationMessage(
        String type,
        UUID scheduleId,
        Integer progress,
        String score,
        String message,
        LocalDateTime timestamp
) {
}
