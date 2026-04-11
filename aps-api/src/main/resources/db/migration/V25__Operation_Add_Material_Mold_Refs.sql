ALTER TABLE operations
    ADD COLUMN IF NOT EXISTS required_material_id UUID REFERENCES materials(id),
    ADD COLUMN IF NOT EXISTS required_mold_id UUID REFERENCES molds(id);

CREATE INDEX IF NOT EXISTS idx_operations_required_material_id
    ON operations(required_material_id);

CREATE INDEX IF NOT EXISTS idx_operations_required_mold_id
    ON operations(required_mold_id);
