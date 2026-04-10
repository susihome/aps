INSERT INTO permissions (id, code, name, description, type, icon, sort, enabled, visible, parent_id, create_time, update_time)
SELECT uuid_generate_v4(), 'system:dict', '编码管理', '编码管理页面', 'MENU', 'Collection', 5, true, true,
       (SELECT id FROM permissions WHERE code = 'system'), NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE code = 'system:dict'
);

INSERT INTO permissions (id, code, name, description, type, icon, sort, enabled, visible, parent_id, create_time, update_time)
SELECT uuid_generate_v4(), 'system:dict:type:list', '字典类型列表', '查看字典类型列表', 'BUTTON', 'View', 1, true, true,
       (SELECT id FROM permissions WHERE code = 'system:dict'), NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE code = 'system:dict:type:list'
);

INSERT INTO permissions (id, code, name, description, type, icon, sort, enabled, visible, parent_id, create_time, update_time)
SELECT uuid_generate_v4(), 'system:dict:type:query', '字典类型详情', '查看字典类型详情', 'BUTTON', 'View', 2, true, true,
       (SELECT id FROM permissions WHERE code = 'system:dict'), NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE code = 'system:dict:type:query'
);

INSERT INTO permissions (id, code, name, description, type, icon, sort, enabled, visible, parent_id, create_time, update_time)
SELECT uuid_generate_v4(), 'system:dict:type:add', '新增字典类型', '新增字典类型', 'BUTTON', 'Plus', 3, true, true,
       (SELECT id FROM permissions WHERE code = 'system:dict'), NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE code = 'system:dict:type:add'
);

INSERT INTO permissions (id, code, name, description, type, icon, sort, enabled, visible, parent_id, create_time, update_time)
SELECT uuid_generate_v4(), 'system:dict:type:edit', '编辑字典类型', '编辑字典类型', 'BUTTON', 'Edit', 4, true, true,
       (SELECT id FROM permissions WHERE code = 'system:dict'), NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE code = 'system:dict:type:edit'
);

INSERT INTO permissions (id, code, name, description, type, icon, sort, enabled, visible, parent_id, create_time, update_time)
SELECT uuid_generate_v4(), 'system:dict:type:remove', '删除字典类型', '删除字典类型', 'BUTTON', 'Delete', 5, true, true,
       (SELECT id FROM permissions WHERE code = 'system:dict'), NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE code = 'system:dict:type:remove'
);

INSERT INTO permissions (id, code, name, description, type, icon, sort, enabled, visible, parent_id, create_time, update_time)
SELECT uuid_generate_v4(), 'system:dict:item:list', '字典项列表', '查看字典项列表', 'BUTTON', 'View', 6, true, true,
       (SELECT id FROM permissions WHERE code = 'system:dict'), NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE code = 'system:dict:item:list'
);

INSERT INTO permissions (id, code, name, description, type, icon, sort, enabled, visible, parent_id, create_time, update_time)
SELECT uuid_generate_v4(), 'system:dict:item:query', '字典项详情', '查看字典项详情', 'BUTTON', 'View', 7, true, true,
       (SELECT id FROM permissions WHERE code = 'system:dict'), NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE code = 'system:dict:item:query'
);

INSERT INTO permissions (id, code, name, description, type, icon, sort, enabled, visible, parent_id, create_time, update_time)
SELECT uuid_generate_v4(), 'system:dict:item:add', '新增字典项', '新增字典项', 'BUTTON', 'Plus', 8, true, true,
       (SELECT id FROM permissions WHERE code = 'system:dict'), NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE code = 'system:dict:item:add'
);

INSERT INTO permissions (id, code, name, description, type, icon, sort, enabled, visible, parent_id, create_time, update_time)
SELECT uuid_generate_v4(), 'system:dict:item:edit', '编辑字典项', '编辑字典项', 'BUTTON', 'Edit', 9, true, true,
       (SELECT id FROM permissions WHERE code = 'system:dict'), NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE code = 'system:dict:item:edit'
);

INSERT INTO permissions (id, code, name, description, type, icon, sort, enabled, visible, parent_id, create_time, update_time)
SELECT uuid_generate_v4(), 'system:dict:item:remove', '删除字典项', '删除字典项', 'BUTTON', 'Delete', 10, true, true,
       (SELECT id FROM permissions WHERE code = 'system:dict'), NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE code = 'system:dict:item:remove'
);

INSERT INTO permissions (id, code, name, description, type, icon, sort, enabled, visible, parent_id, create_time, update_time)
SELECT uuid_generate_v4(), 'system:dict:query', '字典通用查询', '按类型编码查询启用字典项', 'BUTTON', 'View', 11, true, true,
       (SELECT id FROM permissions WHERE code = 'system:dict'), NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE code = 'system:dict:query'
);
