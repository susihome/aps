-- 工厂日历功能
-- V8__Factory_Calendar.sql

-- 日历主表
CREATE TABLE factory_calendars (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(100) NOT NULL,
    code VARCHAR(50) UNIQUE NOT NULL,
    description VARCHAR(255),
    year INT NOT NULL,
    is_default BOOLEAN DEFAULT FALSE,
    enabled BOOLEAN DEFAULT TRUE,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 班次表
CREATE TABLE calendar_shifts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    calendar_id UUID NOT NULL REFERENCES factory_calendars(id) ON DELETE CASCADE,
    name VARCHAR(50) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    sort_order INT DEFAULT 0,
    next_day BOOLEAN DEFAULT FALSE,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 日历日期明细表
CREATE TABLE calendar_dates (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    calendar_id UUID NOT NULL REFERENCES factory_calendars(id) ON DELETE CASCADE,
    date DATE NOT NULL,
    date_type VARCHAR(20) NOT NULL DEFAULT 'WORKDAY',
    label VARCHAR(50),
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_calendar_date UNIQUE (calendar_id, date)
);

-- 索引
CREATE INDEX idx_calendar_shifts_calendar_id ON calendar_shifts(calendar_id);
CREATE INDEX idx_calendar_dates_calendar_id ON calendar_dates(calendar_id);
CREATE INDEX idx_calendar_dates_date ON calendar_dates(date);
CREATE INDEX idx_factory_calendars_year ON factory_calendars(year);
