package com.aps.solver.model;

import com.aps.domain.entity.Order;
import com.aps.domain.enums.OrderPriority;
import java.util.Comparator;

/**
 * AssignmentPlanningModel 难度比较器
 * 用于 Timefold 构造启发式算法
 */
public class AssignmentDifficultyComparator implements Comparator<AssignmentPlanningModel> {

    @Override
    public int compare(AssignmentPlanningModel a1, AssignmentPlanningModel a2) {
        // 按工序的优先级和持续时间排序
        // 优先级高的、耗时长的工序优先分配
        if (a1.getOperation() == null || a2.getOperation() == null) {
            return 0;
        }

        // 比较工单优先级
        int priorityCompare = comparePriority(a1, a2);
        if (priorityCompare != 0) {
            return priorityCompare;
        }

        // 比较工序持续时间（耗时长的优先）
        Integer duration1 = a1.getOperation().getStandardDuration();
        Integer duration2 = a2.getOperation().getStandardDuration();

        if (duration1 != null && duration2 != null) {
            int durationCompare = Integer.compare(duration2, duration1);
            if (durationCompare != 0) {
                return durationCompare;
            }
        }

        // 最后按ID排序保证稳定性
        if (a1.getAssignmentId() != null && a2.getAssignmentId() != null) {
            return a1.getAssignmentId().compareTo(a2.getAssignmentId());
        }

        return 0;
    }

    private int comparePriority(AssignmentPlanningModel a1, AssignmentPlanningModel a2) {
        Order order1 = a1.getOperation().getOrder();
        Order order2 = a2.getOperation().getOrder();

        if (order1 == null || order2 == null) {
            return 0;
        }

        OrderPriority p1 = order1.getPriority();
        OrderPriority p2 = order2.getPriority();

        if (p1 == null || p2 == null) {
            return 0;
        }

        // URGENT > HIGH > NORMAL > LOW
        return Integer.compare(p2.ordinal(), p1.ordinal());
    }
}
