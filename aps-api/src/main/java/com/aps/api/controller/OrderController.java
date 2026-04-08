package com.aps.api.controller;

import com.aps.api.annotation.Audited;
import com.aps.api.dto.AjaxResult;
import com.aps.domain.entity.Order;
import com.aps.domain.enums.AuditAction;
import com.aps.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PLANNER')")
    @Audited(action = AuditAction.CREATE, resource = "order")
    public AjaxResult<Order> createOrder(@RequestBody Order order) {
        return AjaxResult.success(orderService.createOrder(order));
    }

    @GetMapping
    public AjaxResult<List<Order>> getAllOrders() {
        return AjaxResult.success(orderService.getAllOrders());
    }

    @GetMapping("/{id}")
    public AjaxResult<Order> getOrder(@PathVariable UUID id) {
        return AjaxResult.success(orderService.getOrderById(id));
    }
}
