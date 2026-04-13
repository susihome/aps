package com.aps.service.mq;

import java.util.UUID;

public interface ScheduleNotificationDispatcher {

    void publishStarted(UUID scheduleId);

    void publishProgress(UUID scheduleId, int progress, String message, String score);

    void publishCompleted(UUID scheduleId, String finalScore);

    void publishFailed(UUID scheduleId, String errorMessage);

    void publishStopped(UUID scheduleId);
}
