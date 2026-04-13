package com.aps.service.mq;

import java.util.UUID;

public record ScheduleSolveTaskMessage(UUID taskId, UUID scheduleId) {
}
