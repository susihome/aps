package com.aps.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "workshops")
@Getter
@Setter
public class Workshop extends BaseEntity {

    @Column(unique = true, nullable = false, length = 50)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "calendar_id")
    private FactoryCalendar calendar;

    @Column(name = "manager_name", length = 50)
    private String managerName;

    @Column(nullable = false)
    private Boolean enabled = true;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Column(length = 500)
    private String description;
}
