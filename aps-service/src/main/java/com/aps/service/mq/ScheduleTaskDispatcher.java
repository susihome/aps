package com.aps.service.mq;

import java.util.UUID;

public interface ScheduleTaskDispatcher {

    void dispatch(UUID taskId, UUID scheduleId);
}
