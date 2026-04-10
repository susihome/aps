-- 补充枚举编码管理预置数据：角色类型、日期类型、审计操作

-- ===================== 字典类型 =====================

INSERT INTO sys_dict_type (id, code, name, description, enabled, sort_order, create_time, update_time)
SELECT uuid_generate_v4(), 'ROLE_TYPE', '角色类型', '系统角色分类编码', true, 4, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE code = 'ROLE_TYPE');

INSERT INTO sys_dict_type (id, code, name, description, enabled, sort_order, create_time, update_time)
SELECT uuid_generate_v4(), 'DATE_TYPE', '日期类型', '工厂日历日期分类编码', true, 5, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE code = 'DATE_TYPE');

INSERT INTO sys_dict_type (id, code, name, description, enabled, sort_order, create_time, update_time)
SELECT uuid_generate_v4(), 'AUDIT_ACTION', '审计操作类型', '审计日志操作行为编码', true, 6, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE code = 'AUDIT_ACTION');

-- ===================== ROLE_TYPE 字典项 =====================

INSERT INTO sys_dict_item (id, dict_type_id, item_code, item_name, item_value, description, enabled, sort_order, is_system, create_time, update_time)
SELECT uuid_generate_v4(), dt.id, 'ADMIN', '管理员', 'ADMIN', '系统管理员，拥有全部访问权限', true, 1, true, NOW(), NOW()
FROM sys_dict_type dt WHERE dt.code = 'ROLE_TYPE'
AND NOT EXISTS (SELECT 1 FROM sys_dict_item di WHERE di.dict_type_id = dt.id AND di.item_code = 'ADMIN');

INSERT INTO sys_dict_item (id, dict_type_id, item_code, item_name, item_value, description, enabled, sort_order, is_system, create_time, update_time)
SELECT uuid_generate_v4(), dt.id, 'PLANNER', '计划员', 'PLANNER', '负责创建和修改排产计划', true, 2, true, NOW(), NOW()
FROM sys_dict_type dt WHERE dt.code = 'ROLE_TYPE'
AND NOT EXISTS (SELECT 1 FROM sys_dict_item di WHERE di.dict_type_id = dt.id AND di.item_code = 'PLANNER');

INSERT INTO sys_dict_item (id, dict_type_id, item_code, item_name, item_value, description, enabled, sort_order, is_system, create_time, update_time)
SELECT uuid_generate_v4(), dt.id, 'SUPERVISOR', '监督员', 'SUPERVISOR', '负责查看和监控排产执行情况', true, 3, true, NOW(), NOW()
FROM sys_dict_type dt WHERE dt.code = 'ROLE_TYPE'
AND NOT EXISTS (SELECT 1 FROM sys_dict_item di WHERE di.dict_type_id = dt.id AND di.item_code = 'SUPERVISOR');

-- ===================== DATE_TYPE 字典项 =====================

INSERT INTO sys_dict_item (id, dict_type_id, item_code, item_name, item_value, description, enabled, sort_order, is_system, create_time, update_time)
SELECT uuid_generate_v4(), dt.id, 'WORKDAY', '工作日', 'WORKDAY', '正常生产排班工作日', true, 1, true, NOW(), NOW()
FROM sys_dict_type dt WHERE dt.code = 'DATE_TYPE'
AND NOT EXISTS (SELECT 1 FROM sys_dict_item di WHERE di.dict_type_id = dt.id AND di.item_code = 'WORKDAY');

INSERT INTO sys_dict_item (id, dict_type_id, item_code, item_name, item_value, description, enabled, sort_order, is_system, create_time, update_time)
SELECT uuid_generate_v4(), dt.id, 'RESTDAY', '休息日', 'RESTDAY', '周末或调休休息日', true, 2, true, NOW(), NOW()
FROM sys_dict_type dt WHERE dt.code = 'DATE_TYPE'
AND NOT EXISTS (SELECT 1 FROM sys_dict_item di WHERE di.dict_type_id = dt.id AND di.item_code = 'RESTDAY');

INSERT INTO sys_dict_item (id, dict_type_id, item_code, item_name, item_value, description, enabled, sort_order, is_system, create_time, update_time)
SELECT uuid_generate_v4(), dt.id, 'HOLIDAY', '节假日', 'HOLIDAY', '法定节假日，不安排生产', true, 3, true, NOW(), NOW()
FROM sys_dict_type dt WHERE dt.code = 'DATE_TYPE'
AND NOT EXISTS (SELECT 1 FROM sys_dict_item di WHERE di.dict_type_id = dt.id AND di.item_code = 'HOLIDAY');

-- ===================== AUDIT_ACTION 字典项 =====================

-- 认证相关
INSERT INTO sys_dict_item (id, dict_type_id, item_code, item_name, item_value, description, enabled, sort_order, is_system, create_time, update_time)
SELECT uuid_generate_v4(), dt.id, 'LOGIN', '登录', 'LOGIN', '用户登录系统', true, 1, true, NOW(), NOW()
FROM sys_dict_type dt WHERE dt.code = 'AUDIT_ACTION'
AND NOT EXISTS (SELECT 1 FROM sys_dict_item di WHERE di.dict_type_id = dt.id AND di.item_code = 'LOGIN');

INSERT INTO sys_dict_item (id, dict_type_id, item_code, item_name, item_value, description, enabled, sort_order, is_system, create_time, update_time)
SELECT uuid_generate_v4(), dt.id, 'LOGOUT', '登出', 'LOGOUT', '用户退出系统', true, 2, true, NOW(), NOW()
FROM sys_dict_type dt WHERE dt.code = 'AUDIT_ACTION'
AND NOT EXISTS (SELECT 1 FROM sys_dict_item di WHERE di.dict_type_id = dt.id AND di.item_code = 'LOGOUT');

INSERT INTO sys_dict_item (id, dict_type_id, item_code, item_name, item_value, description, enabled, sort_order, is_system, create_time, update_time)
SELECT uuid_generate_v4(), dt.id, 'ACCESS_DENIED', '访问拒绝', 'ACCESS_DENIED', '用户访问被拒绝', true, 3, true, NOW(), NOW()
FROM sys_dict_type dt WHERE dt.code = 'AUDIT_ACTION'
AND NOT EXISTS (SELECT 1 FROM sys_dict_item di WHERE di.dict_type_id = dt.id AND di.item_code = 'ACCESS_DENIED');

-- 排产相关
INSERT INTO sys_dict_item (id, dict_type_id, item_code, item_name, item_value, description, enabled, sort_order, is_system, create_time, update_time)
SELECT uuid_generate_v4(), dt.id, 'SCHEDULE_CREATE', '创建排产', 'SCHEDULE_CREATE', '创建排产方案', true, 10, true, NOW(), NOW()
FROM sys_dict_type dt WHERE dt.code = 'AUDIT_ACTION'
AND NOT EXISTS (SELECT 1 FROM sys_dict_item di WHERE di.dict_type_id = dt.id AND di.item_code = 'SCHEDULE_CREATE');

INSERT INTO sys_dict_item (id, dict_type_id, item_code, item_name, item_value, description, enabled, sort_order, is_system, create_time, update_time)
SELECT uuid_generate_v4(), dt.id, 'SCHEDULE_UPDATE', '修改排产', 'SCHEDULE_UPDATE', '修改排产方案', true, 11, true, NOW(), NOW()
FROM sys_dict_type dt WHERE dt.code = 'AUDIT_ACTION'
AND NOT EXISTS (SELECT 1 FROM sys_dict_item di WHERE di.dict_type_id = dt.id AND di.item_code = 'SCHEDULE_UPDATE');

INSERT INTO sys_dict_item (id, dict_type_id, item_code, item_name, item_value, description, enabled, sort_order, is_system, create_time, update_time)
SELECT uuid_generate_v4(), dt.id, 'SCHEDULE_DELETE', '删除排产', 'SCHEDULE_DELETE', '删除排产方案', true, 12, true, NOW(), NOW()
FROM sys_dict_type dt WHERE dt.code = 'AUDIT_ACTION'
AND NOT EXISTS (SELECT 1 FROM sys_dict_item di WHERE di.dict_type_id = dt.id AND di.item_code = 'SCHEDULE_DELETE');

INSERT INTO sys_dict_item (id, dict_type_id, item_code, item_name, item_value, description, enabled, sort_order, is_system, create_time, update_time)
SELECT uuid_generate_v4(), dt.id, 'SCHEDULE_SOLVE', '启动求解', 'SCHEDULE_SOLVE', '启动排产求解引擎', true, 13, true, NOW(), NOW()
FROM sys_dict_type dt WHERE dt.code = 'AUDIT_ACTION'
AND NOT EXISTS (SELECT 1 FROM sys_dict_item di WHERE di.dict_type_id = dt.id AND di.item_code = 'SCHEDULE_SOLVE');

INSERT INTO sys_dict_item (id, dict_type_id, item_code, item_name, item_value, description, enabled, sort_order, is_system, create_time, update_time)
SELECT uuid_generate_v4(), dt.id, 'SCHEDULE_STOP', '停止求解', 'SCHEDULE_STOP', '停止排产求解引擎', true, 14, true, NOW(), NOW()
FROM sys_dict_type dt WHERE dt.code = 'AUDIT_ACTION'
AND NOT EXISTS (SELECT 1 FROM sys_dict_item di WHERE di.dict_type_id = dt.id AND di.item_code = 'SCHEDULE_STOP');

INSERT INTO sys_dict_item (id, dict_type_id, item_code, item_name, item_value, description, enabled, sort_order, is_system, create_time, update_time)
SELECT uuid_generate_v4(), dt.id, 'SCHEDULE_PUBLISH', '发布排产', 'SCHEDULE_PUBLISH', '发布排产方案', true, 15, true, NOW(), NOW()
FROM sys_dict_type dt WHERE dt.code = 'AUDIT_ACTION'
AND NOT EXISTS (SELECT 1 FROM sys_dict_item di WHERE di.dict_type_id = dt.id AND di.item_code = 'SCHEDULE_PUBLISH');

-- 工单相关
INSERT INTO sys_dict_item (id, dict_type_id, item_code, item_name, item_value, description, enabled, sort_order, is_system, create_time, update_time)
SELECT uuid_generate_v4(), dt.id, 'ORDER_CREATE', '创建工单', 'ORDER_CREATE', '创建生产工单', true, 20, true, NOW(), NOW()
FROM sys_dict_type dt WHERE dt.code = 'AUDIT_ACTION'
AND NOT EXISTS (SELECT 1 FROM sys_dict_item di WHERE di.dict_type_id = dt.id AND di.item_code = 'ORDER_CREATE');

INSERT INTO sys_dict_item (id, dict_type_id, item_code, item_name, item_value, description, enabled, sort_order, is_system, create_time, update_time)
SELECT uuid_generate_v4(), dt.id, 'ORDER_UPDATE', '修改工单', 'ORDER_UPDATE', '修改生产工单', true, 21, true, NOW(), NOW()
FROM sys_dict_type dt WHERE dt.code = 'AUDIT_ACTION'
AND NOT EXISTS (SELECT 1 FROM sys_dict_item di WHERE di.dict_type_id = dt.id AND di.item_code = 'ORDER_UPDATE');

INSERT INTO sys_dict_item (id, dict_type_id, item_code, item_name, item_value, description, enabled, sort_order, is_system, create_time, update_time)
SELECT uuid_generate_v4(), dt.id, 'ORDER_DELETE', '删除工单', 'ORDER_DELETE', '删除生产工单', true, 22, true, NOW(), NOW()
FROM sys_dict_type dt WHERE dt.code = 'AUDIT_ACTION'
AND NOT EXISTS (SELECT 1 FROM sys_dict_item di WHERE di.dict_type_id = dt.id AND di.item_code = 'ORDER_DELETE');

INSERT INTO sys_dict_item (id, dict_type_id, item_code, item_name, item_value, description, enabled, sort_order, is_system, create_time, update_time)
SELECT uuid_generate_v4(), dt.id, 'ORDER_IMPORT', '导入工单', 'ORDER_IMPORT', '批量导入生产工单', true, 23, true, NOW(), NOW()
FROM sys_dict_type dt WHERE dt.code = 'AUDIT_ACTION'
AND NOT EXISTS (SELECT 1 FROM sys_dict_item di WHERE di.dict_type_id = dt.id AND di.item_code = 'ORDER_IMPORT');

-- 资源相关
INSERT INTO sys_dict_item (id, dict_type_id, item_code, item_name, item_value, description, enabled, sort_order, is_system, create_time, update_time)
SELECT uuid_generate_v4(), dt.id, 'RESOURCE_CREATE', '创建资源', 'RESOURCE_CREATE', '创建生产资源', true, 30, true, NOW(), NOW()
FROM sys_dict_type dt WHERE dt.code = 'AUDIT_ACTION'
AND NOT EXISTS (SELECT 1 FROM sys_dict_item di WHERE di.dict_type_id = dt.id AND di.item_code = 'RESOURCE_CREATE');

INSERT INTO sys_dict_item (id, dict_type_id, item_code, item_name, item_value, description, enabled, sort_order, is_system, create_time, update_time)
SELECT uuid_generate_v4(), dt.id, 'RESOURCE_UPDATE', '修改资源', 'RESOURCE_UPDATE', '修改生产资源', true, 31, true, NOW(), NOW()
FROM sys_dict_type dt WHERE dt.code = 'AUDIT_ACTION'
AND NOT EXISTS (SELECT 1 FROM sys_dict_item di WHERE di.dict_type_id = dt.id AND di.item_code = 'RESOURCE_UPDATE');

INSERT INTO sys_dict_item (id, dict_type_id, item_code, item_name, item_value, description, enabled, sort_order, is_system, create_time, update_time)
SELECT uuid_generate_v4(), dt.id, 'RESOURCE_DELETE', '删除资源', 'RESOURCE_DELETE', '删除生产资源', true, 32, true, NOW(), NOW()
FROM sys_dict_type dt WHERE dt.code = 'AUDIT_ACTION'
AND NOT EXISTS (SELECT 1 FROM sys_dict_item di WHERE di.dict_type_id = dt.id AND di.item_code = 'RESOURCE_DELETE');

-- 用户权限相关
INSERT INTO sys_dict_item (id, dict_type_id, item_code, item_name, item_value, description, enabled, sort_order, is_system, create_time, update_time)
SELECT uuid_generate_v4(), dt.id, 'USER_CREATE', '创建用户', 'USER_CREATE', '创建系统用户', true, 40, true, NOW(), NOW()
FROM sys_dict_type dt WHERE dt.code = 'AUDIT_ACTION'
AND NOT EXISTS (SELECT 1 FROM sys_dict_item di WHERE di.dict_type_id = dt.id AND di.item_code = 'USER_CREATE');

INSERT INTO sys_dict_item (id, dict_type_id, item_code, item_name, item_value, description, enabled, sort_order, is_system, create_time, update_time)
SELECT uuid_generate_v4(), dt.id, 'USER_UPDATE', '修改用户', 'USER_UPDATE', '修改系统用户信息', true, 41, true, NOW(), NOW()
FROM sys_dict_type dt WHERE dt.code = 'AUDIT_ACTION'
AND NOT EXISTS (SELECT 1 FROM sys_dict_item di WHERE di.dict_type_id = dt.id AND di.item_code = 'USER_UPDATE');

INSERT INTO sys_dict_item (id, dict_type_id, item_code, item_name, item_value, description, enabled, sort_order, is_system, create_time, update_time)
SELECT uuid_generate_v4(), dt.id, 'USER_DELETE', '删除用户', 'USER_DELETE', '删除系统用户', true, 42, true, NOW(), NOW()
FROM sys_dict_type dt WHERE dt.code = 'AUDIT_ACTION'
AND NOT EXISTS (SELECT 1 FROM sys_dict_item di WHERE di.dict_type_id = dt.id AND di.item_code = 'USER_DELETE');

INSERT INTO sys_dict_item (id, dict_type_id, item_code, item_name, item_value, description, enabled, sort_order, is_system, create_time, update_time)
SELECT uuid_generate_v4(), dt.id, 'USER_DISABLE', '禁用用户', 'USER_DISABLE', '禁用系统用户账号', true, 43, true, NOW(), NOW()
FROM sys_dict_type dt WHERE dt.code = 'AUDIT_ACTION'
AND NOT EXISTS (SELECT 1 FROM sys_dict_item di WHERE di.dict_type_id = dt.id AND di.item_code = 'USER_DISABLE');

INSERT INTO sys_dict_item (id, dict_type_id, item_code, item_name, item_value, description, enabled, sort_order, is_system, create_time, update_time)
SELECT uuid_generate_v4(), dt.id, 'USER_ENABLE', '启用用户', 'USER_ENABLE', '启用系统用户账号', true, 44, true, NOW(), NOW()
FROM sys_dict_type dt WHERE dt.code = 'AUDIT_ACTION'
AND NOT EXISTS (SELECT 1 FROM sys_dict_item di WHERE di.dict_type_id = dt.id AND di.item_code = 'USER_ENABLE');

INSERT INTO sys_dict_item (id, dict_type_id, item_code, item_name, item_value, description, enabled, sort_order, is_system, create_time, update_time)
SELECT uuid_generate_v4(), dt.id, 'ROLE_ASSIGN', '分配角色', 'ROLE_ASSIGN', '为用户分配角色', true, 45, true, NOW(), NOW()
FROM sys_dict_type dt WHERE dt.code = 'AUDIT_ACTION'
AND NOT EXISTS (SELECT 1 FROM sys_dict_item di WHERE di.dict_type_id = dt.id AND di.item_code = 'ROLE_ASSIGN');

INSERT INTO sys_dict_item (id, dict_type_id, item_code, item_name, item_value, description, enabled, sort_order, is_system, create_time, update_time)
SELECT uuid_generate_v4(), dt.id, 'ROLE_REMOVE', '移除角色', 'ROLE_REMOVE', '移除用户的角色', true, 46, true, NOW(), NOW()
FROM sys_dict_type dt WHERE dt.code = 'AUDIT_ACTION'
AND NOT EXISTS (SELECT 1 FROM sys_dict_item di WHERE di.dict_type_id = dt.id AND di.item_code = 'ROLE_REMOVE');

INSERT INTO sys_dict_item (id, dict_type_id, item_code, item_name, item_value, description, enabled, sort_order, is_system, create_time, update_time)
SELECT uuid_generate_v4(), dt.id, 'PERMISSION_GRANT', '授予权限', 'PERMISSION_GRANT', '为角色授予权限', true, 47, true, NOW(), NOW()
FROM sys_dict_type dt WHERE dt.code = 'AUDIT_ACTION'
AND NOT EXISTS (SELECT 1 FROM sys_dict_item di WHERE di.dict_type_id = dt.id AND di.item_code = 'PERMISSION_GRANT');

INSERT INTO sys_dict_item (id, dict_type_id, item_code, item_name, item_value, description, enabled, sort_order, is_system, create_time, update_time)
SELECT uuid_generate_v4(), dt.id, 'PERMISSION_REVOKE', '撤销权限', 'PERMISSION_REVOKE', '撤销角色的权限', true, 48, true, NOW(), NOW()
FROM sys_dict_type dt WHERE dt.code = 'AUDIT_ACTION'
AND NOT EXISTS (SELECT 1 FROM sys_dict_item di WHERE di.dict_type_id = dt.id AND di.item_code = 'PERMISSION_REVOKE');

-- 系统配置
INSERT INTO sys_dict_item (id, dict_type_id, item_code, item_name, item_value, description, enabled, sort_order, is_system, create_time, update_time)
SELECT uuid_generate_v4(), dt.id, 'CONFIG_UPDATE', '修改配置', 'CONFIG_UPDATE', '修改系统配置', true, 50, true, NOW(), NOW()
FROM sys_dict_type dt WHERE dt.code = 'AUDIT_ACTION'
AND NOT EXISTS (SELECT 1 FROM sys_dict_item di WHERE di.dict_type_id = dt.id AND di.item_code = 'CONFIG_UPDATE');

-- 通用操作（兼容旧数据）
INSERT INTO sys_dict_item (id, dict_type_id, item_code, item_name, item_value, description, enabled, sort_order, is_system, create_time, update_time)
SELECT uuid_generate_v4(), dt.id, 'CREATE', '创建', 'CREATE', '通用创建操作', true, 90, true, NOW(), NOW()
FROM sys_dict_type dt WHERE dt.code = 'AUDIT_ACTION'
AND NOT EXISTS (SELECT 1 FROM sys_dict_item di WHERE di.dict_type_id = dt.id AND di.item_code = 'CREATE');

INSERT INTO sys_dict_item (id, dict_type_id, item_code, item_name, item_value, description, enabled, sort_order, is_system, create_time, update_time)
SELECT uuid_generate_v4(), dt.id, 'UPDATE', '更新', 'UPDATE', '通用更新操作', true, 91, true, NOW(), NOW()
FROM sys_dict_type dt WHERE dt.code = 'AUDIT_ACTION'
AND NOT EXISTS (SELECT 1 FROM sys_dict_item di WHERE di.dict_type_id = dt.id AND di.item_code = 'UPDATE');

INSERT INTO sys_dict_item (id, dict_type_id, item_code, item_name, item_value, description, enabled, sort_order, is_system, create_time, update_time)
SELECT uuid_generate_v4(), dt.id, 'DELETE', '删除', 'DELETE', '通用删除操作', true, 92, true, NOW(), NOW()
FROM sys_dict_type dt WHERE dt.code = 'AUDIT_ACTION'
AND NOT EXISTS (SELECT 1 FROM sys_dict_item di WHERE di.dict_type_id = dt.id AND di.item_code = 'DELETE');
