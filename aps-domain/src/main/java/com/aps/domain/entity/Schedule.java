package com.aps.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "schedules")
@Getter
@Setter
public class Schedule extends BaseEntity {

    private String name;
    private String status;
    private LocalDateTime scheduleStartTime;
    private LocalDateTime scheduleEndTime;

    @Column(length = 100)
    private String finalScore;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "schedule_id")
    private List<Assignment> assignments = new ArrayList<>();

    @ManyToMany
    @JoinTable(
        name = "schedule_resources",
        joinColumns = @JoinColumn(name = "schedule_id"),
        inverseJoinColumns = @JoinColumn(name = "resource_id")
    )
    private List<Resource> resources = new ArrayList<>();
}
