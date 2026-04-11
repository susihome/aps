-- 排程时间参数配置表
CREATE TABLE schedule_time_parameters (
    id                        UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    resource_id               UUID UNIQUE REFERENCES resources(id) ON DELETE CASCADE,

    -- 工单筛选范围 — "捞哪些工单"
    order_filter_start_days   INTEGER NOT NULL DEFAULT 0,
    order_filter_start_time   TIME    NOT NULL DEFAULT '08:00:00',
    order_filter_end_days     INTEGER NOT NULL DEFAULT 14,
    order_filter_end_time     TIME    NOT NULL DEFAULT '00:00:00',

    -- 排程安排起点 — "从什么时候开始排"
    planning_start_days       INTEGER NOT NULL DEFAULT 0,
    planning_start_time       TIME    NOT NULL DEFAULT '09:00:00',

    -- 显示范围 — "看多远"
    display_start_days        INTEGER NOT NULL DEFAULT 0,
    display_end_days          INTEGER NOT NULL DEFAULT 30,

    -- 辅助参数
    completion_days           INTEGER NOT NULL DEFAULT 0,
    time_scale                INTEGER NOT NULL DEFAULT 1,
    factor                    INTEGER NOT NULL DEFAULT 0,
    exceed_period             INTEGER,

    -- 通用字段
    is_default                BOOLEAN NOT NULL DEFAULT FALSE,
    enabled                   BOOLEAN NOT NULL DEFAULT TRUE,
    remark                    VARCHAR(500),
    create_time               TIMESTAMP NOT NULL DEFAULT NOW(),
    update_time               TIMESTAMP NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE  schedule_time_parameters                          IS '排程时间参数配置';
COMMENT ON COLUMN schedule_time_parameters.resource_id             IS '关联设备ID，NULL表示全局默认';
COMMENT ON COLUMN schedule_time_parameters.order_filter_start_days IS '排程始(天) — 工单筛选范围起点 = Today + N 天';
COMMENT ON COLUMN schedule_time_parameters.order_filter_start_time IS '始时间 — 工单筛选起点精确时间';
COMMENT ON COLUMN schedule_time_parameters.order_filter_end_days   IS '排程终(天) — 工单筛选范围终点 = Today + N 天';
COMMENT ON COLUMN schedule_time_parameters.order_filter_end_time   IS '终时间 — 工单筛选终点精确时间';
COMMENT ON COLUMN schedule_time_parameters.planning_start_days     IS '排程起(天) — 排程安排最早时刻 = Today + N 天';
COMMENT ON COLUMN schedule_time_parameters.planning_start_time     IS '起时间 — 排程安排最早精确时间';
COMMENT ON COLUMN schedule_time_parameters.display_start_days      IS '显示开始(天) — 甘特图起点 = Today + N 天';
COMMENT ON COLUMN schedule_time_parameters.display_end_days        IS '显示结束(天) — 甘特图终点 = Today + N 天';
COMMENT ON COLUMN schedule_time_parameters.completion_days         IS '完成(天) — 工单从开始排程到必须完成的天数限制';
COMMENT ON COLUMN schedule_time_parameters.time_scale              IS '刻度 — 甘特图时间刻度粒度（天）';
COMMENT ON COLUMN schedule_time_parameters.factor                  IS '因子 — 排程调整系数';
COMMENT ON COLUMN schedule_time_parameters.exceed_period           IS '超出期间(天) — 完成天数的可超出期间，允许在 completion_days 基础上额外超出';
COMMENT ON COLUMN schedule_time_parameters.is_default              IS '是否默认配置（全局默认唯一）';
