CREATE TABLE IF NOT EXISTS sys_file_object (
    id UUID PRIMARY KEY,
    business_type VARCHAR(64) NOT NULL,
    bucket_name VARCHAR(128) NOT NULL,
    object_key VARCHAR(512) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(128),
    file_size BIGINT NOT NULL,
    sha256 VARCHAR(64),
    storage_provider VARCHAR(32) NOT NULL,
    visibility VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_sys_file_object_bucket_key
    ON sys_file_object(bucket_name, object_key);

CREATE INDEX IF NOT EXISTS idx_sys_file_object_business_type
    ON sys_file_object(business_type);

CREATE INDEX IF NOT EXISTS idx_sys_file_object_expires_at
    ON sys_file_object(expires_at);
