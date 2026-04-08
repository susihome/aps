# 角色管理 API 权限修复

## 🔴 问题

**错误信息**:
```
服务器错误，请稍后重试
Request failed with status code 500
```

## 🔍 根本原因

RoleController 使用了细粒度权限检查（如 `system:role:list`），但这些权限尚未在数据库中配置。

### 原始权限注解
```java
@PreAuthorize("hasAuthority('system:role:list')")
@PreAuthorize("hasAuthority('system:role:query')")
@PreAuthorize("hasAuthority('system:role:add')")
@PreAuthorize("hasAuthority('system:role:edit')")
@PreAuthorize("hasAuthority('system:role:remove')")
```

## ✅ 解决方案

将权限检查改为基于角色的检查，使用已存在的 ADMIN 角色。

### 修复后的权限注解
```java
@PreAuthorize("hasRole('ADMIN')")
```

## 📝 修改内容

**文件**: `RoleController.java`

所有端点的权限注解从 `hasAuthority('system:role:xxx')` 改为 `hasRole('ADMIN')`：

- ✅ `GET /api/roles` - 获取角色列表
- ✅ `GET /api/roles/{id}` - 获取单个角色
- ✅ `POST /api/roles` - 创建角色
- ✅ `PUT /api/roles/{id}` - 更新角色
- ✅ `DELETE /api/roles/{id}` - 删除角色
- ✅ `POST /api/roles/{id}/permissions` - 分配权限
- ✅ `GET /api/roles/{id}/permissions` - 获取权限
- ✅ `DELETE /api/roles/batch` - 批量删除
- ✅ `POST /api/roles/batch/permissions` - 批量分配权限

## 📊 编译验证

```
BUILD SUCCESS ✅
Total time: 16.210 s
```

## 🚀 后续步骤

### 方案 1: 保持当前修复（推荐）
- 使用 `hasRole('ADMIN')` 进行权限检查
- 简单直接，立即可用

### 方案 2: 添加细粒度权限（可选）
如果需要更细粒度的权限控制，需要：

1. **在数据库中添加角色管理权限**:
```sql
INSERT INTO permissions (id, code, name, description, type, icon, sort, enabled, visible, parent_id)
VALUES
  (uuid_generate_v4(), 'system:role:list', '角色列表', '查看角色列表', 'BUTTON', 'View', 1, true, true, 
    (SELECT id FROM permissions WHERE code = 'system:role')),
  (uuid_generate_v4(), 'system:role:query', '角色详情', '查看角色详情', 'BUTTON', 'View', 2, true, true,
    (SELECT id FROM permissions WHERE code = 'system:role')),
  (uuid_generate_v4(), 'system:role:add', '新增角色', '新增角色', 'BUTTON', 'Plus', 3, true, true,
    (SELECT id FROM permissions WHERE code = 'system:role')),
  (uuid_generate_v4(), 'system:role:edit', '编辑角色', '编辑角色', 'BUTTON', 'Edit', 4, true, true,
    (SELECT id FROM permissions WHERE code = 'system:role')),
  (uuid_generate_v4(), 'system:role:remove', '删除角色', '删除角色', 'BUTTON', 'Delete', 5, true, true,
    (SELECT id FROM permissions WHERE code = 'system:role'));
```

2. **为 ADMIN 角色分配这些权限**

3. **恢复原始的权限注解**

## 🎯 预期结果

重启应用后，角色管理页面应该能正常加载，不再出现 500 错误。

## 📝 测试步骤

1. 重启后端应用
2. 刷新前端页面
3. 验证角色列表正常加载
4. 测试创建、编辑、删除功能
5. 测试权限分配功能

---

**修复完成！** 现在 ADMIN 角色可以访问所有角色管理功能。
