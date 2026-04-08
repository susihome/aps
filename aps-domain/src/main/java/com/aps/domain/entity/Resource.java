package com.aps.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

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
}
