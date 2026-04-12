package com.aps.solver;

import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintCollectors;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import ai.timefold.solver.core.api.score.stream.Joiners;
import com.aps.domain.entity.Mold;
import com.aps.solver.model.AssignmentPlanningModel;

public class ApsConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory factory) {
        return new Constraint[]{
                resourceConflict(factory),
                operationSequence(factory),
                resourceEligibility(factory),
                minimizeDelay(factory),
                maximizeResourceUtilization(factory),
                preferPreferredMold(factory),
                minimizeChangeover(factory)
        };
    }

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

    private Constraint operationSequence(ConstraintFactory factory) {
        return factory.forEach(AssignmentPlanningModel.class)
                .filter(a -> a.getOperation() != null
                        && a.getOperation().getOrderId() != null
                        && a.getOperation().getSequence() != null
                        && a.getOperation().getSequence() > 1)
                .join(AssignmentPlanningModel.class,
                        Joiners.equal(a -> a.getOperation() == null ? null : a.getOperation().getOrderId(),
                                a -> a.getOperation() == null ? null : a.getOperation().getOrderId()),
                        Joiners.filtering((current, previous) -> previous.getOperation() != null
                                && previous.getOperation().getSequence() != null
                                && current.getOperation().getSequence() == previous.getOperation().getSequence() + 1))
                .filter((current, previous) -> current.getStartTime() != null
                        && previous.getEndTime() != null
                        && current.getStartTime().isBefore(previous.getEndTime()))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Operation sequence");
    }

    public Constraint resourceEligibility(ConstraintFactory factory) {
        return factory.forEach(AssignmentPlanningModel.class)
                .filter(a -> a.getAssignedResource() != null
                        && a.getEligibleResources() != null
                        && !a.getEligibleResources().isEmpty()
                        && !a.getEligibleResources().contains(a.getAssignedResource()))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Resource eligibility");
    }

    private Constraint minimizeDelay(ConstraintFactory factory) {
        return factory.forEach(AssignmentPlanningModel.class)
                .filter(a -> a.getEndTime() != null
                        && a.getOperation() != null
                        && a.getOperation().getDueDate() != null
                        && a.getOperation().getPriority() != null
                        && a.getEndTime().isAfter(a.getOperation().getDueDate()))
                .penalizeLong(HardSoftScore.ONE_SOFT,
                        a -> java.time.Duration.between(
                                a.getOperation().getDueDate(),
                                a.getEndTime()
                        ).toMinutes() * a.getOperation().getPriority().getLevel())
                .asConstraint("Minimize delay");
    }

    private Constraint maximizeResourceUtilization(ConstraintFactory factory) {
        return factory.forEach(AssignmentPlanningModel.class)
                .filter(a -> a.getAssignedResource() != null)
                .groupBy(AssignmentPlanningModel::getAssignedResource, ConstraintCollectors.count())
                .rewardLong(HardSoftScore.ONE_SOFT, (resource, count) -> count)
                .asConstraint("Maximize resource utilization");
    }

    public Constraint preferPreferredMold(ConstraintFactory factory) {
        return factory.forEach(AssignmentPlanningModel.class)
                .filter(a -> a.getOperation() != null
                        && a.getOperation().getRequiredMaterialId() != null
                        && a.getPreferredMold() != null
                        && a.getOperation().getLockedMoldId() != null
                        && !a.getPreferredMold().getId().equals(a.getOperation().getLockedMoldId()))
                .penalize(HardSoftScore.ONE_SOFT,
                        a -> a.getPreferredMoldPriority() == null ? 1 : Math.max(1, a.getPreferredMoldPriority()))
                .asConstraint("Prefer preferred mold");
    }

    public Constraint minimizeChangeover(ConstraintFactory factory) {
        return factory.forEach(AssignmentPlanningModel.class)
                .filter(a -> a.getAssignedResource() != null
                        && a.getStartTime() != null
                        && a.getEndTime() != null
                        && a.getPreferredMold() != null)
                .join(AssignmentPlanningModel.class,
                        Joiners.equal(AssignmentPlanningModel::getAssignedResource),
                        Joiners.filtering((a1, a2) -> a1.getStartTime() != null
                                && a2.getEndTime() != null
                                && a2.getEndTime().equals(a1.getStartTime())))
                .filter((current, previous) -> previous.getPreferredMold() != null
                        && current.getPreferredMold() != null
                        && isMoldChanged(previous.getPreferredMold(), current.getPreferredMold()))
                .penalize(HardSoftScore.ONE_SOFT,
                        (current, previous) -> {
                            Integer penalty = current.getPreferredChangeoverTimeMinutes();
                            return penalty == null || penalty <= 0 ? 1 : penalty;
                        })
                .asConstraint("Minimize mold changeover");
    }

    private boolean isMoldChanged(Mold previousMold, Mold currentMold) {
        if (previousMold == null || currentMold == null || previousMold.getId() == null || currentMold.getId() == null) {
            return false;
        }
        return !previousMold.getId().equals(currentMold.getId());
    }
}
