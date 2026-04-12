CREATE TABLE IF NOT EXISTS material_mold_bindings (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    material_id UUID NOT NULL REFERENCES materials(id) ON DELETE CASCADE,
    mold_id UUID NOT NULL REFERENCES molds(id) ON DELETE CASCADE,
    priority INTEGER NOT NULL DEFAULT 0,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    is_preferred BOOLEAN NOT NULL DEFAULT FALSE,
    cycle_time_minutes INTEGER,
    setup_time_minutes INTEGER,
    changeover_time_minutes INTEGER,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    valid_from TIMESTAMP,
    valid_to TIMESTAMP,
    remark VARCHAR(500),
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_material_mold_bindings UNIQUE (material_id, mold_id),
    CONSTRAINT chk_material_mold_priority CHECK (priority >= 0),
    CONSTRAINT chk_material_mold_cycle_time CHECK (cycle_time_minutes IS NULL OR cycle_time_minutes > 0),
    CONSTRAINT chk_material_mold_setup_time CHECK (setup_time_minutes IS NULL OR setup_time_minutes >= 0),
    CONSTRAINT chk_material_mold_changeover_time CHECK (changeover_time_minutes IS NULL OR changeover_time_minutes >= 0),
    CONSTRAINT chk_material_mold_valid_range CHECK (valid_to IS NULL OR valid_from IS NULL OR valid_to >= valid_from)
);

ALTER TABLE molds
    ADD COLUMN IF NOT EXISTS required_tonnage INTEGER,
    ADD COLUMN IF NOT EXISTS max_shot_weight NUMERIC(10, 2),
    ADD COLUMN IF NOT EXISTS maintenance_state VARCHAR(32);

CREATE INDEX IF NOT EXISTS idx_material_mold_bindings_material_id
    ON material_mold_bindings(material_id);

CREATE INDEX IF NOT EXISTS idx_material_mold_bindings_mold_id
    ON material_mold_bindings(mold_id);

CREATE INDEX IF NOT EXISTS idx_material_mold_bindings_enabled
    ON material_mold_bindings(enabled);

CREATE INDEX IF NOT EXISTS idx_material_mold_bindings_default
    ON material_mold_bindings(is_default);
