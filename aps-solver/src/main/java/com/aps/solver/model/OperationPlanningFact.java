package com.aps.solver.model;

import com.aps.domain.entity.Mold;
import com.aps.domain.entity.Resource;
import com.aps.domain.enums.OrderPriority;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperationPlanningFact {

    private UUID operationId;
    private UUID orderId;
    private Integer sequence;
    private Integer standardDuration;
    private LocalDateTime dueDate;
    private OrderPriority priority;
    private UUID requiredMaterialId;
    private UUID lockedMoldId;
    private List<Resource> eligibleResources = new ArrayList<>();
    private List<Mold> candidateMolds = new ArrayList<>();
    private Mold preferredMold;
    private Integer preferredMoldPriority;
    private Integer preferredChangeoverTimeMinutes;
}
