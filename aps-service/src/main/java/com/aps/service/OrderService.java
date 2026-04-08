package com.aps.service;

import com.aps.domain.annotation.Audited;
import com.aps.domain.entity.Order;
import com.aps.domain.enums.AuditAction;
import com.aps.service.exception.ResourceNotFoundException;
import com.aps.service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    @Transactional
    @Audited(action = AuditAction.ORDER_CREATE, resource = "Order")
    public Order createOrder(Order order) {
        return orderRepository.save(order);
    }

    /**
     * 获取所有订单（分页）
     */
    @Transactional(readOnly = true)
    public Page<Order> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }

    /**
     * 获取所有订单（不分页，仅用于小数据集）
     * @deprecated 使用 getAllOrders(Pageable) 代替
     */
    @Deprecated
    @Transactional(readOnly = true)
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Order getOrderById(UUID id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
    }
}
