package com.aps.service.mq;

import com.aps.service.config.ScheduleTaskRabbitConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RabbitMqScheduleTaskDispatcher implements ScheduleTaskDispatcher {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void dispatch(UUID taskId, UUID scheduleId) {
        rabbitTemplate.convertAndSend(
                ScheduleTaskRabbitConfig.SCHEDULE_SOLVE_EXCHANGE,
                ScheduleTaskRabbitConfig.SCHEDULE_SOLVE_ROUTING_KEY,
                new ScheduleSolveTaskMessage(taskId, scheduleId)
        );
    }
}
