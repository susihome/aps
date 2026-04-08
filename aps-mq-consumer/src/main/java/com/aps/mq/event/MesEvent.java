package com.aps.mq.event;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MesEvent {
    private String eventType;
    private String orderId;
    private String operationId;
    private String equipmentId;
    private LocalDateTime completedTime;
    private Integer actualDuration;
    private String qualityStatus;
    private String faultReason;
    private Integer estimatedDowntime;
}
