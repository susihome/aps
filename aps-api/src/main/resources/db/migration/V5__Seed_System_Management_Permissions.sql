INSERT INTO permissions (id, code, name, description, type, icon, sort, enabled, visible, parent_id, create_time, update_time)
SELECT uuid_generate_v4(), 'system', '系统管理', '系统管理模块', 'CATALOG', 'Setting', 1, true, true, NULL, NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE code = 'system'
);

INSERT INTO permissions (id, code, name, description, type, icon, sort, enabled, visible, parent_id, create_time, update_time)
SELECT uuid_generate_v4(), 'system:user', '用户管理', '用户管理页面', 'MENU', 'User', 1, true, true,
       (SELECT id FROM permissions WHERE code = 'system'), NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE code = 'system:user'
);

INSERT INTO permissions (id, code, name, description, type, icon, sort, enabled, visible, parent_id, create_time, update_time)
SELECT uuid_generate_v4(), 'system:user:list', '用户列表', '查看用户列表', 'BUTTON', 'View', 1, true, true,
       (SELECT id FROM permissions WHERE code = 'system:user'), NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE code = 'system:user:list'
);

INSERT INTO permissions (id, code, name, description, type, icon, sort, enabled, visible, parent_id, create_time, update_time)
SELECT uuid_generate_v4(), 'system:user:add', '新增用户', '新增用户', 'BUTTON', 'Plus', 2, true, true,
       (SELECT id FROM permissions WHERE code = 'system:user'), NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE code = 'system:user:add'
);

INSERT INTO permissions (id, code, name, description, type, icon, sort, enabled, visible, parent_id, create_time, update_time)
SELECT uuid_generate_v4(), 'system:user:edit', '编辑用户', '编辑用户', 'BUTTON', 'Edit', 3, true, true,
       (SELECT id FROM permissions WHERE code = 'system:user'), NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE code = 'system:user:edit'
);

INSERT INTO permissions (id, code, name, description, type, icon, sort, enabled, visible, parent_id, create_time, update_time)
SELECT uuid_generate_v4(), 'system:user:delete', '删除用户', '删除用户', 'BUTTON', 'Delete', 4, true, true,
       (SELECT id FROM permissions WHERE code = 'system:user'), NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE code = 'system:user:delete'
);

INSERT INTO permissions (id, code, name, description, type, icon, sort, enabled, visible, parent_id, create_time, update_time)
SELECT uuid_generate_v4(), 'system:user:reset_password', '重置密码', '重置用户密码', 'BUTTON', 'Key', 5, true, true,
       (SELECT id FROM permissions WHERE code = 'system:user'), NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE code = 'system:user:reset_password'
);

INSERT INTO permissions (id, code, name, description, type, icon, sort, enabled, visible, parent_id, create_time, update_time)
SELECT uuid_generate_v4(), 'system:permission', '权限配置', '权限管理页面', 'MENU', 'Lock', 2, true, true,
       (SELECT id FROM permissions WHERE code = 'system'), NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE code = 'system:permission'
);

INSERT INTO permissions (id, code, name, description, type, icon, sort, enabled, visible, parent_id, create_time, update_time)
SELECT uuid_generate_v4(), 'system:permission:list', '权限列表', '查看权限列表', 'BUTTON', 'View', 1, true, true,
       (SELECT id FROM permissions WHERE code = 'system:permission'), NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE code = 'system:permission:list'
);

INSERT INTO permissions (id, code, name, description, type, icon, sort, enabled, visible, parent_id, create_time, update_time)
SELECT uuid_generate_v4(), 'system:permission:query', '权限详情', '查看权限详情', 'BUTTON', 'View', 2, true, true,
       (SELECT id FROM permissions WHERE code = 'system:permission'), NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE code = 'system:permission:query'
);

INSERT INTO permissions (id, code, name, description, type, icon, sort, enabled, visible, parent_id, create_time, update_time)
SELECT uuid_generate_v4(), 'system:permission:add', '新增权限', '新增权限', 'BUTTON', 'Plus', 3, true, true,
       (SELECT id FROM permissions WHERE code = 'system:permission'), NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE code = 'system:permission:add'
);

INSERT INTO permissions (id, code, name, description, type, icon, sort, enabled, visible, parent_id, create_time, update_time)
SELECT uuid_generate_v4(), 'system:permission:edit', '编辑权限', '编辑权限', 'BUTTON', 'Edit', 4, true, true,
       (SELECT id FROM permissions WHERE code = 'system:permission'), NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE code = 'system:permission:edit'
);

INSERT INTO permissions (id, code, name, description, type, icon, sort, enabled, visible, parent_id, create_time, update_time)
SELECT uuid_generate_v4(), 'system:permission:remove', '删除权限', '删除权限', 'BUTTON', 'Delete', 5, true, true,
       (SELECT id FROM permissions WHERE code = 'system:permission'), NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE code = 'system:permission:remove'
);
