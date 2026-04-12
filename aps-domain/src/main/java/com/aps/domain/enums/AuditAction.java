package com.aps.domain.enums;

public enum AuditAction {
    // 认证相关
    LOGIN,          // 用户登录
    LOGOUT,         // 用户登出
    ACCESS_DENIED,  // 访问被拒绝

    // 排产相关
    SCHEDULE_CREATE,      // 创建排产方案
    SCHEDULE_UPDATE,      // 修改排产方案
    SCHEDULE_DELETE,      // 删除排产方案
    SCHEDULE_SOLVE,       // 启动求解
    SCHEDULE_STOP,        // 停止求解
    SCHEDULE_PUBLISH,     // 发布排产方案

    // 工单相关
    ORDER_CREATE,         // 创建工单
    ORDER_UPDATE,         // 修改工单
    ORDER_DELETE,         // 删除工单
    ORDER_IMPORT,         // 批量导入工单

    // 资源相关
    RESOURCE_CREATE,      // 创建资源
    RESOURCE_UPDATE,      // 修改资源
    RESOURCE_DELETE,      // 删除资源

    // 物料相关
    MATERIAL_CREATE,      // 创建物料
    MATERIAL_UPDATE,      // 修改物料
    MATERIAL_DELETE,      // 删除物料

    // 模具相关
    MOLD_CREATE,          // 创建模具
    MOLD_UPDATE,          // 修改模具
    MOLD_DELETE,          // 删除模具

    // 物料模具关系相关
    MATERIAL_MOLD_BINDING_CREATE, // 创建物料模具关系
    MATERIAL_MOLD_BINDING_UPDATE, // 修改物料模具关系
    MATERIAL_MOLD_BINDING_DELETE, // 删除物料模具关系

    // 用户权限相关
    USER_CREATE,          // 创建用户
    USER_UPDATE,          // 修改用户
    USER_DELETE,          // 删除用户
    USER_DISABLE,         // 禁用用户
    USER_ENABLE,          // 启用用户
    ROLE_ASSIGN,          // 分配角色
    ROLE_REMOVE,          // 移除角色
    PERMISSION_GRANT,     // 授予权限
    PERMISSION_REVOKE,    // 撤销权限

    // 排程时间参数
    SCHEDULE_TIME_PARAM_CREATE,   // 创建排程时间参数
    SCHEDULE_TIME_PARAM_UPDATE,   // 修改排程时间参数
    SCHEDULE_TIME_PARAM_DELETE,   // 删除排程时间参数

    // 系统配置
    CONFIG_UPDATE,        // 修改系统配置

    // 通用操作（保留兼容性）
    CREATE,         // 创建资源
    UPDATE,         // 更新资源
    DELETE          // 删除资源
}
