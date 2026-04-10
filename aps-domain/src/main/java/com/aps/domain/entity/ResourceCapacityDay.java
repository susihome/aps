package com.aps.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(
        name = "resource_capacity_days",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_resource_capacity_days_resource_date", columnNames = {"resource_id", "capacity_date"})
        }
)
@Getter
@Setter
public class ResourceCapacityDay extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resource_id", nullable = false)
    private Resource resource;

    @Column(name = "capacity_date", nullable = false)
    private LocalDate capacityDate;

    @Column(name = "shift_minutes_override")
    private Integer shiftMinutesOverride;

    @Column(name = "utilization_rate", nullable = false, precision = 5, scale = 4)
    private BigDecimal utilizationRate = BigDecimal.ONE;

    @Column(length = 255)
    private String remark;
}
