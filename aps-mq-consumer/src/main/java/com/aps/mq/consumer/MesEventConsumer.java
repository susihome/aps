package com.aps.mq.consumer;

import com.aps.mq.config.RabbitMQConfig;
import com.aps.mq.event.MesEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MesEventConsumer {

    /**
     * 处理工序报工事件
     * 当工序完成报工时，检查是否需要重排产
     */
    @RabbitListener(queues = RabbitMQConfig.WORKORDER_REPORT_QUEUE)
    public void handleWorkorderReport(MesEvent event) {
        log.info("收到工序报工事件: orderId={}, operationId={}, completedTime={}",
            event.getOrderId(), event.getOperationId(), event.getCompletedTime());

        try {
            // 检查实际完工时间
            if (event.getActualDuration() != null && event.getActualDuration() > 480) {
                // 如果实际工时超过 8 小时，可能需要重排产
                log.warn("工序实际工时 {} 分钟，超过预期，可能需要重排产", event.getActualDuration());
                triggerReschedule(event.getOrderId(), "工序报工延期");
            }

            // 检查质量状态
            if ("REJECTED".equals(event.getQualityStatus())) {
                log.warn("工序质量不合格，可能需要重排产");
                triggerReschedule(event.getOrderId(), "质量不合格");
            }
        } catch (Exception e) {
            log.error("处理工序报工事件失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 处理设备故障事件
     * 当设备故障预计停机时间超过 2 小时时，触发重排产
     */
    @RabbitListener(queues = RabbitMQConfig.EQUIPMENT_FAULT_QUEUE)
    public void handleEquipmentFault(MesEvent event) {
        log.info("收到设备故障事件: equipmentId={}, estimatedDowntime={} 分钟, reason={}",
            event.getEquipmentId(), event.getEstimatedDowntime(), event.getFaultReason());

        try {
            if (event.getEstimatedDowntime() != null && event.getEstimatedDowntime() > 120) {
                log.warn("设备 {} 预计停机 {} 分钟（> 2小时），触发重排产",
                    event.getEquipmentId(), event.getEstimatedDowntime());
                triggerReschedule(event.getEquipmentId(), "设备故障: " + event.getFaultReason());
            }
        } catch (Exception e) {
            log.error("处理设备故障事件失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 处理物料短缺事件
     * 当物料短缺时，触发重排产
     */
    @RabbitListener(queues = RabbitMQConfig.MATERIAL_SHORTAGE_QUEUE)
    public void handleMaterialShortage(MesEvent event) {
        log.info("收到物料短缺事件: eventType={}, orderId={}",
            event.getEventType(), event.getOrderId());

        try {
            log.warn("物料短缺，触发重排产: orderId={}", event.getOrderId());
            triggerReschedule(event.getOrderId(), "物料短缺");
        } catch (Exception e) {
            log.error("处理物料短缺事件失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 触发重排产
     * 注意: 实际实现需要注入 ScheduleService，这里仅记录日志
     * 在生产环境中应该调用 scheduleService.solveAsync(scheduleId)
     */
    private void triggerReschedule(String entityId, String reason) {
        log.info("触发重排产: entityId={}, reason={}", entityId, reason);

        // TODO: 注入 ScheduleService 并调用 solveAsync
        // scheduleService.solveAsync(UUID.fromString(scheduleId));

        // 当前实现仅记录日志，避免循环依赖
        // aps-mq-consumer 模块不应该依赖 aps-service 模块
        // 应该通过事件发布或其他机制触发重排产
    }
}
