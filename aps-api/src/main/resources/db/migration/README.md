# 数据库迁移脚本

## 概述

本项目使用 Flyway 进行数据库版本管理和迁移。

## 迁移脚本命名规范

- `V{version}__{description}.sql` - 版本迁移脚本
- `R__{description}.sql` - 可重复执行的脚本

示例：
- `V1__Initial_Schema.sql` - 初始化数据库架构
- `V2__Add_User_Avatar.sql` - 添加用户头像字段

## 已有迁移脚本

### V1__Initial_Schema.sql
创建初始数据库架构，包括：

**核心表：**
- `users` - 用户表（UUID 主键）
- `roles` - 角色表
- `permissions` - 权限表
- `user_roles` - 用户角色关联表
- `role_permissions` - 角色权限关联表
- `audit_logs` - 审计日志表

**业务表：**
- `resources` - 资源表（设备/设备）
- `orders` - 工单表
- `operations` - 工序表
- `schedules` - 排产方案表
- `assignments` - 作业分配表

**初始数据：**
- 3 个角色：ADMIN、PLANNER、SUPERVISOR
- 6 个基础权限
- 1 个默认管理员用户（用户名：admin，密码：admin123）

## 执行迁移

### 开发环境

```bash
# 使用 Maven 执行迁移
mvn flyway:migrate

# 查看迁移状态
mvn flyway:info

# 清理数据库（谨慎使用）
mvn flyway:clean
```

### 生产环境

生产环境的迁移会在应用启动时自动执行（通过 Spring Boot 配置）。

## 注意事项

1. **不要修改已执行的迁移脚本** - Flyway 会校验脚本的校验和
2. **使用事务** - 所有迁移脚本应该是事务性的
3. **向后兼容** - 新的迁移应该保持向后兼容
4. **测试迁移** - 在开发环境充分测试后再应用到生产环境
5. **备份数据** - 执行迁移前务必备份数据库

## UUID 主键说明

本项目所有表都使用 UUID 作为主键，优势：
- 分布式系统友好
- 避免主键冲突
- 更好的安全性（不暴露记录数量）

PostgreSQL 使用 `uuid-ossp` 扩展生成 UUID。

## 时间字段规范

所有表都包含：
- `create_time` - 创建时间（自动填充）
- `update_time` - 更新时间（自动更新）

这些字段由 JPA 的 `@PrePersist` 和 `@PreUpdate` 自动管理。
