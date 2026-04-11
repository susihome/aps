-- 模具状态字典类型及字典项种子数据

-- ===================== 字典类型 =====================

INSERT INTO sys_dict_type (id, code, name, description, enabled, sort_order, create_time, update_time)
SELECT uuid_generate_v4(), 'MOLD_STATUS', '模具状态', '模具使用状态分类编码', true, 10, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE code = 'MOLD_STATUS');

-- ===================== MOLD_STATUS 字典项 =====================

INSERT INTO sys_dict_item (id, dict_type_id, item_code, item_name, item_value, description, enabled, sort_order, is_system, create_time, update_time)
SELECT uuid_generate_v4(), dt.id, 'ACTIVE', '可用', 'success', '模具正常可用于生产', true, 1, true, NOW(), NOW()
FROM sys_dict_type dt WHERE dt.code = 'MOLD_STATUS'
AND NOT EXISTS (SELECT 1 FROM sys_dict_item di WHERE di.dict_type_id = dt.id AND di.item_code = 'ACTIVE');

INSERT INTO sys_dict_item (id, dict_type_id, item_code, item_name, item_value, description, enabled, sort_order, is_system, create_time, update_time)
SELECT uuid_generate_v4(), dt.id, 'MAINTENANCE', '维修', 'warning', '模具正在维修保养中', true, 2, true, NOW(), NOW()
FROM sys_dict_type dt WHERE dt.code = 'MOLD_STATUS'
AND NOT EXISTS (SELECT 1 FROM sys_dict_item di WHERE di.dict_type_id = dt.id AND di.item_code = 'MAINTENANCE');

INSERT INTO sys_dict_item (id, dict_type_id, item_code, item_name, item_value, description, enabled, sort_order, is_system, create_time, update_time)
SELECT uuid_generate_v4(), dt.id, 'DISABLED', '停用', 'danger', '模具已停用，不参与排产', true, 3, true, NOW(), NOW()
FROM sys_dict_type dt WHERE dt.code = 'MOLD_STATUS'
AND NOT EXISTS (SELECT 1 FROM sys_dict_item di WHERE di.dict_type_id = dt.id AND di.item_code = 'DISABLED');
