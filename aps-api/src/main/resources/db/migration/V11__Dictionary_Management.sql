-- 编码管理：字典类型 + 字典项

CREATE TABLE IF NOT EXISTS sys_dict_type (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    code VARCHAR(64) NOT NULL,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order INTEGER NOT NULL DEFAULT 0,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS sys_dict_item (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    dict_type_id UUID NOT NULL REFERENCES sys_dict_type(id) ON DELETE RESTRICT,
    item_code VARCHAR(64) NOT NULL,
    item_name VARCHAR(100) NOT NULL,
    item_value VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order INTEGER NOT NULL DEFAULT 0,
    is_system BOOLEAN NOT NULL DEFAULT FALSE,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_dict_type_code ON sys_dict_type(code);
CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_dict_item_type_code ON sys_dict_item(dict_type_id, item_code);
CREATE INDEX IF NOT EXISTS idx_sys_dict_type_enabled_sort ON sys_dict_type(enabled, sort_order);
CREATE INDEX IF NOT EXISTS idx_sys_dict_item_type_enabled_sort ON sys_dict_item(dict_type_id, enabled, sort_order);

-- 预置字典类型
INSERT INTO sys_dict_type (id, code, name, description, enabled, sort_order, create_time, update_time)
SELECT uuid_generate_v4(), 'ORDER_STATUS', '订单状态', '订单处理状态编码', true, 1, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE code = 'ORDER_STATUS');

INSERT INTO sys_dict_type (id, code, name, description, enabled, sort_order, create_time, update_time)
SELECT uuid_generate_v4(), 'PRIORITY', '优先级', '生产优先级编码', true, 2, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE code = 'PRIORITY');

INSERT INTO sys_dict_type (id, code, name, description, enabled, sort_order, create_time, update_time)
SELECT uuid_generate_v4(), 'MACHINE_STATUS', '设备状态', '设备运行状态编码', true, 3, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE code = 'MACHINE_STATUS');

-- 预置订单状态字典项
INSERT INTO sys_dict_item (id, dict_type_id, item_code, item_name, item_value, description, enabled, sort_order, is_system, create_time, update_time)
SELECT uuid_generate_v4(), dt.id, 'PENDING', '待排产', 'PENDING', '订单已创建，尚未排产', true, 1, true, NOW(), NOW()
FROM sys_dict_type dt
WHERE dt.code = 'ORDER_STATUS'
  AND NOT EXISTS (
      SELECT 1 FROM sys_dict_item di WHERE di.dict_type_id = dt.id AND di.item_code = 'PENDING'
  );

INSERT INTO sys_dict_item (id, dict_type_id, item_code, item_name, item_value, description, enabled, sort_order, is_system, create_time, update_time)
SELECT uuid_generate_v4(), dt.id, 'SCHEDULED', '已排产', 'SCHEDULED', '订单已完成排产', true, 2, true, NOW(), NOW()
FROM sys_dict_type dt
WHERE dt.code = 'ORDER_STATUS'
  AND NOT EXISTS (
      SELECT 1 FROM sys_dict_item di WHERE di.dict_type_id = dt.id AND di.item_code = 'SCHEDULED'
  );

INSERT INTO sys_dict_item (id, dict_type_id, item_code, item_name, item_value, description, enabled, sort_order, is_system, create_time, update_time)
SELECT uuid_generate_v4(), dt.id, 'IN_PROGRESS', '生产中', 'IN_PROGRESS', '订单正在生产执行', true, 3, true, NOW(), NOW()
FROM sys_dict_type dt
WHERE dt.code = 'ORDER_STATUS'
  AND NOT EXISTS (
      SELECT 1 FROM sys_dict_item di WHERE di.dict_type_id = dt.id AND di.item_code = 'IN_PROGRESS'
  );

INSERT INTO sys_dict_item (id, dict_type_id, item_code, item_name, item_value, description, enabled, sort_order, is_system, create_time, update_time)
SELECT uuid_generate_v4(), dt.id, 'COMPLETED', '已完成', 'COMPLETED', '订单生产已完成', true, 4, true, NOW(), NOW()
FROM sys_dict_type dt
WHERE dt.code = 'ORDER_STATUS'
  AND NOT EXISTS (
      SELECT 1 FROM sys_dict_item di WHERE di.dict_type_id = dt.id AND di.item_code = 'COMPLETED'
  );

INSERT INTO sys_dict_item (id, dict_type_id, item_code, item_name, item_value, description, enabled, sort_order, is_system, create_time, update_time)
SELECT uuid_generate_v4(), dt.id, 'CANCELLED', '已取消', 'CANCELLED', '订单已取消', true, 5, true, NOW(), NOW()
FROM sys_dict_type dt
WHERE dt.code = 'ORDER_STATUS'
  AND NOT EXISTS (
      SELECT 1 FROM sys_dict_item di WHERE di.dict_type_id = dt.id AND di.item_code = 'CANCELLED'
  );

-- 预置优先级字典项
INSERT INTO sys_dict_item (id, dict_type_id, item_code, item_name, item_value, description, enabled, sort_order, is_system, create_time, update_time)
SELECT uuid_generate_v4(), dt.id, 'URGENT', '紧急', 'URGENT', '最高优先级', true, 1, true, NOW(), NOW()
FROM sys_dict_type dt
WHERE dt.code = 'PRIORITY'
  AND NOT EXISTS (
      SELECT 1 FROM sys_dict_item di WHERE di.dict_type_id = dt.id AND di.item_code = 'URGENT'
  );

INSERT INTO sys_dict_item (id, dict_type_id, item_code, item_name, item_value, description, enabled, sort_order, is_system, create_time, update_time)
SELECT uuid_generate_v4(), dt.id, 'HIGH', '高', 'HIGH', '高优先级', true, 2, true, NOW(), NOW()
FROM sys_dict_type dt
WHERE dt.code = 'PRIORITY'
  AND NOT EXISTS (
      SELECT 1 FROM sys_dict_item di WHERE di.dict_type_id = dt.id AND di.item_code = 'HIGH'
  );

INSERT INTO sys_dict_item (id, dict_type_id, item_code, item_name, item_value, description, enabled, sort_order, is_system, create_time, update_time)
SELECT uuid_generate_v4(), dt.id, 'NORMAL', '中', 'NORMAL', '常规优先级', true, 3, true, NOW(), NOW()
FROM sys_dict_type dt
WHERE dt.code = 'PRIORITY'
  AND NOT EXISTS (
      SELECT 1 FROM sys_dict_item di WHERE di.dict_type_id = dt.id AND di.item_code = 'NORMAL'
  );

INSERT INTO sys_dict_item (id, dict_type_id, item_code, item_name, item_value, description, enabled, sort_order, is_system, create_time, update_time)
SELECT uuid_generate_v4(), dt.id, 'LOW', '低', 'LOW', '低优先级', true, 4, true, NOW(), NOW()
FROM sys_dict_type dt
WHERE dt.code = 'PRIORITY'
  AND NOT EXISTS (
      SELECT 1 FROM sys_dict_item di WHERE di.dict_type_id = dt.id AND di.item_code = 'LOW'
  );

-- 预置设备状态字典项
INSERT INTO sys_dict_item (id, dict_type_id, item_code, item_name, item_value, description, enabled, sort_order, is_system, create_time, update_time)
SELECT uuid_generate_v4(), dt.id, 'RUNNING', '运行中', 'RUNNING', '设备正常运行', true, 1, true, NOW(), NOW()
FROM sys_dict_type dt
WHERE dt.code = 'MACHINE_STATUS'
  AND NOT EXISTS (
      SELECT 1 FROM sys_dict_item di WHERE di.dict_type_id = dt.id AND di.item_code = 'RUNNING'
  );

INSERT INTO sys_dict_item (id, dict_type_id, item_code, item_name, item_value, description, enabled, sort_order, is_system, create_time, update_time)
SELECT uuid_generate_v4(), dt.id, 'IDLE', '空闲', 'IDLE', '设备空闲可排产', true, 2, true, NOW(), NOW()
FROM sys_dict_type dt
WHERE dt.code = 'MACHINE_STATUS'
  AND NOT EXISTS (
      SELECT 1 FROM sys_dict_item di WHERE di.dict_type_id = dt.id AND di.item_code = 'IDLE'
  );

INSERT INTO sys_dict_item (id, dict_type_id, item_code, item_name, item_value, description, enabled, sort_order, is_system, create_time, update_time)
SELECT uuid_generate_v4(), dt.id, 'MAINTENANCE', '维护中', 'MAINTENANCE', '设备维护停机', true, 3, true, NOW(), NOW()
FROM sys_dict_type dt
WHERE dt.code = 'MACHINE_STATUS'
  AND NOT EXISTS (
      SELECT 1 FROM sys_dict_item di WHERE di.dict_type_id = dt.id AND di.item_code = 'MAINTENANCE'
  );

INSERT INTO sys_dict_item (id, dict_type_id, item_code, item_name, item_value, description, enabled, sort_order, is_system, create_time, update_time)
SELECT uuid_generate_v4(), dt.id, 'DISABLED', '停用', 'DISABLED', '设备不可用', true, 4, true, NOW(), NOW()
FROM sys_dict_type dt
WHERE dt.code = 'MACHINE_STATUS'
  AND NOT EXISTS (
      SELECT 1 FROM sys_dict_item di WHERE di.dict_type_id = dt.id AND di.item_code = 'DISABLED'
  );
