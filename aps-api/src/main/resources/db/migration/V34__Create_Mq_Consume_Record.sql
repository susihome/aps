CREATE TABLE IF NOT EXISTS mq_consume_record (
    id UUID PRIMARY KEY,
    message_id VARCHAR(128) NOT NULL,
    consumer_name VARCHAR(128) NOT NULL,
    business_key VARCHAR(128),
    consumed_at TIMESTAMP NOT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_mq_consume_record_msg_consumer
    ON mq_consume_record(message_id, consumer_name);
