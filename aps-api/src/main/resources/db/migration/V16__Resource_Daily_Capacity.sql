CREATE TABLE IF NOT EXISTS resource_capacity_days (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    resource_id UUID NOT NULL REFERENCES resources(id) ON DELETE CASCADE,
    capacity_date DATE NOT NULL,
    shift_minutes_override INT,
    utilization_rate DECIMAL(5,4) NOT NULL DEFAULT 1.0000,
    remark VARCHAR(255),
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_resource_capacity_days_resource_date UNIQUE (resource_id, capacity_date),
    CONSTRAINT chk_resource_capacity_days_shift_minutes CHECK (shift_minutes_override IS NULL OR shift_minutes_override >= 0),
    CONSTRAINT chk_resource_capacity_days_utilization CHECK (utilization_rate >= 0 AND utilization_rate <= 1)
);

CREATE INDEX IF NOT EXISTS idx_resource_capacity_days_resource_date
    ON resource_capacity_days(resource_id, capacity_date);
