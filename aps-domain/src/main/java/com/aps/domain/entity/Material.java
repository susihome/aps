package com.aps.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "materials")
@Getter
@Setter
public class Material extends BaseEntity {

    @Column(name = "material_code", nullable = false, unique = true, length = 64)
    private String materialCode;

    @Column(name = "material_name", nullable = false, length = 120)
    private String materialName;

    @Column(length = 255)
    private String specification;

    @Column(length = 32)
    private String unit;

    @Column(nullable = false)
    private Boolean enabled = true;

    @Column(length = 500)
    private String remark;

    // ===== 车间排产属性 =====

    @Column(name = "color_code", length = 32)
    private String colorCode;

    @Column(name = "raw_material_type", length = 32)
    private String rawMaterialType;

    @Column(name = "default_lot_size")
    private Integer defaultLotSize;

    @Column(name = "min_lot_size")
    private Integer minLotSize;

    @Column(name = "max_lot_size")
    private Integer maxLotSize;

    @Column(name = "allow_delay")
    private Boolean allowDelay;

    @Column(name = "abc_classification", length = 1)
    private String abcClassification;

    @Column(name = "product_group", length = 32)
    private String productGroup;

    @OneToMany(mappedBy = "material")
    private List<MaterialMoldBinding> moldBindings = new ArrayList<>();
}
