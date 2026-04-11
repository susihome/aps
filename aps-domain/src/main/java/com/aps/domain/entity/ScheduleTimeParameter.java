package com.aps.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "schedule_time_parameters")
@Getter
@Setter
public class ScheduleTimeParameter extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_id", unique = true)
    private Resource resource;

    // 工单筛选范围 — "捞哪些工单"
    @Column(nullable = false)
    private Integer orderFilterStartDays = 0;

    @Column(nullable = false)
    private LocalTime orderFilterStartTime = LocalTime.of(8, 0);

    @Column(nullable = false)
    private Integer orderFilterEndDays = 14;

    @Column(nullable = false)
    private LocalTime orderFilterEndTime = LocalTime.of(0, 0);

    // 排程安排起点 — "从什么时候开始排"
    @Column(nullable = false)
    private Integer planningStartDays = 0;

    @Column(nullable = false)
    private LocalTime planningStartTime = LocalTime.of(9, 0);

    // 显示范围 — "看多远"
    @Column(nullable = false)
    private Integer displayStartDays = 0;

    @Column(nullable = false)
    private Integer displayEndDays = 30;

    // 辅助参数
    @Column(nullable = false)
    private Integer completionDays = 0;

    @Column(nullable = false)
    private Integer timeScale = 1;

    @Column(nullable = false)
    private Integer factor = 0;

    @Column
    private Integer exceedPeriod;

    // 通用字段
    @Column(nullable = false)
    private Boolean isDefault = false;

    @Column(nullable = false)
    private Boolean enabled = true;

    @Column(length = 500)
    private String remark;

    public UUID getResourceId() {
        return resource != null ? resource.getId() : null;
    }
}
