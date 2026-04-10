-- 工厂建模：车间 + 注塑机增强
-- V9__Workshop_And_Machine.sql

-- 车间表
CREATE TABLE workshops (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    code VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    calendar_id UUID REFERENCES factory_calendars(id) ON DELETE SET NULL,
    manager_name VARCHAR(50),
    enabled BOOLEAN DEFAULT TRUE,
    sort_order INT DEFAULT 0,
    description VARCHAR(500),
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 资源表新增注塑机属性
ALTER TABLE resources
    ADD COLUMN IF NOT EXISTS workshop_id UUID REFERENCES workshops(id) ON DELETE SET NULL,
    ADD COLUMN IF NOT EXISTS tonnage INT,
    ADD COLUMN IF NOT EXISTS machine_brand VARCHAR(50),
    ADD COLUMN IF NOT EXISTS machine_model VARCHAR(50),
    ADD COLUMN IF NOT EXISTS max_shot_weight DECIMAL(10,2),
    ADD COLUMN IF NOT EXISTS status VARCHAR(20),
    ADD COLUMN IF NOT EXISTS calendar_id UUID REFERENCES factory_calendars(id) ON DELETE SET NULL;

-- 索引
CREATE INDEX idx_workshops_calendar_id ON workshops(calendar_id);
CREATE INDEX idx_workshops_enabled ON workshops(enabled);
CREATE INDEX idx_resources_workshop_id ON resources(workshop_id);
CREATE INDEX idx_resources_status ON resources(status);
CREATE INDEX idx_resources_calendar_id ON resources(calendar_id);
