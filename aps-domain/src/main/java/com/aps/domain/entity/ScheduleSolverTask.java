package com.aps.domain.entity;

import com.aps.domain.enums.SolverTaskStatus;
import com.aps.domain.enums.SolverTaskType;
import com.aps.domain.enums.TriggerSource;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "schedule_solver_task")
@Getter
@Setter
public class ScheduleSolverTask extends BaseEntity {

    @Column(nullable = false)
    private UUID scheduleId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private SolverTaskType taskType;

    private UUID triggeredBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private TriggerSource triggerSource;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private SolverTaskStatus status;

    @Column(length = 128)
    private String score;

    private Integer progress;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @Column(length = 64)
    private String lockOwnerToken;

    private LocalDateTime startedAt;

    private LocalDateTime finishedAt;
}
