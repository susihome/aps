package com.aps.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 作业分配实体 - 纯持久化对象
 * 移除 Timefold 注解，与 AssignmentPlanningModel 分离
 */
@Entity
@Table(name = "assignments")
@Getter
@Setter
public class Assignment extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "operation_id")
    private Operation operation;

    @ManyToOne
    @JoinColumn(name = "assigned_resource_id")
    private Resource assignedResource;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private Boolean pinned = false;

    @Column(name = "schedule_id")
    private UUID scheduleId;
}
