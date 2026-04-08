package com.aps.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Entity
@Table(name = "permissions")
@Getter
@Setter
public class Permission extends BaseEntity {

    @Column(unique = true, nullable = false, length = 100)
    private String code;  // 例如: "system:user:add"

    @Column(nullable = false, length = 100)
    private String name;  // 权限名称

    @Column(length = 255)
    private String description;  // 权限描述

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PermissionType type;  // 权限类型: CATALOG, MENU, BUTTON

    @Column(length = 255)
    private String routePath;  // 路由路径

    @Column(length = 50)
    private String icon;  // 图标名称

    @Column(nullable = false)
    private Integer sort = 0;  // 排序号

    @Column(nullable = false)
    private Boolean enabled = true;  // 是否启用

    @Column(nullable = false)
    private Boolean visible = true;  // 是否可见

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Permission parent;  // 父权限

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Permission> children;  // 子权限

    public enum PermissionType {
        CATALOG,  // 目录
        MENU,     // 菜单
        BUTTON    // 按钮
    }
}
