package com.aps.api.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ScheduleProgressPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public void publishProgress(UUID scheduleId, int progress, String messageText, String score) {
        ProgressMessage message = new ProgressMessage(
                "PROGRESS",
                scheduleId.toString(),
                progress,
                messageText,
                score,
                LocalDateTime.now()
        );
        messagingTemplate.convertAndSend("/topic/schedule/" + scheduleId, message);
    }

    public void publishConflict(UUID scheduleId, String conflictType, String message) {
        ConflictMessage conflict = new ConflictMessage(
                "CONFLICT",
                scheduleId.toString(),
                conflictType,
                message,
                LocalDateTime.now()
        );
        messagingTemplate.convertAndSend("/topic/schedule/" + scheduleId, conflict);
    }

    public void publishComplete(UUID scheduleId, String finalScore) {
        CompleteMessage message = new CompleteMessage(
                "COMPLETE",
                scheduleId.toString(),
                finalScore,
                LocalDateTime.now()
        );
        messagingTemplate.convertAndSend("/topic/schedule/" + scheduleId, message);
    }

    record ProgressMessage(
            String type,
            String scheduleId,
            int progress,
            String message,
            String currentScore,
            LocalDateTime timestamp
    ) {}

    record ConflictMessage(
            String type,
            String scheduleId,
            String conflictType,
            String message,
            LocalDateTime timestamp
    ) {}

    record CompleteMessage(
            String type,
            String scheduleId,
            String finalScore,
            LocalDateTime timestamp
    ) {}
}
