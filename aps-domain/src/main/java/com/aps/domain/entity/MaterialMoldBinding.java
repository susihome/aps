package com.aps.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "material_mold_bindings",
        uniqueConstraints = @UniqueConstraint(name = "uq_material_mold_bindings", columnNames = {"material_id", "mold_id"}))
@Getter
@Setter
public class MaterialMoldBinding extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "material_id", nullable = false)
    private Material material;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mold_id", nullable = false)
    private Mold mold;

    @Column(nullable = false)
    private Integer priority = 0;

    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;

    @Column(name = "is_preferred", nullable = false)
    private Boolean isPreferred = false;

    @Column(name = "cycle_time_minutes")
    private Integer cycleTimeMinutes;

    @Column(name = "setup_time_minutes")
    private Integer setupTimeMinutes;

    @Column(name = "changeover_time_minutes")
    private Integer changeoverTimeMinutes;

    @Column(nullable = false)
    private Boolean enabled = true;

    @Column(name = "valid_from")
    private LocalDateTime validFrom;

    @Column(name = "valid_to")
    private LocalDateTime validTo;

    @Column(length = 500)
    private String remark;
}
