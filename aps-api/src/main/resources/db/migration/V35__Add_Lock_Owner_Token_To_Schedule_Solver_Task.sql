ALTER TABLE schedule_solver_task
    ADD COLUMN lock_owner_token VARCHAR(64);

CREATE INDEX idx_solver_task_schedule_status
    ON schedule_solver_task(schedule_id, status, create_time DESC);
