package com.aps.solver.model;

import com.aps.domain.enums.OrderPriority;

import java.util.Comparator;

/**
 * AssignmentPlanningModel 难度比较器
 * 用于 Timefold 构造启发式算法
 */
public class AssignmentDifficultyComparator implements Comparator<AssignmentPlanningModel> {

    @Override
    public int compare(AssignmentPlanningModel a1, AssignmentPlanningModel a2) {
        if (a1.getOperation() == null || a2.getOperation() == null) {
            return 0;
        }

        int priorityCompare = comparePriority(a1, a2);
        if (priorityCompare != 0) {
            return priorityCompare;
        }

        Integer duration1 = a1.getOperation().getStandardDuration();
        Integer duration2 = a2.getOperation().getStandardDuration();

        if (duration1 != null && duration2 != null) {
            int durationCompare = Integer.compare(duration2, duration1);
            if (durationCompare != 0) {
                return durationCompare;
            }
        }

        if (a1.getAssignmentId() != null && a2.getAssignmentId() != null) {
            return a1.getAssignmentId().compareTo(a2.getAssignmentId());
        }

        return 0;
    }

    private int comparePriority(AssignmentPlanningModel a1, AssignmentPlanningModel a2) {
        OrderPriority p1 = a1.getOperation().getPriority();
        OrderPriority p2 = a2.getOperation().getPriority();

        if (p1 == null || p2 == null) {
            return 0;
        }

        return Integer.compare(p2.ordinal(), p1.ordinal());
    }
}
