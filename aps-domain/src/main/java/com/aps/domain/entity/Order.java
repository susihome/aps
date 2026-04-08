package com.aps.domain.entity;

import com.aps.domain.enums.OrderPriority;
import com.aps.domain.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
public class Order extends BaseEntity {
    @Column(unique = true, nullable = false)
    private String orderNo;

    private String productCode;
    private String productName;
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    private OrderPriority priority;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private LocalDateTime dueDate;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<Operation> operations = new ArrayList<>();
}
