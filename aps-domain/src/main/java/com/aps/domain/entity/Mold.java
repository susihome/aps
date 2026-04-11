package com.aps.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "molds")
@Getter
@Setter
public class Mold extends BaseEntity {

    @Column(name = "mold_code", nullable = false, unique = true, length = 64)
    private String moldCode;

    @Column(name = "mold_name", nullable = false, length = 120)
    private String moldName;

    @Column(name = "cavity_count")
    private Integer cavityCount;

    @Column(length = 20)
    private String status;

    @Column(nullable = false)
    private Boolean enabled = true;

    @Column(length = 500)
    private String remark;
}
