package com.aps.mq.consumer;

import com.aps.service.MqConsumeRecordService;
import com.aps.service.ScheduleService;
import com.aps.service.config.ScheduleTaskProperties;
import com.aps.service.config.ScheduleTaskRabbitConfig;
import com.aps.service.exception.BusinessException;
import com.aps.service.mq.ScheduleSolveTaskMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ScheduleTaskConsumer {

    private static final String CONSUMER_NAME = "schedule-task-consumer";

    private final ScheduleService scheduleService;
    private final MqConsumeRecordService mqConsumeRecordService;
    private final RabbitTemplate rabbitTemplate;
    private final ScheduleTaskProperties scheduleTaskProperties;

    @RabbitListener(queues = ScheduleTaskRabbitConfig.SCHEDULE_SOLVE_QUEUE)
    public void consumeSolveTask(ScheduleSolveTaskMessage message, Message amqpMessage) {
        log.info("收到排产任务: taskId={}, scheduleId={}", message.taskId(), message.scheduleId());
        if (!mqConsumeRecordService.tryStartConsume(message.taskId().toString(), CONSUMER_NAME, message.scheduleId().toString())) {
            log.info("忽略重复排产任务: taskId={}", message.taskId());
            return;
        }
        try {
            scheduleService.executeSolveTask(message.taskId());
            mqConsumeRecordService.markConsumed(message.taskId().toString(), CONSUMER_NAME, message.scheduleId().toString());
        } catch (BusinessException exception) {
            log.error("排产任务属于不可重试异常，转入死信队列: taskId={}", message.taskId(), exception);
            sendToDlq(message);
        } catch (Exception exception) {
            long retryCount = getRetryCount(amqpMessage);
            if (retryCount >= scheduleTaskProperties.getMaxRetryCount()) {
                log.error("排产任务超过最大重试次数，转入死信队列: taskId={}, retryCount={}", message.taskId(), retryCount, exception);
                sendToDlq(message);
                return;
            }
            log.warn("排产任务执行失败，准备进入重试队列: taskId={}, retryCount={}", message.taskId(), retryCount, exception);
            throw new AmqpRejectAndDontRequeueException("排产任务执行失败，进入重试队列", exception);
        }
    }

    private long getRetryCount(Message amqpMessage) {
        Object header = amqpMessage.getMessageProperties().getHeaders().get("x-death");
        if (!(header instanceof List<?> deaths) || deaths.isEmpty()) {
            return 0L;
        }
        Object firstDeath = deaths.getFirst();
        if (!(firstDeath instanceof Map<?, ?> deathInfo)) {
            return 0L;
        }
        Object count = deathInfo.get("count");
        if (count instanceof Number number) {
            return number.longValue();
        }
        return 0L;
    }

    private void sendToDlq(ScheduleSolveTaskMessage message) {
        rabbitTemplate.convertAndSend(
                ScheduleTaskRabbitConfig.SCHEDULE_SOLVE_EXCHANGE,
                ScheduleTaskRabbitConfig.SCHEDULE_SOLVE_DLQ_ROUTING_KEY,
                message);
    }
}
