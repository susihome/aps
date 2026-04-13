package com.aps.api.websocket;

import com.aps.service.mq.ScheduleNotificationMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AnonymousQueue;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ScheduleNotificationConsumer {

    private final ScheduleProgressPublisher scheduleProgressPublisher;

    @RabbitListener(queues = "#{scheduleNotificationQueue.name}")
    public void consume(ScheduleNotificationMessage message) {
        log.info("收到排产通知: type={}, scheduleId={}", message.type(), message.scheduleId());
        switch (message.type()) {
            case "STARTED" -> scheduleProgressPublisher.publishProgress(message.scheduleId(),
                    message.progress() == null ? 0 : message.progress(),
                    message.message(),
                    message.score());
            case "PROGRESS" -> scheduleProgressPublisher.publishProgress(message.scheduleId(),
                    message.progress() == null ? 0 : message.progress(),
                    message.message(),
                    message.score());
            case "COMPLETED" -> scheduleProgressPublisher.publishComplete(message.scheduleId(), message.score());
            case "FAILED" -> scheduleProgressPublisher.publishConflict(message.scheduleId(), "ERROR", message.message());
            case "STOPPED" -> scheduleProgressPublisher.publishConflict(message.scheduleId(), "STOPPED", message.message());
            default -> log.warn("忽略未知排产通知类型: {}", message.type());
        }
    }

    @Configuration
    static class ScheduleNotificationQueueConfig {

        @Bean
        public Queue scheduleNotificationQueue() {
            return new AnonymousQueue();
        }

        @Bean
        public Binding scheduleNotificationBinding(Queue scheduleNotificationQueue,
                                                   FanoutExchange scheduleNotificationExchange) {
            return BindingBuilder.bind(scheduleNotificationQueue).to(scheduleNotificationExchange);
        }
    }
}
