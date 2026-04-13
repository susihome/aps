-- 物料模具绑定权限种子数据

-- MENU
INSERT INTO permissions (id, code, name, description, type, icon, sort, enabled, visible, parent_id, create_time, update_time)
SELECT uuid_generate_v4(), 'basedata:materialmold', '物料模具绑定', '物料模具绑定管理页面', 'MENU', 'Connection', 6, true, true,
       (SELECT id FROM permissions WHERE code = 'basedata'), NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE code = 'basedata:materialmold'
);

-- BUTTON: list
INSERT INTO permissions (id, code, name, description, type, icon, sort, enabled, visible, parent_id, create_time, update_time)
SELECT uuid_generate_v4(), 'basedata:materialmold:list', '查看绑定', '查看物料模具绑定列表', 'BUTTON', 'View', 1, true, true,
       (SELECT id FROM permissions WHERE code = 'basedata:materialmold'), NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE code = 'basedata:materialmold:list'
);

-- BUTTON: query
INSERT INTO permissions (id, code, name, description, type, icon, sort, enabled, visible, parent_id, create_time, update_time)
SELECT uuid_generate_v4(), 'basedata:materialmold:query', '绑定详情', '查看物料模具绑定详情', 'BUTTON', 'View', 2, true, true,
       (SELECT id FROM permissions WHERE code = 'basedata:materialmold'), NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE code = 'basedata:materialmold:query'
);

-- BUTTON: add
INSERT INTO permissions (id, code, name, description, type, icon, sort, enabled, visible, parent_id, create_time, update_time)
SELECT uuid_generate_v4(), 'basedata:materialmold:add', '新增绑定', '新增物料模具绑定', 'BUTTON', 'Plus', 3, true, true,
       (SELECT id FROM permissions WHERE code = 'basedata:materialmold'), NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE code = 'basedata:materialmold:add'
);

-- BUTTON: edit
INSERT INTO permissions (id, code, name, description, type, icon, sort, enabled, visible, parent_id, create_time, update_time)
SELECT uuid_generate_v4(), 'basedata:materialmold:edit', '编辑绑定', '编辑物料模具绑定', 'BUTTON', 'Edit', 4, true, true,
       (SELECT id FROM permissions WHERE code = 'basedata:materialmold'), NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE code = 'basedata:materialmold:edit'
);

-- BUTTON: remove
INSERT INTO permissions (id, code, name, description, type, icon, sort, enabled, visible, parent_id, create_time, update_time)
SELECT uuid_generate_v4(), 'basedata:materialmold:remove', '删除绑定', '删除物料模具绑定', 'BUTTON', 'Delete', 5, true, true,
       (SELECT id FROM permissions WHERE code = 'basedata:materialmold'), NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE code = 'basedata:materialmold:remove'
);

-- ADMIN 拥有全部权限
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.code IN (
    'basedata:materialmold',
    'basedata:materialmold:list',
    'basedata:materialmold:query',
    'basedata:materialmold:add',
    'basedata:materialmold:edit',
    'basedata:materialmold:remove'
)
WHERE r.name = 'ADMIN'
  AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
  );

-- PLANNER / SUPERVISOR 仅查看
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.code IN (
    'basedata:materialmold',
    'basedata:materialmold:list',
    'basedata:materialmold:query'
)
WHERE r.name IN ('PLANNER', 'SUPERVISOR')
  AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
  );
