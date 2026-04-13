package com.aps.service.config;

import org.springframework.amqp.core.FanoutExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ScheduleNotificationRabbitConfig {

    public static final String SCHEDULE_NOTIFICATION_EXCHANGE = "schedule.notification.exchange";

    @Bean
    public FanoutExchange scheduleNotificationExchange() {
        return new FanoutExchange(SCHEDULE_NOTIFICATION_EXCHANGE, true, false);
    }
}
