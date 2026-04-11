-- 排产管理目录（父节点）
INSERT INTO permissions (id, code, name, description, type, icon, sort, enabled, visible, parent_id, create_time, update_time)
SELECT uuid_generate_v4(), 'schedule', '排产管理', '排产管理模块', 'CATALOG', 'SetUp', 2, true, true, NULL, NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE code = 'schedule'
);

-- 排程时间参数菜单
INSERT INTO permissions (id, code, name, description, type, icon, sort, enabled, visible, parent_id, create_time, update_time)
SELECT uuid_generate_v4(), 'schedule:time-param', '排程时间参数', '排程时间参数配置页面', 'MENU', 'Timer', 1, true, true,
       (SELECT id FROM permissions WHERE code = 'schedule'), NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE code = 'schedule:time-param'
);

INSERT INTO permissions (id, code, name, description, type, icon, sort, enabled, visible, parent_id, create_time, update_time)
SELECT uuid_generate_v4(), 'schedule:time-param:list', '查看排程时间参数', '查看排程时间参数列表', 'BUTTON', 'View', 1, true, true,
       (SELECT id FROM permissions WHERE code = 'schedule:time-param'), NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE code = 'schedule:time-param:list'
);

INSERT INTO permissions (id, code, name, description, type, icon, sort, enabled, visible, parent_id, create_time, update_time)
SELECT uuid_generate_v4(), 'schedule:time-param:add', '新增排程时间参数', '新增排程时间参数', 'BUTTON', 'Plus', 2, true, true,
       (SELECT id FROM permissions WHERE code = 'schedule:time-param'), NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE code = 'schedule:time-param:add'
);

INSERT INTO permissions (id, code, name, description, type, icon, sort, enabled, visible, parent_id, create_time, update_time)
SELECT uuid_generate_v4(), 'schedule:time-param:edit', '编辑排程时间参数', '编辑排程时间参数', 'BUTTON', 'Edit', 3, true, true,
       (SELECT id FROM permissions WHERE code = 'schedule:time-param'), NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE code = 'schedule:time-param:edit'
);

INSERT INTO permissions (id, code, name, description, type, icon, sort, enabled, visible, parent_id, create_time, update_time)
SELECT uuid_generate_v4(), 'schedule:time-param:remove', '删除排程时间参数', '删除排程时间参数', 'BUTTON', 'Delete', 4, true, true,
       (SELECT id FROM permissions WHERE code = 'schedule:time-param'), NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE code = 'schedule:time-param:remove'
);

INSERT INTO permissions (id, code, name, description, type, icon, sort, enabled, visible, parent_id, create_time, update_time)
SELECT uuid_generate_v4(), 'schedule:time-param:preview', '预览排程时间参数', '预览排程时间参数计算结果', 'BUTTON', 'View', 5, true, true,
       (SELECT id FROM permissions WHERE code = 'schedule:time-param'), NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE code = 'schedule:time-param:preview'
);

-- 授予 ADMIN 和 PLANNER 角色权限
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.code IN (
    'schedule',
    'schedule:time-param',
    'schedule:time-param:list',
    'schedule:time-param:add',
    'schedule:time-param:edit',
    'schedule:time-param:remove',
    'schedule:time-param:preview'
)
WHERE r.name IN ('ADMIN', 'PLANNER')
  AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
  );
