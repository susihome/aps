CREATE TABLE IF NOT EXISTS schedule_solver_task (
    id UUID PRIMARY KEY,
    schedule_id UUID NOT NULL,
    task_type VARCHAR(32) NOT NULL,
    triggered_by UUID,
    trigger_source VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL,
    score VARCHAR(128),
    progress INTEGER,
    error_message TEXT,
    started_at TIMESTAMP,
    finished_at TIMESTAMP,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_schedule_solver_task_schedule_id
    ON schedule_solver_task(schedule_id);

CREATE INDEX IF NOT EXISTS idx_schedule_solver_task_status
    ON schedule_solver_task(status);
