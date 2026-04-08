package com.aps.solver.model;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.entity.PlanningPin;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;
import ai.timefold.solver.core.api.domain.variable.ShadowVariable;
import ai.timefold.solver.core.api.domain.variable.VariableListener;
import ai.timefold.solver.core.api.score.director.ScoreDirector;
import com.aps.domain.entity.Operation;
import com.aps.domain.entity.Resource;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Timefold 规划实体 - 纯内存对象，不持久化
 * 与 Assignment Entity 分离，避免 JPA 和 Solver 上下文混淆
 */
@PlanningEntity(difficultyComparatorClass = AssignmentDifficultyComparator.class)
@Getter
@Setter
@NoArgsConstructor
public class AssignmentPlanningModel implements Comparable<AssignmentPlanningModel> {

    private UUID assignmentId;

    // Problem facts（不会被 Solver 修改）
    private Operation operation;

    // Planning variables（会被 Solver 修改）
    @PlanningVariable(valueRangeProviderRefs = "resourceRange")
    private Resource assignedResource;

    @PlanningVariable(valueRangeProviderRefs = "timeRange")
    private LocalDateTime startTime;

    // Shadow variable: 根据 startTime + duration 自动计算
    @ShadowVariable(
            variableListenerClass = EndTimeUpdatingVariableListener.class,
            sourceVariableName = "startTime"
    )
    private LocalDateTime endTime;

    @PlanningPin
    private Boolean pinned = false;

    public AssignmentPlanningModel(UUID assignmentId, Operation operation) {
        this.assignmentId = assignmentId;
        this.operation = operation;
        this.pinned = false;
    }

    @Override
    public int compareTo(AssignmentPlanningModel other) {
        if (this.assignmentId == null || other.assignmentId == null) {
            return 0;
        }
        return this.assignmentId.compareTo(other.assignmentId);
    }

    /**
     * Variable Listener: 当 startTime 变化时自动更新 endTime
     */
    public static class EndTimeUpdatingVariableListener implements VariableListener<SchedulePlanningModel, AssignmentPlanningModel> {

        @Override
        public void beforeEntityAdded(ScoreDirector<SchedulePlanningModel> scoreDirector, AssignmentPlanningModel assignment) {
            // Do nothing
        }

        @Override
        public void afterEntityAdded(ScoreDirector<SchedulePlanningModel> scoreDirector, AssignmentPlanningModel assignment) {
            updateEndTime(scoreDirector, assignment);
        }

        @Override
        public void beforeVariableChanged(ScoreDirector<SchedulePlanningModel> scoreDirector, AssignmentPlanningModel assignment) {
            // Do nothing
        }

        @Override
        public void afterVariableChanged(ScoreDirector<SchedulePlanningModel> scoreDirector, AssignmentPlanningModel assignment) {
            updateEndTime(scoreDirector, assignment);
        }

        @Override
        public void beforeEntityRemoved(ScoreDirector<SchedulePlanningModel> scoreDirector, AssignmentPlanningModel assignment) {
            // Do nothing
        }

        @Override
        public void afterEntityRemoved(ScoreDirector<SchedulePlanningModel> scoreDirector, AssignmentPlanningModel assignment) {
            // Do nothing
        }

        private void updateEndTime(ScoreDirector<SchedulePlanningModel> scoreDirector, AssignmentPlanningModel assignment) {
            LocalDateTime startTime = assignment.getStartTime();
            Operation operation = assignment.getOperation();

            LocalDateTime endTime;
            if (startTime == null || operation == null || operation.getStandardDuration() == null) {
                endTime = null;
            } else {
                endTime = startTime.plusMinutes(operation.getStandardDuration());
            }

            scoreDirector.beforeVariableChanged(assignment, "endTime");
            assignment.setEndTime(endTime);
            scoreDirector.afterVariableChanged(assignment, "endTime");
        }
    }
}
