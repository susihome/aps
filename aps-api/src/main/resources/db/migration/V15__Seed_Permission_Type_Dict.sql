-- 补充权限类型编码管理预置数据

INSERT INTO sys_dict_type (id, code, name, description, enabled, sort_order, create_time, update_time)
SELECT uuid_generate_v4(), 'PERMISSION_TYPE', '权限类型', '权限节点类型编码（目录/菜单/按钮）', true, 7, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE code = 'PERMISSION_TYPE');

INSERT INTO sys_dict_item (id, dict_type_id, item_code, item_name, item_value, description, enabled, sort_order, is_system, create_time, update_time)
SELECT uuid_generate_v4(), dt.id, 'CATALOG', '目录', 'CATALOG', '导航目录节点，用于分组菜单', true, 1, true, NOW(), NOW()
FROM sys_dict_type dt WHERE dt.code = 'PERMISSION_TYPE'
AND NOT EXISTS (SELECT 1 FROM sys_dict_item di WHERE di.dict_type_id = dt.id AND di.item_code = 'CATALOG');

INSERT INTO sys_dict_item (id, dict_type_id, item_code, item_name, item_value, description, enabled, sort_order, is_system, create_time, update_time)
SELECT uuid_generate_v4(), dt.id, 'MENU', '菜单', 'MENU', '页面菜单节点，对应前端路由', true, 2, true, NOW(), NOW()
FROM sys_dict_type dt WHERE dt.code = 'PERMISSION_TYPE'
AND NOT EXISTS (SELECT 1 FROM sys_dict_item di WHERE di.dict_type_id = dt.id AND di.item_code = 'MENU');

INSERT INTO sys_dict_item (id, dict_type_id, item_code, item_name, item_value, description, enabled, sort_order, is_system, create_time, update_time)
SELECT uuid_generate_v4(), dt.id, 'BUTTON', '按钮', 'BUTTON', '操作按钮节点，控制功能级权限', true, 3, true, NOW(), NOW()
FROM sys_dict_type dt WHERE dt.code = 'PERMISSION_TYPE'
AND NOT EXISTS (SELECT 1 FROM sys_dict_item di WHERE di.dict_type_id = dt.id AND di.item_code = 'BUTTON');
