CREATE TABLE IF NOT EXISTS materials (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    material_code VARCHAR(64) NOT NULL UNIQUE,
    material_name VARCHAR(120) NOT NULL,
    specification VARCHAR(255),
    unit VARCHAR(32),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    remark VARCHAR(500),
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_materials_enabled ON materials(enabled);
CREATE INDEX IF NOT EXISTS idx_materials_name ON materials(material_name);

CREATE TABLE IF NOT EXISTS molds (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    mold_code VARCHAR(64) NOT NULL UNIQUE,
    mold_name VARCHAR(120) NOT NULL,
    cavity_count INTEGER,
    status VARCHAR(20),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    remark VARCHAR(500),
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_molds_cavity_count CHECK (cavity_count IS NULL OR cavity_count > 0)
);

CREATE INDEX IF NOT EXISTS idx_molds_enabled ON molds(enabled);
CREATE INDEX IF NOT EXISTS idx_molds_status ON molds(status);
CREATE INDEX IF NOT EXISTS idx_molds_name ON molds(mold_name);
