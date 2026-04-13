package com.aps.service.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class ScheduleTaskRabbitConfig {

    public static final String SCHEDULE_SOLVE_EXCHANGE = "schedule.task.exchange";
    public static final String SCHEDULE_SOLVE_QUEUE = "schedule.task.plan";
    public static final String SCHEDULE_SOLVE_ROUTING_KEY = "schedule.task.plan";
    public static final String SCHEDULE_SOLVE_RETRY_QUEUE = "schedule.task.plan.retry";
    public static final String SCHEDULE_SOLVE_RETRY_ROUTING_KEY = "schedule.task.plan.retry";
    public static final String SCHEDULE_SOLVE_DLQ = "schedule.task.plan.dlq";
    public static final String SCHEDULE_SOLVE_DLQ_ROUTING_KEY = "schedule.task.plan.dlq";

    private final ScheduleTaskProperties scheduleTaskProperties;

    @Bean
    public DirectExchange scheduleSolveExchange() {
        return new DirectExchange(SCHEDULE_SOLVE_EXCHANGE, true, false);
    }

    @Bean
    public Queue scheduleSolveQueue() {
        return QueueBuilder.durable(SCHEDULE_SOLVE_QUEUE)
                .withArgument("x-dead-letter-exchange", SCHEDULE_SOLVE_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", SCHEDULE_SOLVE_RETRY_ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue scheduleSolveRetryQueue() {
        return QueueBuilder.durable(SCHEDULE_SOLVE_RETRY_QUEUE)
                .withArgument("x-message-ttl", scheduleTaskProperties.getRetryDelayMs())
                .withArgument("x-dead-letter-exchange", SCHEDULE_SOLVE_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", SCHEDULE_SOLVE_ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue scheduleSolveDlq() {
        return QueueBuilder.durable(SCHEDULE_SOLVE_DLQ).build();
    }

    @Bean
    public Binding scheduleSolveBinding(Queue scheduleSolveQueue, DirectExchange scheduleSolveExchange) {
        return BindingBuilder.bind(scheduleSolveQueue).to(scheduleSolveExchange).with(SCHEDULE_SOLVE_ROUTING_KEY);
    }

    @Bean
    public Binding scheduleSolveRetryBinding(Queue scheduleSolveRetryQueue, DirectExchange scheduleSolveExchange) {
        return BindingBuilder.bind(scheduleSolveRetryQueue).to(scheduleSolveExchange).with(SCHEDULE_SOLVE_RETRY_ROUTING_KEY);
    }

    @Bean
    public Binding scheduleSolveDlqBinding(Queue scheduleSolveDlq, DirectExchange scheduleSolveExchange) {
        return BindingBuilder.bind(scheduleSolveDlq).to(scheduleSolveExchange).with(SCHEDULE_SOLVE_DLQ_ROUTING_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter scheduleTaskMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         Jackson2JsonMessageConverter scheduleTaskMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(scheduleTaskMessageConverter);
        return template;
    }
}
