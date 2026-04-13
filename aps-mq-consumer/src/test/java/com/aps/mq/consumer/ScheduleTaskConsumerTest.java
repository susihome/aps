package com.aps.mq.consumer;

import com.aps.service.ScheduleService;
import com.aps.service.MqConsumeRecordService;
import com.aps.service.config.ScheduleTaskProperties;
import com.aps.service.config.ScheduleTaskRabbitConfig;
import com.aps.service.exception.BusinessException;
import com.aps.service.mq.ScheduleSolveTaskMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("排产任务消费者测试")
class ScheduleTaskConsumerTest {

    @Mock
    private ScheduleService scheduleService;

    @Mock
    private MqConsumeRecordService mqConsumeRecordService;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private ScheduleTaskProperties scheduleTaskProperties;

    @InjectMocks
    private ScheduleTaskConsumer scheduleTaskConsumer;

    @Test
    @DisplayName("消费排产任务时应调用执行服务")
    void consumeSolveTask_shouldInvokeScheduleService() {
        UUID taskId = UUID.randomUUID();
        UUID scheduleId = UUID.randomUUID();
        when(mqConsumeRecordService.tryStartConsume(taskId.toString(), "schedule-task-consumer", scheduleId.toString()))
                .thenReturn(true);

        scheduleTaskConsumer.consumeSolveTask(new ScheduleSolveTaskMessage(taskId, scheduleId), new Message(new byte[0], new MessageProperties()));

        verify(scheduleService).executeSolveTask(taskId);
        verify(mqConsumeRecordService).markConsumed(taskId.toString(), "schedule-task-consumer", scheduleId.toString());
    }

    @Test
    @DisplayName("重复消费排产任务时应忽略")
    void consumeSolveTask_whenDuplicated_shouldSkip() {
        UUID taskId = UUID.randomUUID();
        UUID scheduleId = UUID.randomUUID();
        when(mqConsumeRecordService.tryStartConsume(taskId.toString(), "schedule-task-consumer", scheduleId.toString()))
                .thenReturn(false);

        scheduleTaskConsumer.consumeSolveTask(new ScheduleSolveTaskMessage(taskId, scheduleId), new Message(new byte[0], new MessageProperties()));

        verifyNoInteractions(scheduleService);
    }

    @Test
    @DisplayName("普通异常且未超过重试次数时应进入重试队列")
    void consumeSolveTask_whenRetryableFailure_shouldRejectForRetry() {
        UUID taskId = UUID.randomUUID();
        UUID scheduleId = UUID.randomUUID();
        when(scheduleTaskProperties.getMaxRetryCount()).thenReturn(3L);
        when(mqConsumeRecordService.tryStartConsume(taskId.toString(), "schedule-task-consumer", scheduleId.toString()))
                .thenReturn(true);
        org.mockito.Mockito.doThrow(new IllegalStateException("temporary failure"))
                .when(scheduleService).executeSolveTask(taskId);

        Assertions.assertThrows(AmqpRejectAndDontRequeueException.class, () ->
                scheduleTaskConsumer.consumeSolveTask(new ScheduleSolveTaskMessage(taskId, scheduleId), new Message(new byte[0], new MessageProperties())));
    }

    @Test
    @DisplayName("业务异常时应直接进入死信队列")
    void consumeSolveTask_whenBusinessFailure_shouldRouteToDlq() {
        UUID taskId = UUID.randomUUID();
        UUID scheduleId = UUID.randomUUID();
        when(mqConsumeRecordService.tryStartConsume(taskId.toString(), "schedule-task-consumer", scheduleId.toString()))
                .thenReturn(true);
        org.mockito.Mockito.doThrow(new BusinessException("invalid task"))
                .when(scheduleService).executeSolveTask(taskId);

        scheduleTaskConsumer.consumeSolveTask(new ScheduleSolveTaskMessage(taskId, scheduleId), new Message(new byte[0], new MessageProperties()));

        verify(rabbitTemplate).convertAndSend(
                ScheduleTaskRabbitConfig.SCHEDULE_SOLVE_EXCHANGE,
                ScheduleTaskRabbitConfig.SCHEDULE_SOLVE_DLQ_ROUTING_KEY,
                new ScheduleSolveTaskMessage(taskId, scheduleId));
    }

    @Test
    @DisplayName("超过最大重试次数时应直接进入死信队列")
    void consumeSolveTask_whenRetryExceeded_shouldRouteToDlq() {
        UUID taskId = UUID.randomUUID();
        UUID scheduleId = UUID.randomUUID();
        when(scheduleTaskProperties.getMaxRetryCount()).thenReturn(3L);
        when(mqConsumeRecordService.tryStartConsume(taskId.toString(), "schedule-task-consumer", scheduleId.toString()))
                .thenReturn(true);
        org.mockito.Mockito.doThrow(new IllegalStateException("temporary failure"))
                .when(scheduleService).executeSolveTask(taskId);
        MessageProperties properties = new MessageProperties();
        properties.getHeaders().put("x-death", List.of(Map.of("count", 3L)));

        scheduleTaskConsumer.consumeSolveTask(new ScheduleSolveTaskMessage(taskId, scheduleId), new Message(new byte[0], properties));

        verify(rabbitTemplate).convertAndSend(
                ScheduleTaskRabbitConfig.SCHEDULE_SOLVE_EXCHANGE,
                ScheduleTaskRabbitConfig.SCHEDULE_SOLVE_DLQ_ROUTING_KEY,
                new ScheduleSolveTaskMessage(taskId, scheduleId));
    }
}
