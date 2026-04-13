package com.aps.service.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Queue;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("排产任务 RabbitMQ 配置测试")
class ScheduleTaskRabbitConfigTest {

    @Test
    @DisplayName("重试队列应使用配置化 TTL")
    void scheduleSolveRetryQueue_shouldUseConfiguredTtl() {
        ScheduleTaskProperties properties = new ScheduleTaskProperties();
        properties.setRetryDelayMs(15_000);
        ScheduleTaskRabbitConfig config = new ScheduleTaskRabbitConfig(properties);

        Queue queue = config.scheduleSolveRetryQueue();

        assertThat(queue.getArguments())
                .containsEntry("x-message-ttl", 15_000L)
                .containsEntry("x-dead-letter-exchange", ScheduleTaskRabbitConfig.SCHEDULE_SOLVE_EXCHANGE)
                .containsEntry("x-dead-letter-routing-key", ScheduleTaskRabbitConfig.SCHEDULE_SOLVE_ROUTING_KEY);
    }
}
