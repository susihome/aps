package com.aps.solver;

import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import ai.timefold.solver.core.api.score.stream.Joiners;
import ai.timefold.solver.core.api.score.stream.ConstraintCollectors;
import com.aps.solver.model.AssignmentPlanningModel;

public class ApsConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory factory) {
        return new Constraint[]{
                resourceConflict(factory),
                operationSequence(factory),
                minimizeDelay(factory),
                maximizeResourceUtilization(factory)
        };
    }

    // 硬约束：资源时间冲突（添加空值保护）
    private Constraint resourceConflict(ConstraintFactory factory) {
        return factory.forEach(AssignmentPlanningModel.class)
                .filter(a -> a.getAssignedResource() != null
                        && a.getStartTime() != null
                        && a.getEndTime() != null)
                .join(AssignmentPlanningModel.class,
                        Joiners.equal(AssignmentPlanningModel::getAssignedResource),
                        Joiners.lessThan(AssignmentPlanningModel::getAssignmentId),
                        Joiners.overlapping(
                                AssignmentPlanningModel::getStartTime,
                                AssignmentPlanningModel::getEndTime,
                                AssignmentPlanningModel::getStartTime,
                                AssignmentPlanningModel::getEndTime
                        ))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Resource conflict");
    }

    // 硬约束：工序顺序（完善空值保护）
    private Constraint operationSequence(ConstraintFactory factory) {
        return factory.forEach(AssignmentPlanningModel.class)
                .filter(a -> a.getOperation() != null
                        && a.getOperation().getOrder() != null
                        && a.getOperation().getSequence() != null
                        && a.getOperation().getSequence() > 1)
                .join(AssignmentPlanningModel.class,
                        Joiners.equal(
                                a -> a.getOperation() != null && a.getOperation().getOrder() != null
                                        ? a.getOperation().getOrder().getId() : null,
                                a -> a.getOperation() != null && a.getOperation().getOrder() != null
                                        ? a.getOperation().getOrder().getId() : null
                        ),
                        Joiners.filtering((current, previous) ->
                                previous.getOperation() != null
                                && previous.getOperation().getOrder() != null
                                && previous.getOperation().getSequence() != null
                                && current.getOperation().getSequence() == previous.getOperation().getSequence() + 1))
                .filter((current, previous) ->
                        current.getStartTime() != null && previous.getEndTime() != null &&
                        current.getStartTime().isBefore(previous.getEndTime()))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Operation sequence");
    }

    // 软约束：最小化延期
    private Constraint minimizeDelay(ConstraintFactory factory) {
        return factory.forEach(AssignmentPlanningModel.class)
                .filter(a -> a.getEndTime() != null
                        && a.getOperation() != null
                        && a.getOperation().getOrder() != null
                        && a.getOperation().getOrder().getDueDate() != null
                        && a.getOperation().getOrder().getPriority() != null
                        && a.getEndTime().isAfter(a.getOperation().getOrder().getDueDate()))
                .penalizeLong(HardSoftScore.ONE_SOFT,
                        a -> java.time.Duration.between(
                                a.getOperation().getOrder().getDueDate(),
                                a.getEndTime()
                        ).toMinutes() * a.getOperation().getOrder().getPriority().getLevel())
                .asConstraint("Minimize delay");
    }

    // 软约束：最大化资源利用率（添加空值保护）
    private Constraint maximizeResourceUtilization(ConstraintFactory factory) {
        return factory.forEach(AssignmentPlanningModel.class)
                .filter(a -> a.getAssignedResource() != null)
                .groupBy(AssignmentPlanningModel::getAssignedResource, ConstraintCollectors.count())
                .rewardLong(HardSoftScore.ONE_SOFT, (resource, count) -> count)
                .asConstraint("Maximize resource utilization");
    }
}
