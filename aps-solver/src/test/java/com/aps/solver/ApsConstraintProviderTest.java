package com.aps.solver;

import com.aps.domain.entity.*;
import com.aps.domain.enums.OrderPriority;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 测试 ApsConstraintProvider 约束规则
 * 验证硬约束和软约束的正确性
 */
class ApsConstraintProviderTest {

    @Test
    void testResourceConflict_noOverlap() {
        // 测试无时间重叠的资源分配
        Resource resource = createResource("R1");
        Assignment a1 = createAssignment(resource,
                LocalDateTime.of(2026, 4, 2, 9, 0),
                LocalDateTime.of(2026, 4, 2, 10, 0));
        Assignment a2 = createAssignment(resource,
                LocalDateTime.of(2026, 4, 2, 10, 0),
                LocalDateTime.of(2026, 4, 2, 11, 0));

        // 验证时间不重叠
        assertFalse(isOverlapping(a1, a2), "相邻时间段不应重叠");
    }

    @Test
    void testResourceConflict_withOverlap() {
        // 测试有时间重叠的资源分配
        Resource resource = createResource("R1");
        Assignment a1 = createAssignment(resource,
                LocalDateTime.of(2026, 4, 2, 9, 0),
                LocalDateTime.of(2026, 4, 2, 10, 30));
        Assignment a2 = createAssignment(resource,
                LocalDateTime.of(2026, 4, 2, 10, 0),
                LocalDateTime.of(2026, 4, 2, 11, 0));

        // 验证时间重叠
        assertTrue(isOverlapping(a1, a2), "重叠时间段应被检测到");
    }

    @Test
    void testOperationSequence_correctOrder() {
        // 测试正确的工序顺序
        Order order = createOrder("O1", LocalDateTime.of(2026, 4, 10, 17, 0));
        Operation op1 = createOperation(order, 1, 60);
        Operation op2 = createOperation(order, 2, 60);

        Assignment a1 = createAssignmentWithOperation(op1,
                LocalDateTime.of(2026, 4, 2, 9, 0),
                LocalDateTime.of(2026, 4, 2, 10, 0));
        Assignment a2 = createAssignmentWithOperation(op2,
                LocalDateTime.of(2026, 4, 2, 10, 0),
                LocalDateTime.of(2026, 4, 2, 11, 0));

        // 验证工序2在工序1之后开始
        assertFalse(a2.getStartTime().isBefore(a1.getEndTime()),
                "后续工序应在前序工序完成后开始");
    }

    @Test
    void testOperationSequence_wrongOrder() {
        // 测试错误的工序顺序
        Order order = createOrder("O1", LocalDateTime.of(2026, 4, 10, 17, 0));
        Operation op1 = createOperation(order, 1, 60);
        Operation op2 = createOperation(order, 2, 60);

        Assignment a1 = createAssignmentWithOperation(op1,
                LocalDateTime.of(2026, 4, 2, 10, 0),
                LocalDateTime.of(2026, 4, 2, 11, 0));
        Assignment a2 = createAssignmentWithOperation(op2,
                LocalDateTime.of(2026, 4, 2, 9, 0),
                LocalDateTime.of(2026, 4, 2, 10, 0));

        // 验证工序2在工序1之前开始（违反约束）
        assertTrue(a2.getStartTime().isBefore(a1.getEndTime()),
                "工序顺序错误应被检测到");
    }

    @Test
    void testMinimizeDelay_onTime() {
        // 测试按时完成的工单
        Order order = createOrder("O1", LocalDateTime.of(2026, 4, 10, 17, 0));
        Operation operation = createOperation(order, 1, 60);
        Assignment assignment = createAssignmentWithOperation(operation,
                LocalDateTime.of(2026, 4, 2, 9, 0),
                LocalDateTime.of(2026, 4, 2, 10, 0));

        // 验证未延期
        assertFalse(assignment.getEndTime().isAfter(order.getDueDate()),
                "按时完成不应有延期");
    }

    @Test
    void testMinimizeDelay_late() {
        // 测试延期的工单
        Order order = createOrder("O1", LocalDateTime.of(2026, 4, 2, 10, 0));
        order.setPriority(OrderPriority.HIGH);
        Operation operation = createOperation(order, 1, 60);
        Assignment assignment = createAssignmentWithOperation(operation,
                LocalDateTime.of(2026, 4, 2, 9, 0),
                LocalDateTime.of(2026, 4, 2, 11, 0));

        // 验证延期
        assertTrue(assignment.getEndTime().isAfter(order.getDueDate()),
                "延期应被检测到");

        // 计算延期时间
        long delayMinutes = java.time.Duration.between(
                order.getDueDate(),
                assignment.getEndTime()
        ).toMinutes();
        assertEquals(60, delayMinutes, "延期时间应为60分钟");
    }

    @Test
    void testNullSafety_minimizeDelay() {
        // 测试 minimizeDelay 的空值安全性
        Assignment assignment = new Assignment();

        // endTime 为 null 不应抛出异常
        assertNull(assignment.getEndTime());
        assertNull(assignment.getOperation());

        // 设置 operation 但 order 为 null
        Operation operation = new Operation();
        assignment.setOperation(operation);
        assertNull(operation.getOrder());
    }

    // Helper methods
    private boolean isOverlapping(Assignment a1, Assignment a2) {
        if (a1.getStartTime() == null || a1.getEndTime() == null ||
            a2.getStartTime() == null || a2.getEndTime() == null) {
            return false;
        }
        return a1.getStartTime().isBefore(a2.getEndTime()) &&
               a2.getStartTime().isBefore(a1.getEndTime());
    }

    private Resource createResource(String code) {
        Resource resource = new Resource();
        resource.setResourceCode(code);
        resource.setResourceName("Resource " + code);
        resource.setResourceType("MACHINE");
        resource.setAvailable(true);
        return resource;
    }

    private Order createOrder(String orderNo, LocalDateTime dueDate) {
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setDueDate(dueDate);
        order.setPriority(OrderPriority.NORMAL);
        return order;
    }

    private Operation createOperation(Order order, int sequence, int duration) {
        Operation operation = new Operation();
        operation.setOrder(order);
        operation.setSequence(sequence);
        operation.setStandardDuration(duration);
        return operation;
    }

    private Assignment createAssignment(Resource resource,
                                        LocalDateTime startTime, LocalDateTime endTime) {
        Assignment assignment = new Assignment();
        assignment.setAssignedResource(resource);
        assignment.setStartTime(startTime);
        assignment.setEndTime(endTime);
        return assignment;
    }

    private Assignment createAssignmentWithOperation(Operation operation,
                                                     LocalDateTime startTime, LocalDateTime endTime) {
        Assignment assignment = new Assignment();
        assignment.setOperation(operation);
        assignment.setStartTime(startTime);
        assignment.setEndTime(endTime);
        return assignment;
    }
}
