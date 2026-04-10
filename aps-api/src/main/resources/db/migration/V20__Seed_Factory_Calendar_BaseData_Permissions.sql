-- 工厂日历权限迁移到基础数据模块

INSERT INTO permissions (id, code, name, description, type, icon, sort, enabled, visible, parent_id, create_time, update_time)
SELECT uuid_generate_v4(), 'basedata:factory-calendar', '工厂日历', '工厂日历页面', 'MENU', 'Calendar', 3, true, true,
       (SELECT id FROM permissions WHERE code = 'basedata'), NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE code = 'basedata:factory-calendar'
);

INSERT INTO permissions (id, code, name, description, type, icon, sort, enabled, visible, parent_id, create_time, update_time)
SELECT uuid_generate_v4(), 'basedata:factory-calendar:list', '查看工厂日历', '查看工厂日历列表', 'BUTTON', 'View', 1, true, true,
       (SELECT id FROM permissions WHERE code = 'basedata:factory-calendar'), NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE code = 'basedata:factory-calendar:list'
);

INSERT INTO permissions (id, code, name, description, type, icon, sort, enabled, visible, parent_id, create_time, update_time)
SELECT uuid_generate_v4(), 'basedata:factory-calendar:query', '工厂日历详情', '查看工厂日历详情与班次日期', 'BUTTON', 'View', 2, true, true,
       (SELECT id FROM permissions WHERE code = 'basedata:factory-calendar'), NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE code = 'basedata:factory-calendar:query'
);

INSERT INTO permissions (id, code, name, description, type, icon, sort, enabled, visible, parent_id, create_time, update_time)
SELECT uuid_generate_v4(), 'basedata:factory-calendar:add', '新增工厂日历', '新增工厂日历', 'BUTTON', 'Plus', 3, true, true,
       (SELECT id FROM permissions WHERE code = 'basedata:factory-calendar'), NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE code = 'basedata:factory-calendar:add'
);

INSERT INTO permissions (id, code, name, description, type, icon, sort, enabled, visible, parent_id, create_time, update_time)
SELECT uuid_generate_v4(), 'basedata:factory-calendar:edit', '编辑工厂日历', '编辑工厂日历与班次日期', 'BUTTON', 'Edit', 4, true, true,
       (SELECT id FROM permissions WHERE code = 'basedata:factory-calendar'), NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE code = 'basedata:factory-calendar:edit'
);

INSERT INTO permissions (id, code, name, description, type, icon, sort, enabled, visible, parent_id, create_time, update_time)
SELECT uuid_generate_v4(), 'basedata:factory-calendar:remove', '删除工厂日历', '删除工厂日历与班次', 'BUTTON', 'Delete', 5, true, true,
       (SELECT id FROM permissions WHERE code = 'basedata:factory-calendar'), NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE code = 'basedata:factory-calendar:remove'
);

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.code IN (
    'basedata:factory-calendar',
    'basedata:factory-calendar:list',
    'basedata:factory-calendar:query',
    'basedata:factory-calendar:add',
    'basedata:factory-calendar:edit',
    'basedata:factory-calendar:remove'
)
WHERE r.name = 'ADMIN'
  AND NOT EXISTS (
    SELECT 1
    FROM role_permissions rp
    WHERE rp.role_id = r.id
      AND rp.permission_id = p.id
  );
