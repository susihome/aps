-- 物料与模具权限迁移到基础数据模块

INSERT INTO permissions (id, code, name, description, type, icon, sort, enabled, visible, parent_id, create_time, update_time)
SELECT uuid_generate_v4(), 'basedata:material', '物料管理', '物料管理页面', 'MENU', 'Collection', 4, true, true,
       (SELECT id FROM permissions WHERE code = 'basedata'), NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE code = 'basedata:material'
);

INSERT INTO permissions (id, code, name, description, type, icon, sort, enabled, visible, parent_id, create_time, update_time)
SELECT uuid_generate_v4(), 'basedata:material:list', '查看物料', '查看物料列表', 'BUTTON', 'View', 1, true, true,
       (SELECT id FROM permissions WHERE code = 'basedata:material'), NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE code = 'basedata:material:list'
);

INSERT INTO permissions (id, code, name, description, type, icon, sort, enabled, visible, parent_id, create_time, update_time)
SELECT uuid_generate_v4(), 'basedata:material:query', '物料详情', '查看物料详情', 'BUTTON', 'View', 2, true, true,
       (SELECT id FROM permissions WHERE code = 'basedata:material'), NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE code = 'basedata:material:query'
);

INSERT INTO permissions (id, code, name, description, type, icon, sort, enabled, visible, parent_id, create_time, update_time)
SELECT uuid_generate_v4(), 'basedata:material:add', '新增物料', '新增物料', 'BUTTON', 'Plus', 3, true, true,
       (SELECT id FROM permissions WHERE code = 'basedata:material'), NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE code = 'basedata:material:add'
);

INSERT INTO permissions (id, code, name, description, type, icon, sort, enabled, visible, parent_id, create_time, update_time)
SELECT uuid_generate_v4(), 'basedata:material:edit', '编辑物料', '编辑物料', 'BUTTON', 'Edit', 4, true, true,
       (SELECT id FROM permissions WHERE code = 'basedata:material'), NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE code = 'basedata:material:edit'
);

INSERT INTO permissions (id, code, name, description, type, icon, sort, enabled, visible, parent_id, create_time, update_time)
SELECT uuid_generate_v4(), 'basedata:material:remove', '删除物料', '删除物料', 'BUTTON', 'Delete', 5, true, true,
       (SELECT id FROM permissions WHERE code = 'basedata:material'), NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE code = 'basedata:material:remove'
);

INSERT INTO permissions (id, code, name, description, type, icon, sort, enabled, visible, parent_id, create_time, update_time)
SELECT uuid_generate_v4(), 'basedata:mold', '模具管理', '模具管理页面', 'MENU', 'Collection', 5, true, true,
       (SELECT id FROM permissions WHERE code = 'basedata'), NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE code = 'basedata:mold'
);

INSERT INTO permissions (id, code, name, description, type, icon, sort, enabled, visible, parent_id, create_time, update_time)
SELECT uuid_generate_v4(), 'basedata:mold:list', '查看模具', '查看模具列表', 'BUTTON', 'View', 1, true, true,
       (SELECT id FROM permissions WHERE code = 'basedata:mold'), NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE code = 'basedata:mold:list'
);

INSERT INTO permissions (id, code, name, description, type, icon, sort, enabled, visible, parent_id, create_time, update_time)
SELECT uuid_generate_v4(), 'basedata:mold:query', '模具详情', '查看模具详情', 'BUTTON', 'View', 2, true, true,
       (SELECT id FROM permissions WHERE code = 'basedata:mold'), NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE code = 'basedata:mold:query'
);

INSERT INTO permissions (id, code, name, description, type, icon, sort, enabled, visible, parent_id, create_time, update_time)
SELECT uuid_generate_v4(), 'basedata:mold:add', '新增模具', '新增模具', 'BUTTON', 'Plus', 3, true, true,
       (SELECT id FROM permissions WHERE code = 'basedata:mold'), NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE code = 'basedata:mold:add'
);

INSERT INTO permissions (id, code, name, description, type, icon, sort, enabled, visible, parent_id, create_time, update_time)
SELECT uuid_generate_v4(), 'basedata:mold:edit', '编辑模具', '编辑模具', 'BUTTON', 'Edit', 4, true, true,
       (SELECT id FROM permissions WHERE code = 'basedata:mold'), NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE code = 'basedata:mold:edit'
);

INSERT INTO permissions (id, code, name, description, type, icon, sort, enabled, visible, parent_id, create_time, update_time)
SELECT uuid_generate_v4(), 'basedata:mold:remove', '删除模具', '删除模具', 'BUTTON', 'Delete', 5, true, true,
       (SELECT id FROM permissions WHERE code = 'basedata:mold'), NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE code = 'basedata:mold:remove'
);

-- ADMIN 拥有全部权限
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.code IN (
    'basedata:material',
    'basedata:material:list',
    'basedata:material:query',
    'basedata:material:add',
    'basedata:material:edit',
    'basedata:material:remove',
    'basedata:mold',
    'basedata:mold:list',
    'basedata:mold:query',
    'basedata:mold:add',
    'basedata:mold:edit',
    'basedata:mold:remove'
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
    'basedata:material',
    'basedata:material:list',
    'basedata:material:query',
    'basedata:mold',
    'basedata:mold:list',
    'basedata:mold:query'
)
WHERE r.name IN ('PLANNER', 'SUPERVISOR')
  AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
  );
