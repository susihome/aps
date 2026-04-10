package com.aps.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "sys_dict_item")
@Getter
@Setter
public class DictItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dict_type_id", nullable = false)
    private DictType dictType;

    @Column(name = "item_code", nullable = false, length = 64)
    private String itemCode;

    @Column(name = "item_name", nullable = false, length = 100)
    private String itemName;

    @Column(name = "item_value", nullable = false, length = 100)
    private String itemValue;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private Boolean enabled = true;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @Column(name = "is_system", nullable = false)
    private Boolean isSystem = false;
}
