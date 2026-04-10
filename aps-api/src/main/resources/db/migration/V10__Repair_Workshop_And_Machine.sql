CREATE TABLE IF NOT EXISTS workshops (
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

ALTER TABLE workshops
    ADD COLUMN IF NOT EXISTS code VARCHAR(50),
    ADD COLUMN IF NOT EXISTS name VARCHAR(100),
    ADD COLUMN IF NOT EXISTS calendar_id UUID REFERENCES factory_calendars(id) ON DELETE SET NULL,
    ADD COLUMN IF NOT EXISTS manager_name VARCHAR(50),
    ADD COLUMN IF NOT EXISTS enabled BOOLEAN DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS sort_order INT DEFAULT 0,
    ADD COLUMN IF NOT EXISTS description VARCHAR(500),
    ADD COLUMN IF NOT EXISTS create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN IF NOT EXISTS update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE resources
    ADD COLUMN IF NOT EXISTS workshop_id UUID REFERENCES workshops(id) ON DELETE SET NULL,
    ADD COLUMN IF NOT EXISTS tonnage INT,
    ADD COLUMN IF NOT EXISTS machine_brand VARCHAR(50),
    ADD COLUMN IF NOT EXISTS machine_model VARCHAR(50),
    ADD COLUMN IF NOT EXISTS max_shot_weight DECIMAL(10,2),
    ADD COLUMN IF NOT EXISTS status VARCHAR(20),
    ADD COLUMN IF NOT EXISTS calendar_id UUID REFERENCES factory_calendars(id) ON DELETE SET NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uk_workshops_code ON workshops(code);
CREATE INDEX IF NOT EXISTS idx_workshops_calendar_id ON workshops(calendar_id);
CREATE INDEX IF NOT EXISTS idx_workshops_enabled ON workshops(enabled);
CREATE INDEX IF NOT EXISTS idx_resources_workshop_id ON resources(workshop_id);
CREATE INDEX IF NOT EXISTS idx_resources_status ON resources(status);
CREATE INDEX IF NOT EXISTS idx_resources_calendar_id ON resources(calendar_id);
