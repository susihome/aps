ALTER TABLE materials
    ADD COLUMN IF NOT EXISTS color_code        VARCHAR(32),
    ADD COLUMN IF NOT EXISTS raw_material_type VARCHAR(32),
    ADD COLUMN IF NOT EXISTS default_lot_size  INTEGER,
    ADD COLUMN IF NOT EXISTS min_lot_size      INTEGER,
    ADD COLUMN IF NOT EXISTS max_lot_size      INTEGER,
    ADD COLUMN IF NOT EXISTS allow_delay       BOOLEAN,
    ADD COLUMN IF NOT EXISTS abc_classification VARCHAR(1),
    ADD COLUMN IF NOT EXISTS product_group     VARCHAR(32);

CREATE INDEX IF NOT EXISTS idx_materials_color_code        ON materials(color_code);
CREATE INDEX IF NOT EXISTS idx_materials_raw_material_type ON materials(raw_material_type);
CREATE INDEX IF NOT EXISTS idx_materials_product_group     ON materials(product_group);
CREATE INDEX IF NOT EXISTS idx_materials_abc_classification ON materials(abc_classification);

INSERT INTO sys_dict_type (id, code, name, description, enabled, sort_order, create_time, update_time)
VALUES
    (uuid_generate_v4(), 'MATERIAL_COLOR',    '物料颜色', '注塑排产换色防呆顺序：从浅到深', TRUE, 90, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (uuid_generate_v4(), 'MATERIAL_RAW_TYPE', '原料类型', '注塑排产温度阶梯顺序：从低温到高温', TRUE, 91, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (code) DO NOTHING;

INSERT INTO sys_dict_item (id, dict_type_id, item_code, item_name, item_value, description, enabled, sort_order, is_system, create_time, update_time)
SELECT uuid_generate_v4(), dt.id, 'TRANSPARENT',  '透明',        'TRANSPARENT',  '最浅，优先安排，无需洗机',             TRUE, 1, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM sys_dict_type dt WHERE dt.code = 'MATERIAL_COLOR'
UNION ALL
SELECT uuid_generate_v4(), dt.id, 'WHITE',         '白色',        'WHITE',         '透明后可直接换白，少量洗机',           TRUE, 2, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM sys_dict_type dt WHERE dt.code = 'MATERIAL_COLOR'
UNION ALL
SELECT uuid_generate_v4(), dt.id, 'LIGHT_YELLOW',  '浅黄',        'LIGHT_YELLOW',  '浅色系，换色成本低',                   TRUE, 3, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM sys_dict_type dt WHERE dt.code = 'MATERIAL_COLOR'
UNION ALL
SELECT uuid_generate_v4(), dt.id, 'RED',            '红色',        'RED',            '中深色，换色成本中等',                 TRUE, 4, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM sys_dict_type dt WHERE dt.code = 'MATERIAL_COLOR'
UNION ALL
SELECT uuid_generate_v4(), dt.id, 'BLUE',           '蓝色',        'BLUE',           '深色，换色成本较高',                   TRUE, 5, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM sys_dict_type dt WHERE dt.code = 'MATERIAL_COLOR'
UNION ALL
SELECT uuid_generate_v4(), dt.id, 'BLACK',          '黑色',        'BLACK',          '最深，安排在最后，换色成本最高',       TRUE, 6, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM sys_dict_type dt WHERE dt.code = 'MATERIAL_COLOR';

INSERT INTO sys_dict_item (id, dict_type_id, item_code, item_name, item_value, description, enabled, sort_order, is_system, create_time, update_time)
SELECT uuid_generate_v4(), dt.id, 'PP',  'PP-低温料',  'PP',  '加工温度约180-220°C，优先排产，无需升温等待',  TRUE, 1, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM sys_dict_type dt WHERE dt.code = 'MATERIAL_RAW_TYPE'
UNION ALL
SELECT uuid_generate_v4(), dt.id, 'ABS', 'ABS-中温料', 'ABS', '加工温度约200-240°C，从PP升温成本低',         TRUE, 2, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM sys_dict_type dt WHERE dt.code = 'MATERIAL_RAW_TYPE'
UNION ALL
SELECT uuid_generate_v4(), dt.id, 'PC',  'PC-高温料',  'PC',  '加工温度约260-300°C，安排在最后，逆序需降温等待', TRUE, 3, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP FROM sys_dict_type dt WHERE dt.code = 'MATERIAL_RAW_TYPE';
