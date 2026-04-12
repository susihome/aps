package com.aps.solver.converter;

import com.aps.domain.entity.Assignment;
import com.aps.domain.entity.Material;
import com.aps.domain.entity.MaterialMoldBinding;
import com.aps.domain.entity.Mold;
import com.aps.domain.entity.Resource;
import com.aps.domain.entity.Schedule;
import com.aps.solver.model.AssignmentPlanningModel;
import com.aps.solver.model.OperationPlanningFact;
import com.aps.solver.model.SchedulePlanningModel;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Schedule Entity 与 SchedulePlanningModel 转换器
 * 深拷贝策略：确保 Solver 修改不影响原始 JPA 实体
 */
@Component
public class ScheduleModelConverter {

    public SchedulePlanningModel toPlanningModel(Schedule schedule) {
        SchedulePlanningModel model = new SchedulePlanningModel(
            schedule.getId(),
            schedule.getScheduleStartTime(),
            schedule.getScheduleEndTime()
        );

        List<AssignmentPlanningModel> planningAssignments = new ArrayList<>();
        for (Assignment assignment : schedule.getAssignments()) {
            OperationPlanningFact operationFact = toOperationFact(assignment, schedule.getResources());
            AssignmentPlanningModel planningAssignment = new AssignmentPlanningModel(
                assignment.getId(),
                operationFact
            );
            planningAssignment.setAssignedResource(assignment.getAssignedResource());
            planningAssignment.setStartTime(assignment.getStartTime());
            planningAssignment.setEndTime(assignment.getEndTime());
            planningAssignment.setPinned(assignment.getPinned());
            planningAssignment.setEligibleResources(new ArrayList<>(operationFact.getEligibleResources()));
            planningAssignment.setCandidateMolds(new ArrayList<>(operationFact.getCandidateMolds()));
            planningAssignment.setPreferredMold(operationFact.getPreferredMold());
            planningAssignment.setPreferredMoldPriority(operationFact.getPreferredMoldPriority());
            planningAssignment.setPreferredChangeoverTimeMinutes(operationFact.getPreferredChangeoverTimeMinutes());
            planningAssignments.add(planningAssignment);
        }
        model.setAssignments(planningAssignments);
        model.setResources(new ArrayList<>(schedule.getResources()));
        return model;
    }

    public void updateScheduleFromModel(Schedule schedule, SchedulePlanningModel model) {
        if (model.getScore() != null) {
            schedule.setFinalScore(model.getScore().toString());
        }

        Map<java.util.UUID, Assignment> assignmentMap = new HashMap<>();
        for (Assignment assignment : schedule.getAssignments()) {
            assignmentMap.put(assignment.getId(), assignment);
        }

        for (AssignmentPlanningModel planningAssignment : model.getAssignments()) {
            Assignment assignment = assignmentMap.get(planningAssignment.getAssignmentId());
            if (assignment != null) {
                assignment.setAssignedResource(planningAssignment.getAssignedResource());
                assignment.setStartTime(planningAssignment.getStartTime());
                assignment.setEndTime(planningAssignment.getEndTime());
                assignment.setPinned(planningAssignment.getPinned());
            }
        }
    }

    private OperationPlanningFact toOperationFact(Assignment assignment, List<Resource> scheduleResources) {
        if (assignment.getOperation() == null) {
            return OperationPlanningFact.builder()
                    .eligibleResources(new ArrayList<>(scheduleResources))
                    .candidateMolds(new ArrayList<>())
                    .build();
        }

        Material requiredMaterial = assignment.getOperation().getRequiredMaterial();
        Mold lockedMold = assignment.getOperation().getRequiredMold();
        List<MaterialMoldBinding> bindings = requiredMaterial == null || requiredMaterial.getMoldBindings() == null
                ? List.of()
                : requiredMaterial.getMoldBindings().stream()
                .filter(binding -> Boolean.TRUE.equals(binding.getEnabled()))
                .sorted((left, right) -> {
                    int defaultCompare = Boolean.compare(Boolean.TRUE.equals(right.getIsDefault()), Boolean.TRUE.equals(left.getIsDefault()));
                    if (defaultCompare != 0) {
                        return defaultCompare;
                    }
                    int preferredCompare = Boolean.compare(Boolean.TRUE.equals(right.getIsPreferred()), Boolean.TRUE.equals(left.getIsPreferred()));
                    if (preferredCompare != 0) {
                        return preferredCompare;
                    }
                    return Integer.compare(right.getPriority() == null ? 0 : right.getPriority(), left.getPriority() == null ? 0 : left.getPriority());
                })
                .toList();

        Set<Resource> eligibleResources = new LinkedHashSet<>();
        List<Mold> candidateMolds = new ArrayList<>();
        MaterialMoldBinding preferredBinding = null;

        for (MaterialMoldBinding binding : bindings) {
            Mold mold = binding.getMold();
            if (mold == null || !Boolean.TRUE.equals(mold.getEnabled())) {
                continue;
            }
            if (lockedMold != null && !lockedMold.getId().equals(mold.getId())) {
                continue;
            }
            if (preferredBinding == null) {
                preferredBinding = binding;
            }
            candidateMolds.add(mold);
            for (Resource resource : scheduleResources) {
                if (isResourceEligible(resource, mold)) {
                    eligibleResources.add(resource);
                }
            }
        }

        if (eligibleResources.isEmpty() && lockedMold != null) {
            for (Resource resource : scheduleResources) {
                if (isResourceEligible(resource, lockedMold)) {
                    eligibleResources.add(resource);
                }
            }
            candidateMolds = List.of(lockedMold);
        }

        return OperationPlanningFact.builder()
                .operationId(assignment.getOperation().getId())
                .orderId(assignment.getOperation().getOrder() == null ? null : assignment.getOperation().getOrder().getId())
                .sequence(assignment.getOperation().getSequence())
                .standardDuration(assignment.getOperation().getStandardDuration())
                .dueDate(assignment.getOperation().getOrder() == null ? null : assignment.getOperation().getOrder().getDueDate())
                .priority(assignment.getOperation().getOrder() == null ? null : assignment.getOperation().getOrder().getPriority())
                .requiredMaterialId(requiredMaterial == null ? null : requiredMaterial.getId())
                .lockedMoldId(lockedMold == null ? null : lockedMold.getId())
                .eligibleResources(eligibleResources.isEmpty() ? new ArrayList<>(scheduleResources) : new ArrayList<>(eligibleResources))
                .candidateMolds(candidateMolds)
                .preferredMold(preferredBinding == null ? lockedMold : preferredBinding.getMold())
                .preferredMoldPriority(preferredBinding == null ? null : preferredBinding.getPriority())
                .preferredChangeoverTimeMinutes(preferredBinding == null ? null : preferredBinding.getChangeoverTimeMinutes())
                .build();
    }

    private boolean isResourceEligible(Resource resource, Mold mold) {
        if (resource == null || mold == null) {
            return false;
        }
        if (!Boolean.TRUE.equals(resource.getAvailable())) {
            return false;
        }
        Integer requiredTonnage = mold.getRequiredTonnage();
        if (requiredTonnage != null && resource.getTonnage() != null && resource.getTonnage() < requiredTonnage) {
            return false;
        }
        BigDecimal moldMaxShotWeight = mold.getMaxShotWeight();
        if (moldMaxShotWeight != null && resource.getMaxShotWeight() != null
                && resource.getMaxShotWeight().compareTo(moldMaxShotWeight) < 0) {
            return false;
        }
        return true;
    }
}
