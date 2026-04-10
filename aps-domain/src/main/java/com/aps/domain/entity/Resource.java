package com.aps.domain.entity;

import com.aps.domain.enums.MachineStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "resources")
@Getter
@Setter
public class Resource extends BaseEntity {

    @Column(unique = true, nullable = false)
    private String resourceCode;

    private String resourceName;

    private String resourceType;

    private Boolean available;

    // ===== 注塑机增强属性 =====

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workshop_id")
    private Workshop workshop;

    @Column
    private Integer tonnage;

    @Column(name = "machine_brand", length = 50)
    private String machineBrand;

    @Column(name = "machine_model", length = 50)
    private String machineModel;

    @Column(name = "max_shot_weight", precision = 10, scale = 2)
    private BigDecimal maxShotWeight;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private MachineStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "calendar_id")
    private FactoryCalendar calendar;
}
