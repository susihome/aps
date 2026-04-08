package com.aps.solver.model;

import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import com.aps.domain.entity.Resource;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Timefold 求解模型 - 纯内存对象，不持久化
 */
@PlanningSolution
@Getter
@Setter
@NoArgsConstructor
public class SchedulePlanningModel {

    private UUID scheduleId;
    private LocalDateTime scheduleStartTime;
    private LocalDateTime scheduleEndTime;

    @PlanningScore
    private HardSoftScore score;

    @PlanningEntityCollectionProperty
    private List<AssignmentPlanningModel> assignments = new ArrayList<>();

    @ProblemFactCollectionProperty
    @ValueRangeProvider(id = "resourceRange")
    private List<Resource> resources = new ArrayList<>();

    @ValueRangeProvider(id = "timeRange")
    public List<LocalDateTime> getTimeRange() {
        if (scheduleStartTime == null || scheduleEndTime == null) {
            return new ArrayList<>();
        }
        List<LocalDateTime> timeSlots = new ArrayList<>();
        LocalDateTime current = scheduleStartTime;
        while (!current.isAfter(scheduleEndTime)) {
            timeSlots.add(current);
            current = current.plusMinutes(30);
        }
        return timeSlots;
    }

    public SchedulePlanningModel(UUID scheduleId, LocalDateTime startTime, LocalDateTime endTime) {
        this.scheduleId = scheduleId;
        this.scheduleStartTime = startTime;
        this.scheduleEndTime = endTime;
    }
}
