package com.aps.domain.entity;

import com.aps.domain.enums.DateType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "calendar_dates")
@Getter
@Setter
public class CalendarDate extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "calendar_id", nullable = false)
    private FactoryCalendar calendar;

    @Column(nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(name = "date_type", nullable = false, length = 20)
    private DateType dateType = DateType.WORKDAY;

    @Column(length = 50)
    private String label;
}
