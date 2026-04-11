package com.aps.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "operations")
@Getter
@Setter
public class Operation extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    private String operationCode;
    private String operationName;
    private Integer sequence;
    private Integer standardDuration;

    @ManyToOne
    @JoinColumn(name = "required_resource_id")
    private Resource requiredResource;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "required_material_id")
    private Material requiredMaterial;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "required_mold_id")
    private Mold requiredMold;
}
