-- 基础数据模块权限种子数据
-- 包含：基础数据目录、工厂建模菜单、设备日产能菜单及按钮权限

-- 1. 基础数据 目录
INSERT INTO permissions (id, code, name, description, type, icon, sort, enabled, visible, parent_id, create_time, update_time)
SELECT uuid_generate_v4(), 'basedata', '基础数据', '基础数据模块', 'CATALOG', 'Setting', 3, true, true, NULL, NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE code = 'basedata'
);

-- 2. 工厂建模 菜单
INSERT INTO permissions (id, code, name, description, type, icon, sort, enabled, visible, parent_id, create_time, update_time)
SELECT uuid_generate_v4(), 'basedata:workshop', '工厂建模', '工厂建模页面', 'MENU', 'Setting', 1, true, true,
       (SELECT id FROM permissions WHERE code = 'basedata'), NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE code = 'basedata:workshop'
);

INSERT INTO permissions (id, code, name, description, type, icon, sort, enabled, visible, parent_id, create_time, update_time)
SELECT uuid_generate_v4(), 'basedata:workshop:list', '查看工厂建模', '查看车间与设备列表', 'BUTTON', 'View', 1, true, true,
       (SELECT id FROM permissions WHERE code = 'basedata:workshop'), NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE code = 'basedata:workshop:list'
);

INSERT INTO permissions (id, code, name, description, type, icon, sort, enabled, visible, parent_id, create_time, update_time)
SELECT uuid_generate_v4(), 'basedata:workshop:add', '新增车间/设备', '新增车间或设备', 'BUTTON', 'Plus', 2, true, true,
       (SELECT id FROM permissions WHERE code = 'basedata:workshop'), NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE code = 'basedata:workshop:add'
);

INSERT INTO permissions (id, code, name, description, type, icon, sort, enabled, visible, parent_id, create_time, update_time)
SELECT uuid_generate_v4(), 'basedata:workshop:edit', '编辑车间/设备', '编辑车间或设备', 'BUTTON', 'Edit', 3, true, true,
       (SELECT id FROM permissions WHERE code = 'basedata:workshop'), NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE code = 'basedata:workshop:edit'
);

INSERT INTO permissions (id, code, name, description, type, icon, sort, enabled, visible, parent_id, create_time, update_time)
SELECT uuid_generate_v4(), 'basedata:workshop:delete', '删除车间/设备', '删除车间或设备', 'BUTTON', 'Delete', 4, true, true,
       (SELECT id FROM permissions WHERE code = 'basedata:workshop'), NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE code = 'basedata:workshop:delete'
);

-- 3. 设备日产能 菜单
INSERT INTO permissions (id, code, name, description, type, icon, sort, enabled, visible, parent_id, create_time, update_time)
SELECT uuid_generate_v4(), 'basedata:resource-capacity', '设备日产能', '设备日产能管理页面', 'MENU', 'Cpu', 2, true, true,
       (SELECT id FROM permissions WHERE code = 'basedata'), NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE code = 'basedata:resource-capacity'
);

INSERT INTO permissions (id, code, name, description, type, icon, sort, enabled, visible, parent_id, create_time, update_time)
SELECT uuid_generate_v4(), 'basedata:resource-capacity:list', '查看设备日产能', '查看设备产能列表', 'BUTTON', 'View', 1, true, true,
       (SELECT id FROM permissions WHERE code = 'basedata:resource-capacity'), NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE code = 'basedata:resource-capacity:list'
);

INSERT INTO permissions (id, code, name, description, type, icon, sort, enabled, visible, parent_id, create_time, update_time)
SELECT uuid_generate_v4(), 'basedata:resource-capacity:edit', '编辑设备日产能', '编辑设备产能数据', 'BUTTON', 'Edit', 2, true, true,
       (SELECT id FROM permissions WHERE code = 'basedata:resource-capacity'), NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE code = 'basedata:resource-capacity:edit'
);

INSERT INTO permissions (id, code, name, description, type, icon, sort, enabled, visible, parent_id, create_time, update_time)
SELECT uuid_generate_v4(), 'basedata:resource-capacity:batch-edit', '批量编辑产能', '批量编辑设备产能数据', 'BUTTON', 'Edit', 3, true, true,
       (SELECT id FROM permissions WHERE code = 'basedata:resource-capacity'), NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE code = 'basedata:resource-capacity:batch-edit'
);

-- 4. 将基础数据所有权限授予 ADMIN 角色
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.code IN (
    'basedata',
    'basedata:workshop',
    'basedata:workshop:list',
    'basedata:workshop:add',
    'basedata:workshop:edit',
    'basedata:workshop:delete',
    'basedata:resource-capacity',
    'basedata:resource-capacity:list',
    'basedata:resource-capacity:edit',
    'basedata:resource-capacity:batch-edit'
)
WHERE r.name = 'ADMIN'
  AND NOT EXISTS (
    SELECT 1
    FROM role_permissions rp
    WHERE rp.role_id = r.id
      AND rp.permission_id = p.id
  );

-- 5. 将设备日产能（查看+编辑）权限授予 PLANNER 角色
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.code IN (
    'basedata',
    'basedata:resource-capacity',
    'basedata:resource-capacity:list',
    'basedata:resource-capacity:edit',
    'basedata:resource-capacity:batch-edit'
)
WHERE r.name = 'PLANNER'
  AND NOT EXISTS (
    SELECT 1
    FROM role_permissions rp
    WHERE rp.role_id = r.id
      AND rp.permission_id = p.id
  );

-- 6. 将设备日产能（仅查看）权限授予 SUPERVISOR 角色
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.code IN (
    'basedata',
    'basedata:resource-capacity',
    'basedata:resource-capacity:list'
)
WHERE r.name = 'SUPERVISOR'
  AND NOT EXISTS (
    SELECT 1
    FROM role_permissions rp
    WHERE rp.role_id = r.id
      AND rp.permission_id = p.id
  );
