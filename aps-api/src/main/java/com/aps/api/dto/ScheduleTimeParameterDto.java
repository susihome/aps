package com.aps.api.dto;

import com.aps.domain.entity.ScheduleTimeParameter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.Hibernate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleTimeParameterDto {

    private UUID id;

    private UUID resourceId;
    private String resourceCode;
    private String resourceName;

    // 工单筛选范围
    private Integer orderFilterStartDays;
    private LocalTime orderFilterStartTime;
    private Integer orderFilterEndDays;
    private LocalTime orderFilterEndTime;

    // 排程安排起点
    private Integer planningStartDays;
    private LocalTime planningStartTime;

    // 显示范围
    private Integer displayStartDays;
    private Integer displayEndDays;

    // 辅助参数
    private Integer completionDays;
    private Integer timeScale;
    private Integer factor;
    private Integer exceedPeriod;

    // 通用字段
    private Boolean isDefault;
    private Boolean enabled;
    private String remark;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // 预览计算结果（仅 preview 接口填充）
    private LocalDateTime calculatedOrderFilterStart;
    private LocalDateTime calculatedOrderFilterEnd;
    private LocalDateTime calculatedPlanningStart;
    private LocalDate calculatedDisplayStart;
    private LocalDate calculatedDisplayEnd;

    public static ScheduleTimeParameterDto fromEntity(ScheduleTimeParameter p) {
        boolean resourceInitialized = Hibernate.isInitialized(p.getResource()) && p.getResource() != null;
        return ScheduleTimeParameterDto.builder()
                .id(p.getId())
                .resourceId(resourceInitialized ? p.getResource().getId() : null)
                .resourceCode(resourceInitialized ? p.getResource().getResourceCode() : null)
                .resourceName(resourceInitialized ? p.getResource().getResourceName() : null)
                .orderFilterStartDays(p.getOrderFilterStartDays())
                .orderFilterStartTime(p.getOrderFilterStartTime())
                .orderFilterEndDays(p.getOrderFilterEndDays())
                .orderFilterEndTime(p.getOrderFilterEndTime())
                .planningStartDays(p.getPlanningStartDays())
                .planningStartTime(p.getPlanningStartTime())
                .displayStartDays(p.getDisplayStartDays())
                .displayEndDays(p.getDisplayEndDays())
                .completionDays(p.getCompletionDays())
                .timeScale(p.getTimeScale())
                .factor(p.getFactor())
                .exceedPeriod(p.getExceedPeriod())
                .isDefault(p.getIsDefault())
                .enabled(p.getEnabled())
                .remark(p.getRemark())
                .createTime(p.getCreateTime())
                .updateTime(p.getUpdateTime())
                .build();
    }
}
