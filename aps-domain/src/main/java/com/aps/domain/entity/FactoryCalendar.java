package com.aps.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "factory_calendars")
@Getter
@Setter
public class FactoryCalendar extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false, length = 50)
    private String code;

    @Column(length = 255)
    private String description;

    @Column(nullable = false)
    private Integer year;

    @Column(name = "is_default")
    private Boolean isDefault = false;

    @Column(nullable = false)
    private Boolean enabled = true;

    @OneToMany(mappedBy = "calendar", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CalendarShift> shifts = new ArrayList<>();

    @OneToMany(mappedBy = "calendar", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CalendarDate> dates = new ArrayList<>();
}
