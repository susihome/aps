package com.aps.solver;

import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.test.api.score.stream.ConstraintVerifier;
import com.aps.domain.entity.Mold;
import com.aps.domain.entity.Resource;
import com.aps.domain.enums.OrderPriority;
import com.aps.solver.model.AssignmentPlanningModel;
import com.aps.solver.model.OperationPlanningFact;
import com.aps.solver.model.SchedulePlanningModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

class ApsConstraintProviderTest {

    private ConstraintVerifier<ApsConstraintProvider, SchedulePlanningModel> constraintVerifier;

    private static final LocalDateTime SCHEDULE_START = LocalDateTime.of(2026, 4, 2, 8, 0);
    private static final LocalDateTime SCHEDULE_END = LocalDateTime.of(2026, 4, 3, 8, 0);

    @BeforeEach
    void setUp() {
        constraintVerifier = ConstraintVerifier.build(
                new ApsConstraintProvider(),
                SchedulePlanningModel.class,
                AssignmentPlanningModel.class
        );
    }

    @Test
    void resourceEligibility_shouldPenalizeIneligibleResource() {
        Resource eligible = createResource("R1", 300, 100);
        Resource ineligible = createResource("R2", 150, 30);
        assertNotEquals(eligible, ineligible);

        AssignmentPlanningModel assignment = createAssignment(createOperationFact(1, 60), ineligible);
        assignment.setEligibleResources(List.of(eligible));
        assignment.setStartTime(SCHEDULE_START);
        assignment.setEndTime(SCHEDULE_START.plusMinutes(60));

        SchedulePlanningModel solution = buildSolution(List.of(eligible, ineligible), List.of(assignment));

        constraintVerifier.verifyThat(ApsConstraintProvider::resourceEligibility)
                .givenSolution(solution)
                .penalizesBy(1);
    }

    @Test
    void resourceEligibility_shouldNotPenalizeEligibleResource() {
        Resource eligible = createResource("R1", 300, 100);

        AssignmentPlanningModel assignment = createAssignment(createOperationFact(1, 60), eligible);
        assignment.setEligibleResources(List.of(eligible));
        assignment.setStartTime(SCHEDULE_START);
        assignment.setEndTime(SCHEDULE_START.plusMinutes(60));

        SchedulePlanningModel solution = buildSolution(List.of(eligible), List.of(assignment));

        constraintVerifier.verifyThat(ApsConstraintProvider::resourceEligibility)
                .givenSolution(solution)
                .penalizesBy(0);
    }

    @Test
    void preferPreferredMold_shouldPenalizeLockedNonPreferredMold() {
        Mold preferred = createMold("MOLD-A");
        Mold locked = createMold("MOLD-B");

        Resource resource = createResource("R1", 300, 100);
        AssignmentPlanningModel assignment = createAssignment(createOperationFact(1, 60), resource);
        assignment.setPreferredMold(preferred);
        assignment.setPreferredMoldPriority(5);
        assignment.getOperation().setRequiredMaterialId(UUID.randomUUID());
        assignment.getOperation().setLockedMoldId(locked.getId());
        assignment.setStartTime(SCHEDULE_START);
        assignment.setEndTime(SCHEDULE_START.plusMinutes(60));

        SchedulePlanningModel solution = buildSolution(List.of(resource), List.of(assignment));

        constraintVerifier.verifyThat(ApsConstraintProvider::preferPreferredMold)
                .givenSolution(solution)
                .penalizesBy(5);
    }

    @Test
    void minimizeChangeover_shouldPenalizeAdjacentMoldSwitch() {
        Resource resource = createResource("R1", 300, 100);

        AssignmentPlanningModel previous = createAssignment(createOperationFact(1, 60), resource);
        previous.setPreferredMold(createMold("MOLD-A"));
        previous.setPreferredChangeoverTimeMinutes(10);
        previous.setStartTime(SCHEDULE_START);
        previous.setEndTime(SCHEDULE_START.plusMinutes(60));

        AssignmentPlanningModel current = createAssignment(createOperationFact(2, 60), resource);
        current.setPreferredMold(createMold("MOLD-B"));
        current.setPreferredChangeoverTimeMinutes(20);
        current.setStartTime(SCHEDULE_START.plusMinutes(60));
        current.setEndTime(SCHEDULE_START.plusMinutes(120));

        SchedulePlanningModel solution = buildSolution(List.of(resource), List.of(previous, current));

        constraintVerifier.verifyThat(ApsConstraintProvider::minimizeChangeover)
                .givenSolution(solution)
                .penalizesBy(20);
    }

    private SchedulePlanningModel buildSolution(List<Resource> resources, List<AssignmentPlanningModel> assignments) {
        SchedulePlanningModel solution = new SchedulePlanningModel(UUID.randomUUID(), SCHEDULE_START, SCHEDULE_END);
        solution.setResources(new ArrayList<>(resources));
        solution.setAssignments(new ArrayList<>(assignments));
        return solution;
    }

    private AssignmentPlanningModel createAssignment(OperationPlanningFact operation, Resource resource) {
        AssignmentPlanningModel assignment = new AssignmentPlanningModel(UUID.randomUUID(), operation);
        assignment.setAssignedResource(resource);
        return assignment;
    }

    private OperationPlanningFact createOperationFact(int sequence, int duration) {
        return OperationPlanningFact.builder()
                .operationId(UUID.randomUUID())
                .orderId(UUID.randomUUID())
                .sequence(sequence)
                .standardDuration(duration)
                .dueDate(LocalDateTime.of(2026, 4, 3, 12, 0))
                .priority(OrderPriority.NORMAL)
                .eligibleResources(new ArrayList<>())
                .candidateMolds(new ArrayList<>())
                .build();
    }

    private Resource createResource(String code, Integer tonnage, int maxShotWeight) {
        Resource resource = new Resource();
        resource.setId(UUID.randomUUID());
        resource.setResourceCode(code);
        resource.setResourceName("Resource " + code);
        resource.setAvailable(true);
        resource.setTonnage(tonnage);
        resource.setMaxShotWeight(BigDecimal.valueOf(maxShotWeight));
        return resource;
    }

    private Mold createMold(String code) {
        Mold mold = new Mold();
        mold.setId(UUID.randomUUID());
        mold.setMoldCode(code);
        return mold;
    }
}