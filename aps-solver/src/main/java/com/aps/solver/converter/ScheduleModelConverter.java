package com.aps.solver.converter;

import com.aps.domain.entity.Assignment;
import com.aps.domain.entity.Schedule;
import com.aps.solver.model.AssignmentPlanningModel;
import com.aps.solver.model.SchedulePlanningModel;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Schedule Entity 与 SchedulePlanningModel 转换器
 * 深拷贝策略：确保 Solver 修改不影响原始 JPA 实体
 */
@Component
public class ScheduleModelConverter {

    /**
     * Entity -> Planning Model（深拷贝）
     */
    public SchedulePlanningModel toPlanningModel(Schedule schedule) {
        SchedulePlanningModel model = new SchedulePlanningModel(
            schedule.getId(),
            schedule.getScheduleStartTime(),
            schedule.getScheduleEndTime()
        );

        // 深拷贝 assignments：创建独立的 Planning Model 对象
        List<AssignmentPlanningModel> planningAssignments = new ArrayList<>();
        for (Assignment assignment : schedule.getAssignments()) {
            AssignmentPlanningModel planningAssignment = new AssignmentPlanningModel(
                assignment.getId(),
                assignment.getOperation()
            );
            planningAssignment.setAssignedResource(assignment.getAssignedResource());
            planningAssignment.setStartTime(assignment.getStartTime());
            planningAssignment.setEndTime(assignment.getEndTime());
            planningAssignment.setPinned(assignment.getPinned());

            planningAssignments.add(planningAssignment);
        }
        model.setAssignments(planningAssignments);

        // Resources 作为 problem facts，可以共享引用（只读）
        model.setResources(new ArrayList<>(schedule.getResources()));

        return model;
    }

    /**
     * Planning Model -> Entity（更新求解结果）
     */
    public void updateScheduleFromModel(Schedule schedule, SchedulePlanningModel model) {
        // 更新分数
        if (model.getScore() != null) {
            schedule.setFinalScore(model.getScore().toString());
        }

        // 创建 ID -> Assignment 映射，用于快速查找
        Map<java.util.UUID, Assignment> assignmentMap = new HashMap<>();
        for (Assignment assignment : schedule.getAssignments()) {
            assignmentMap.put(assignment.getId(), assignment);
        }

        // 将 Planning Model 的求解结果回写到 Entity
        for (AssignmentPlanningModel planningAssignment : model.getAssignments()) {
            Assignment assignment = assignmentMap.get(planningAssignment.getAssignmentId());
            if (assignment != null) {
                // 只更新 Solver 修改的字段
                assignment.setAssignedResource(planningAssignment.getAssignedResource());
                assignment.setStartTime(planningAssignment.getStartTime());
                assignment.setEndTime(planningAssignment.getEndTime());
                assignment.setPinned(planningAssignment.getPinned());
            }
        }
    }
}
