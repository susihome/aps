-- 补齐系统管理模块遗漏的权限种子数据
-- 包含：审计日志、工厂日历

-- 1. 审计日志 菜单
INSERT INTO permissions (id, code, name, description, type, icon, sort, enabled, visible, parent_id, create_time, update_time)
SELECT uuid_generate_v4(), 'system:audit-log', '审计日志', '审计日志页面', 'MENU', 'Document', 4, true, true,
       (SELECT id FROM permissions WHERE code = 'system'), NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE code = 'system:audit-log'
);

INSERT INTO permissions (id, code, name, description, type, icon, sort, enabled, visible, parent_id, create_time, update_time)
SELECT uuid_generate_v4(), 'system:audit-log:list', '查看审计日志', '分页查看审计日志', 'BUTTON', 'View', 1, true, true,
       (SELECT id FROM permissions WHERE code = 'system:audit-log'), NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE code = 'system:audit-log:list'
);

INSERT INTO permissions (id, code, name, description, type, icon, sort, enabled, visible, parent_id, create_time, update_time)
SELECT uuid_generate_v4(), 'system:audit-log:query', '审计日志详情', '查看审计日志详情', 'BUTTON', 'View', 2, true, true,
       (SELECT id FROM permissions WHERE code = 'system:audit-log'), NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE code = 'system:audit-log:query'
);

INSERT INTO permissions (id, code, name, description, type, icon, sort, enabled, visible, parent_id, create_time, update_time)
SELECT uuid_generate_v4(), 'system:audit-log:search', '搜索审计日志', '按条件搜索审计日志', 'BUTTON', 'Search', 3, true, true,
       (SELECT id FROM permissions WHERE code = 'system:audit-log'), NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE code = 'system:audit-log:search'
);

INSERT INTO permissions (id, code, name, description, type, icon, sort, enabled, visible, parent_id, create_time, update_time)
SELECT uuid_generate_v4(), 'system:audit-log:statistics', '审计日志统计', '查看审计日志统计分析', 'BUTTON', 'DataAnalysis', 4, true, true,
       (SELECT id FROM permissions WHERE code = 'system:audit-log'), NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE code = 'system:audit-log:statistics'
);

INSERT INTO permissions (id, code, name, description, type, icon, sort, enabled, visible, parent_id, create_time, update_time)
SELECT uuid_generate_v4(), 'system:audit-log:export', '导出审计日志', '导出审计日志CSV', 'BUTTON', 'Download', 5, true, true,
       (SELECT id FROM permissions WHERE code = 'system:audit-log'), NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE code = 'system:audit-log:export'
);

-- 2. 工厂日历 菜单
INSERT INTO permissions (id, code, name, description, type, icon, sort, enabled, visible, parent_id, create_time, update_time)
SELECT uuid_generate_v4(), 'system:factory-calendar', '工厂日历', '工厂日历页面', 'MENU', 'Calendar', 6, true, true,
       (SELECT id FROM permissions WHERE code = 'system'), NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE code = 'system:factory-calendar'
);

INSERT INTO permissions (id, code, name, description, type, icon, sort, enabled, visible, parent_id, create_time, update_time)
SELECT uuid_generate_v4(), 'system:factory-calendar:list', '查看工厂日历', '查看工厂日历列表', 'BUTTON', 'View', 1, true, true,
       (SELECT id FROM permissions WHERE code = 'system:factory-calendar'), NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE code = 'system:factory-calendar:list'
);

INSERT INTO permissions (id, code, name, description, type, icon, sort, enabled, visible, parent_id, create_time, update_time)
SELECT uuid_generate_v4(), 'system:factory-calendar:add', '新增工厂日历', '新增工厂日历', 'BUTTON', 'Plus', 2, true, true,
       (SELECT id FROM permissions WHERE code = 'system:factory-calendar'), NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE code = 'system:factory-calendar:add'
);

INSERT INTO permissions (id, code, name, description, type, icon, sort, enabled, visible, parent_id, create_time, update_time)
SELECT uuid_generate_v4(), 'system:factory-calendar:edit', '编辑工厂日历', '编辑工厂日历与班次日期', 'BUTTON', 'Edit', 3, true, true,
       (SELECT id FROM permissions WHERE code = 'system:factory-calendar'), NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE code = 'system:factory-calendar:edit'
);

INSERT INTO permissions (id, code, name, description, type, icon, sort, enabled, visible, parent_id, create_time, update_time)
SELECT uuid_generate_v4(), 'system:factory-calendar:delete', '删除工厂日历', '删除工厂日历', 'BUTTON', 'Delete', 4, true, true,
       (SELECT id FROM permissions WHERE code = 'system:factory-calendar'), NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE code = 'system:factory-calendar:delete'
);

INSERT INTO permissions (id, code, name, description, type, icon, sort, enabled, visible, parent_id, create_time, update_time)
SELECT uuid_generate_v4(), 'system:factory-calendar:set-default', '设置默认日历', '设置默认工厂日历', 'BUTTON', 'Select', 5, true, true,
       (SELECT id FROM permissions WHERE code = 'system:factory-calendar'), NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE code = 'system:factory-calendar:set-default'
);

-- 3. 将新增系统管理权限授予 ADMIN 角色
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.code IN (
    'system:audit-log',
    'system:audit-log:list',
    'system:audit-log:query',
    'system:audit-log:search',
    'system:audit-log:statistics',
    'system:audit-log:export',
    'system:factory-calendar',
    'system:factory-calendar:list',
    'system:factory-calendar:add',
    'system:factory-calendar:edit',
    'system:factory-calendar:delete',
    'system:factory-calendar:set-default'
)
WHERE r.name = 'ADMIN'
  AND NOT EXISTS (
    SELECT 1
    FROM role_permissions rp
    WHERE rp.role_id = r.id
      AND rp.permission_id = p.id
  );
