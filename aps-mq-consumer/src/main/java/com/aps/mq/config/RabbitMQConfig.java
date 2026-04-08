package com.aps.mq.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String WORKORDER_REPORT_QUEUE = "mes.workorder.report";
    public static final String EQUIPMENT_FAULT_QUEUE = "mes.equipment.fault";
    public static final String MATERIAL_SHORTAGE_QUEUE = "mes.material.shortage";

    @Bean
    public Queue workorderReportQueue() {
        return new Queue(WORKORDER_REPORT_QUEUE, true);
    }

    @Bean
    public Queue equipmentFaultQueue() {
        return new Queue(EQUIPMENT_FAULT_QUEUE, true);
    }

    @Bean
    public Queue materialShortageQueue() {
        return new Queue(MATERIAL_SHORTAGE_QUEUE, true);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}
