--
-- PostgreSQL database dump
--

\restrict 5FD6Tn0s4KM02Om9biT9qt3ztVtN7heOzaI2ESbxbwC5nEgHKRoOuB7PSyu0TJb

-- Dumped from database version 16.13 (Debian 16.13-1.pgdg13+1)
-- Dumped by pg_dump version 18.1

-- Started on 2026-04-11 08:18:48

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- TOC entry 5 (class 2615 OID 2200)
-- Name: public; Type: SCHEMA; Schema: -; Owner: pg_database_owner
--

CREATE SCHEMA public;


ALTER SCHEMA public OWNER TO pg_database_owner;

--
-- TOC entry 3728 (class 0 OID 0)
-- Dependencies: 5
-- Name: SCHEMA public; Type: COMMENT; Schema: -; Owner: pg_database_owner
--

COMMENT ON SCHEMA public IS 'standard public schema';


SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- TOC entry 228 (class 1259 OID 17331)
-- Name: assignments; Type: TABLE; Schema: public; Owner: aps_user
--

CREATE TABLE public.assignments (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    schedule_id uuid NOT NULL,
    operation_id uuid NOT NULL,
    assigned_resource_id uuid,
    start_time timestamp without time zone,
    end_time timestamp without time zone,
    pinned boolean DEFAULT false,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


ALTER TABLE public.assignments OWNER TO aps_user;

--
-- TOC entry 222 (class 1259 OID 17253)
-- Name: audit_logs; Type: TABLE; Schema: public; Owner: aps_user
--

CREATE TABLE public.audit_logs (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    user_id uuid,
    username character varying(50),
    action character varying(50) NOT NULL,
    resource character varying(100),
    details text,
    ip_address character varying(45),
    "timestamp" timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


ALTER TABLE public.audit_logs OWNER TO aps_user;

--
-- TOC entry 231 (class 1259 OID 17404)
-- Name: calendar_dates; Type: TABLE; Schema: public; Owner: aps_user
--

CREATE TABLE public.calendar_dates (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    calendar_id uuid NOT NULL,
    date date NOT NULL,
    date_type character varying(20) DEFAULT 'WORKDAY'::character varying NOT NULL,
    label character varying(50),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


ALTER TABLE public.calendar_dates OWNER TO aps_user;

--
-- TOC entry 230 (class 1259 OID 17390)
-- Name: calendar_shifts; Type: TABLE; Schema: public; Owner: aps_user
--

CREATE TABLE public.calendar_shifts (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    calendar_id uuid NOT NULL,
    name character varying(50) NOT NULL,
    start_time time without time zone NOT NULL,
    end_time time without time zone NOT NULL,
    sort_order integer DEFAULT 0,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    next_day boolean DEFAULT false NOT NULL,
    break_minutes integer DEFAULT 0 NOT NULL
);


ALTER TABLE public.calendar_shifts OWNER TO aps_user;

--
-- TOC entry 229 (class 1259 OID 17378)
-- Name: factory_calendars; Type: TABLE; Schema: public; Owner: aps_user
--

CREATE TABLE public.factory_calendars (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    name character varying(100) NOT NULL,
    code character varying(50) NOT NULL,
    description character varying(255),
    year integer NOT NULL,
    is_default boolean DEFAULT false,
    enabled boolean DEFAULT true,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


ALTER TABLE public.factory_calendars OWNER TO aps_user;

--
-- TOC entry 216 (class 1259 OID 17181)
-- Name: flyway_schema_history; Type: TABLE; Schema: public; Owner: aps_user
--

CREATE TABLE public.flyway_schema_history (
    installed_rank integer NOT NULL,
    version character varying(50),
    description character varying(200) NOT NULL,
    type character varying(20) NOT NULL,
    script character varying(1000) NOT NULL,
    checksum integer,
    installed_by character varying(100) NOT NULL,
    installed_on timestamp without time zone DEFAULT now() NOT NULL,
    execution_time integer NOT NULL,
    success boolean NOT NULL
);


ALTER TABLE public.flyway_schema_history OWNER TO aps_user;

--
-- TOC entry 225 (class 1259 OID 17290)
-- Name: operations; Type: TABLE; Schema: public; Owner: aps_user
--

CREATE TABLE public.operations (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    order_id uuid NOT NULL,
    operation_code character varying(50),
    operation_name character varying(100),
    sequence integer NOT NULL,
    standard_duration integer,
    required_resource_id uuid,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


ALTER TABLE public.operations OWNER TO aps_user;

--
-- TOC entry 224 (class 1259 OID 17280)
-- Name: orders; Type: TABLE; Schema: public; Owner: aps_user
--

CREATE TABLE public.orders (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    order_no character varying(50) NOT NULL,
    product_code character varying(50),
    product_name character varying(100),
    quantity integer NOT NULL,
    priority character varying(20) NOT NULL,
    status character varying(20) NOT NULL,
    due_date timestamp without time zone NOT NULL,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


ALTER TABLE public.orders OWNER TO aps_user;

--
-- TOC entry 219 (class 1259 OID 17211)
-- Name: permissions; Type: TABLE; Schema: public; Owner: aps_user
--

CREATE TABLE public.permissions (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    code character varying(100) NOT NULL,
    description character varying(255),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    name character varying(100) DEFAULT ''::character varying NOT NULL,
    type character varying(20) DEFAULT 'BUTTON'::character varying NOT NULL,
    route_path character varying(255),
    icon character varying(50),
    sort integer DEFAULT 0 NOT NULL,
    enabled boolean DEFAULT true NOT NULL,
    visible boolean DEFAULT true NOT NULL,
    parent_id uuid
);


ALTER TABLE public.permissions OWNER TO aps_user;

--
-- TOC entry 3729 (class 0 OID 0)
-- Dependencies: 219
-- Name: TABLE permissions; Type: COMMENT; Schema: public; Owner: aps_user
--

COMMENT ON TABLE public.permissions IS '权限表';


--
-- TOC entry 3730 (class 0 OID 0)
-- Dependencies: 219
-- Name: COLUMN permissions.code; Type: COMMENT; Schema: public; Owner: aps_user
--

COMMENT ON COLUMN public.permissions.code IS '权限编码（唯一）';


--
-- TOC entry 3731 (class 0 OID 0)
-- Dependencies: 219
-- Name: COLUMN permissions.description; Type: COMMENT; Schema: public; Owner: aps_user
--

COMMENT ON COLUMN public.permissions.description IS '权限描述';


--
-- TOC entry 3732 (class 0 OID 0)
-- Dependencies: 219
-- Name: COLUMN permissions.name; Type: COMMENT; Schema: public; Owner: aps_user
--

COMMENT ON COLUMN public.permissions.name IS '权限名称';


--
-- TOC entry 3733 (class 0 OID 0)
-- Dependencies: 219
-- Name: COLUMN permissions.type; Type: COMMENT; Schema: public; Owner: aps_user
--

COMMENT ON COLUMN public.permissions.type IS '权限类型：CATALOG-目录, MENU-菜单, BUTTON-按钮';


--
-- TOC entry 3734 (class 0 OID 0)
-- Dependencies: 219
-- Name: COLUMN permissions.route_path; Type: COMMENT; Schema: public; Owner: aps_user
--

COMMENT ON COLUMN public.permissions.route_path IS '路由路径';


--
-- TOC entry 3735 (class 0 OID 0)
-- Dependencies: 219
-- Name: COLUMN permissions.icon; Type: COMMENT; Schema: public; Owner: aps_user
--

COMMENT ON COLUMN public.permissions.icon IS '图标名称';


--
-- TOC entry 3736 (class 0 OID 0)
-- Dependencies: 219
-- Name: COLUMN permissions.sort; Type: COMMENT; Schema: public; Owner: aps_user
--

COMMENT ON COLUMN public.permissions.sort IS '排序号';


--
-- TOC entry 3737 (class 0 OID 0)
-- Dependencies: 219
-- Name: COLUMN permissions.enabled; Type: COMMENT; Schema: public; Owner: aps_user
--

COMMENT ON COLUMN public.permissions.enabled IS '是否启用';


--
-- TOC entry 3738 (class 0 OID 0)
-- Dependencies: 219
-- Name: COLUMN permissions.visible; Type: COMMENT; Schema: public; Owner: aps_user
--

COMMENT ON COLUMN public.permissions.visible IS '是否可见';


--
-- TOC entry 3739 (class 0 OID 0)
-- Dependencies: 219
-- Name: COLUMN permissions.parent_id; Type: COMMENT; Schema: public; Owner: aps_user
--

COMMENT ON COLUMN public.permissions.parent_id IS '父权限ID';


--
-- TOC entry 235 (class 1259 OID 17495)
-- Name: resource_capacity_days; Type: TABLE; Schema: public; Owner: aps_user
--

CREATE TABLE public.resource_capacity_days (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    resource_id uuid NOT NULL,
    capacity_date date NOT NULL,
    shift_minutes_override integer,
    utilization_rate numeric(5,4) DEFAULT 1.0000 NOT NULL,
    remark character varying(255),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT chk_resource_capacity_days_shift_minutes CHECK (((shift_minutes_override IS NULL) OR (shift_minutes_override >= 0))),
    CONSTRAINT chk_resource_capacity_days_utilization CHECK (((utilization_rate >= (0)::numeric) AND (utilization_rate <= (1)::numeric)))
);


ALTER TABLE public.resource_capacity_days OWNER TO aps_user;

--
-- TOC entry 223 (class 1259 OID 17269)
-- Name: resources; Type: TABLE; Schema: public; Owner: aps_user
--

CREATE TABLE public.resources (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    resource_code character varying(50) NOT NULL,
    resource_name character varying(100),
    resource_type character varying(50),
    available boolean DEFAULT true,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    workshop_id uuid,
    tonnage integer,
    machine_brand character varying(50),
    machine_model character varying(50),
    max_shot_weight numeric(10,2),
    status character varying(20),
    calendar_id uuid
);


ALTER TABLE public.resources OWNER TO aps_user;

--
-- TOC entry 221 (class 1259 OID 17238)
-- Name: role_permissions; Type: TABLE; Schema: public; Owner: aps_user
--

CREATE TABLE public.role_permissions (
    role_id uuid NOT NULL,
    permission_id uuid NOT NULL
);


ALTER TABLE public.role_permissions OWNER TO aps_user;

--
-- TOC entry 218 (class 1259 OID 17201)
-- Name: roles; Type: TABLE; Schema: public; Owner: aps_user
--

CREATE TABLE public.roles (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    name character varying(50) NOT NULL,
    description character varying(255),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


ALTER TABLE public.roles OWNER TO aps_user;

--
-- TOC entry 227 (class 1259 OID 17316)
-- Name: schedule_resources; Type: TABLE; Schema: public; Owner: aps_user
--

CREATE TABLE public.schedule_resources (
    schedule_id uuid NOT NULL,
    resource_id uuid NOT NULL
);


ALTER TABLE public.schedule_resources OWNER TO aps_user;

--
-- TOC entry 236 (class 1259 OID 17515)
-- Name: schedule_time_parameters; Type: TABLE; Schema: public; Owner: aps_user
--

CREATE TABLE public.schedule_time_parameters (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    resource_id uuid,
    order_filter_start_days integer DEFAULT 0 NOT NULL,
    order_filter_start_time time without time zone DEFAULT '08:00:00'::time without time zone NOT NULL,
    order_filter_end_days integer DEFAULT 14 NOT NULL,
    order_filter_end_time time without time zone DEFAULT '00:00:00'::time without time zone NOT NULL,
    planning_start_days integer DEFAULT 0 NOT NULL,
    planning_start_time time without time zone DEFAULT '09:00:00'::time without time zone NOT NULL,
    display_start_days integer DEFAULT 0 NOT NULL,
    display_end_days integer DEFAULT 30 NOT NULL,
    completion_days integer DEFAULT 0 NOT NULL,
    time_scale integer DEFAULT 1 NOT NULL,
    factor integer DEFAULT 0 NOT NULL,
    exceed_period integer,
    is_default boolean DEFAULT false NOT NULL,
    enabled boolean DEFAULT true NOT NULL,
    remark character varying(500),
    create_time timestamp without time zone DEFAULT now() NOT NULL,
    update_time timestamp without time zone DEFAULT now() NOT NULL
);


ALTER TABLE public.schedule_time_parameters OWNER TO aps_user;

--
-- TOC entry 3740 (class 0 OID 0)
-- Dependencies: 236
-- Name: TABLE schedule_time_parameters; Type: COMMENT; Schema: public; Owner: aps_user
--

COMMENT ON TABLE public.schedule_time_parameters IS '排程时间参数配置';


--
-- TOC entry 3741 (class 0 OID 0)
-- Dependencies: 236
-- Name: COLUMN schedule_time_parameters.resource_id; Type: COMMENT; Schema: public; Owner: aps_user
--

COMMENT ON COLUMN public.schedule_time_parameters.resource_id IS '关联设备ID，NULL表示全局默认';


--
-- TOC entry 3742 (class 0 OID 0)
-- Dependencies: 236
-- Name: COLUMN schedule_time_parameters.order_filter_start_days; Type: COMMENT; Schema: public; Owner: aps_user
--

COMMENT ON COLUMN public.schedule_time_parameters.order_filter_start_days IS '排程始(天) — 工单筛选范围起点 = Today + N 天';


--
-- TOC entry 3743 (class 0 OID 0)
-- Dependencies: 236
-- Name: COLUMN schedule_time_parameters.order_filter_start_time; Type: COMMENT; Schema: public; Owner: aps_user
--

COMMENT ON COLUMN public.schedule_time_parameters.order_filter_start_time IS '始时间 — 工单筛选起点精确时间';


--
-- TOC entry 3744 (class 0 OID 0)
-- Dependencies: 236
-- Name: COLUMN schedule_time_parameters.order_filter_end_days; Type: COMMENT; Schema: public; Owner: aps_user
--

COMMENT ON COLUMN public.schedule_time_parameters.order_filter_end_days IS '排程终(天) — 工单筛选范围终点 = Today + N 天';


--
-- TOC entry 3745 (class 0 OID 0)
-- Dependencies: 236
-- Name: COLUMN schedule_time_parameters.order_filter_end_time; Type: COMMENT; Schema: public; Owner: aps_user
--

COMMENT ON COLUMN public.schedule_time_parameters.order_filter_end_time IS '终时间 — 工单筛选终点精确时间';


--
-- TOC entry 3746 (class 0 OID 0)
-- Dependencies: 236
-- Name: COLUMN schedule_time_parameters.planning_start_days; Type: COMMENT; Schema: public; Owner: aps_user
--

COMMENT ON COLUMN public.schedule_time_parameters.planning_start_days IS '排程起(天) — 排程安排最早时刻 = Today + N 天';


--
-- TOC entry 3747 (class 0 OID 0)
-- Dependencies: 236
-- Name: COLUMN schedule_time_parameters.planning_start_time; Type: COMMENT; Schema: public; Owner: aps_user
--

COMMENT ON COLUMN public.schedule_time_parameters.planning_start_time IS '起时间 — 排程安排最早精确时间';


--
-- TOC entry 3748 (class 0 OID 0)
-- Dependencies: 236
-- Name: COLUMN schedule_time_parameters.display_start_days; Type: COMMENT; Schema: public; Owner: aps_user
--

COMMENT ON COLUMN public.schedule_time_parameters.display_start_days IS '显示开始(天) — 甘特图起点 = Today + N 天';


--
-- TOC entry 3749 (class 0 OID 0)
-- Dependencies: 236
-- Name: COLUMN schedule_time_parameters.display_end_days; Type: COMMENT; Schema: public; Owner: aps_user
--

COMMENT ON COLUMN public.schedule_time_parameters.display_end_days IS '显示结束(天) — 甘特图终点 = Today + N 天';


--
-- TOC entry 3750 (class 0 OID 0)
-- Dependencies: 236
-- Name: COLUMN schedule_time_parameters.completion_days; Type: COMMENT; Schema: public; Owner: aps_user
--

COMMENT ON COLUMN public.schedule_time_parameters.completion_days IS '完成(天) — 工单从开始排程到必须完成的天数限制';


--
-- TOC entry 3751 (class 0 OID 0)
-- Dependencies: 236
-- Name: COLUMN schedule_time_parameters.time_scale; Type: COMMENT; Schema: public; Owner: aps_user
--

COMMENT ON COLUMN public.schedule_time_parameters.time_scale IS '刻度 — 甘特图时间刻度粒度（天）';


--
-- TOC entry 3752 (class 0 OID 0)
-- Dependencies: 236
-- Name: COLUMN schedule_time_parameters.factor; Type: COMMENT; Schema: public; Owner: aps_user
--

COMMENT ON COLUMN public.schedule_time_parameters.factor IS '因子 — 排程调整系数';


--
-- TOC entry 3753 (class 0 OID 0)
-- Dependencies: 236
-- Name: COLUMN schedule_time_parameters.exceed_period; Type: COMMENT; Schema: public; Owner: aps_user
--

COMMENT ON COLUMN public.schedule_time_parameters.exceed_period IS '超出期间(天) — 完成天数的可超出期间，允许在 completion_days 基础上额外超出';


--
-- TOC entry 3754 (class 0 OID 0)
-- Dependencies: 236
-- Name: COLUMN schedule_time_parameters.is_default; Type: COMMENT; Schema: public; Owner: aps_user
--

COMMENT ON COLUMN public.schedule_time_parameters.is_default IS '是否默认配置（全局默认唯一）';


--
-- TOC entry 226 (class 1259 OID 17308)
-- Name: schedules; Type: TABLE; Schema: public; Owner: aps_user
--

CREATE TABLE public.schedules (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    name character varying(100) NOT NULL,
    status character varying(20) NOT NULL,
    schedule_start_time timestamp without time zone NOT NULL,
    schedule_end_time timestamp without time zone NOT NULL,
    final_score character varying(100),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


ALTER TABLE public.schedules OWNER TO aps_user;

--
-- TOC entry 234 (class 1259 OID 17473)
-- Name: sys_dict_item; Type: TABLE; Schema: public; Owner: aps_user
--

CREATE TABLE public.sys_dict_item (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    dict_type_id uuid NOT NULL,
    item_code character varying(64) NOT NULL,
    item_name character varying(100) NOT NULL,
    item_value character varying(100) NOT NULL,
    description character varying(500),
    enabled boolean DEFAULT true NOT NULL,
    sort_order integer DEFAULT 0 NOT NULL,
    is_system boolean DEFAULT false NOT NULL,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


ALTER TABLE public.sys_dict_item OWNER TO aps_user;

--
-- TOC entry 233 (class 1259 OID 17461)
-- Name: sys_dict_type; Type: TABLE; Schema: public; Owner: aps_user
--

CREATE TABLE public.sys_dict_type (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    code character varying(64) NOT NULL,
    name character varying(100) NOT NULL,
    description character varying(500),
    enabled boolean DEFAULT true NOT NULL,
    sort_order integer DEFAULT 0 NOT NULL,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


ALTER TABLE public.sys_dict_type OWNER TO aps_user;

--
-- TOC entry 220 (class 1259 OID 17223)
-- Name: user_roles; Type: TABLE; Schema: public; Owner: aps_user
--

CREATE TABLE public.user_roles (
    user_id uuid NOT NULL,
    role_id uuid NOT NULL
);


ALTER TABLE public.user_roles OWNER TO aps_user;

--
-- TOC entry 217 (class 1259 OID 17190)
-- Name: users; Type: TABLE; Schema: public; Owner: aps_user
--

CREATE TABLE public.users (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    username character varying(50) NOT NULL,
    password_hash character varying(255) NOT NULL,
    email character varying(100),
    enabled boolean DEFAULT true,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    last_login_at timestamp without time zone
);


ALTER TABLE public.users OWNER TO aps_user;

--
-- TOC entry 232 (class 1259 OID 17425)
-- Name: workshops; Type: TABLE; Schema: public; Owner: aps_user
--

CREATE TABLE public.workshops (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    code character varying(50) NOT NULL,
    name character varying(100) NOT NULL,
    calendar_id uuid,
    manager_name character varying(50),
    enabled boolean DEFAULT true,
    sort_order integer DEFAULT 0,
    description character varying(500),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


ALTER TABLE public.workshops OWNER TO aps_user;

--
-- TOC entry 3714 (class 0 OID 17331)
-- Dependencies: 228
-- Data for Name: assignments; Type: TABLE DATA; Schema: public; Owner: aps_user
--

COPY public.assignments (id, schedule_id, operation_id, assigned_resource_id, start_time, end_time, pinned, create_time, update_time) FROM stdin;
\.


--
-- TOC entry 3708 (class 0 OID 17253)
-- Dependencies: 222
-- Data for Name: audit_logs; Type: TABLE DATA; Schema: public; Owner: aps_user
--

COPY public.audit_logs (id, user_id, username, action, resource, details, ip_address, "timestamp", create_time, update_time) FROM stdin;
7094acfa-336a-4727-af70-f4c6039dfa51	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	LOGIN	auth	用户登录成功	0:0:0:0:0:0:0:1	2026-04-07 16:56:59.335707	2026-04-07 16:56:59.335707	2026-04-07 16:56:59.335707
e7c34cbb-1f69-49fc-8107-2c5f19863b62	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	LOGIN	auth	用户登录成功	0:0:0:0:0:0:0:1	2026-04-07 17:01:57.531565	2026-04-07 17:01:57.534579	2026-04-07 17:01:57.534579
4609a086-8e6d-4017-937c-29e7a101204d	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	LOGIN	auth	用户登录成功	0:0:0:0:0:0:0:1	2026-04-07 17:05:46.343189	2026-04-07 17:05:46.345704	2026-04-07 17:05:46.345704
723d20c0-7ee0-404e-9d3e-5b160571b676	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	LOGIN	auth	用户登录成功	0:0:0:0:0:0:0:1	2026-04-07 17:08:38.715239	2026-04-07 17:08:38.715239	2026-04-07 17:08:38.715239
e2a350ee-105e-43ed-b41f-35b0fc9d6c80	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	LOGIN	auth	用户登录成功	0:0:0:0:0:0:0:1	2026-04-07 17:12:25.124266	2026-04-07 17:12:25.125257	2026-04-07 17:12:25.125257
1a461c7f-5585-476a-89b3-211be34c8fec	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	LOGIN	auth	用户登录成功	0:0:0:0:0:0:0:1	2026-04-07 17:17:42.480796	2026-04-07 17:17:42.48745	2026-04-07 17:17:42.48745
95b0b76d-0b3c-4b3e-ad63-185d2fd44fb1	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	LOGIN	auth	用户登录成功	0:0:0:0:0:0:0:1	2026-04-07 17:18:05.908861	2026-04-07 17:18:05.909865	2026-04-07 17:18:05.909865
28bef838-2dd5-4bfe-ab36-2246056d709d	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	LOGIN	auth	用户登录成功	0:0:0:0:0:0:0:1	2026-04-07 17:18:33.59654	2026-04-07 17:18:33.59654	2026-04-07 17:18:33.59654
1bfc21fa-3d5d-41e8-9ff9-51ae486e7982	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	LOGIN	auth	用户登录成功	0:0:0:0:0:0:0:1	2026-04-07 17:18:45.287496	2026-04-07 17:18:45.288002	2026-04-07 17:18:45.288002
8a4d79ba-673c-4415-8dac-5a6012359af6	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	LOGIN	auth	用户登录成功	0:0:0:0:0:0:0:1	2026-04-07 17:24:23.428009	2026-04-07 17:24:23.430526	2026-04-07 17:24:23.430526
6a230f21-0832-4623-b57d-e9502513844a	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	LOGIN	auth	用户登录成功	0:0:0:0:0:0:0:1	2026-04-07 17:25:32.694655	2026-04-07 17:25:32.694655	2026-04-07 17:25:32.694655
6cb615de-6fb6-480c-9524-cf9bcf1b31ec	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	LOGIN	auth	用户登录成功	0:0:0:0:0:0:0:1	2026-04-07 17:28:54.631699	2026-04-07 17:28:54.646775	2026-04-07 17:28:54.646775
015a2927-0c42-4ba4-844f-9b8bfc9ddb2f	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	LOGIN	auth	用户登录成功	0:0:0:0:0:0:0:1	2026-04-07 17:39:17.534674	2026-04-07 17:39:17.536227	2026-04-07 17:39:17.536227
bbf2327a-75c5-4508-86fa-8e7c3ddd5ee5	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	LOGIN	auth	用户登录成功	0:0:0:0:0:0:0:1	2026-04-07 17:46:32.29979	2026-04-07 17:46:32.29979	2026-04-07 17:46:32.29979
08ca7d1c-0659-4af0-8fe5-272897d94bb3	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	LOGIN	auth	用户登录成功	0:0:0:0:0:0:0:1	2026-04-07 18:06:58.682842	2026-04-07 18:06:58.683854	2026-04-07 18:06:58.683854
15ff8fcc-c0b9-4c66-aaad-32c64e797c60	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	LOGIN	auth	用户登录成功	0:0:0:0:0:0:0:1	2026-04-07 18:07:34.008745	2026-04-07 18:07:34.008745	2026-04-07 18:07:34.008745
a28c39fb-4797-4218-80fa-53cb3b9b2ea4	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	LOGIN	auth	用户登录成功	0:0:0:0:0:0:0:1	2026-04-07 18:10:51.849519	2026-04-07 18:10:51.849519	2026-04-07 18:10:51.849519
eb6b77dd-5e70-47ef-89e9-0e2fa7a1caa7	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	LOGIN	auth	用户登录成功	0:0:0:0:0:0:0:1	2026-04-07 18:14:51.448623	2026-04-07 18:14:51.448623	2026-04-07 18:14:51.448623
f286ae2c-3ef2-4131-b8c3-ed64d3922956	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	LOGIN	auth	用户登录成功	0:0:0:0:0:0:0:1	2026-04-07 18:19:26.456947	2026-04-07 18:19:26.457679	2026-04-07 18:19:26.457679
51ad65f2-b91d-4969-905c-3a0ca5a1db26	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	LOGIN	auth	用户登录成功	0:0:0:0:0:0:0:1	2026-04-07 18:24:18.689825	2026-04-07 18:24:18.691291	2026-04-07 18:24:18.691291
bd701252-6d42-4dab-b8ea-61d40a7db9b0	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	LOGIN	auth	用户登录成功	0:0:0:0:0:0:0:1	2026-04-07 18:28:11.528593	2026-04-07 18:28:11.528593	2026-04-07 18:28:11.528593
f32cc87e-05d7-47ef-bc5b-d886e882a116	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	LOGIN	auth	用户登录成功	0:0:0:0:0:0:0:1	2026-04-07 20:02:41.665986	2026-04-07 20:02:41.672028	2026-04-07 20:02:41.672028
627b3a42-7c23-4fe1-a449-f84c29b13217	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	LOGIN	auth	用户登录成功	0:0:0:0:0:0:0:1	2026-04-08 07:40:41.515944	2026-04-08 07:40:41.516584	2026-04-08 07:40:41.516584
9a67bac7-ab9d-42e7-8a3c-23ebd046b8d5	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	LOGIN	auth	用户登录成功	0:0:0:0:0:0:0:1	2026-04-08 08:16:51.324117	2026-04-08 08:16:51.326633	2026-04-08 08:16:51.326633
34e13866-131a-4b96-a695-cda86f12b7de	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	ORDER_CREATE	Order	[{"id":null,"createTime":null,"updateTime":null,"orderNo":"TEST-ORDER-001","productCode":"PROD-001","productName":"Test Product","quantity":100,"priority":"HIGH","status":null,"dueDate":"2026-04-15T00:00:00","operations":[]}]	0:0:0:0:0:0:0:1	2026-04-08 08:18:59.149579	2026-04-08 08:18:59.149579	2026-04-08 08:18:59.149579
187d63a1-e314-4578-a5a0-dfcb515d4e70	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	ORDER_CREATE	Order	[{"id":null,"createTime":null,"updateTime":null,"orderNo":"TEST-ORDER-001","productCode":"PROD-001","productName":"Test Product","quantity":100,"priority":"HIGH","status":"PENDING","dueDate":"2026-04-15T00:00:00","operations":[]}]	0:0:0:0:0:0:0:1	2026-04-08 08:19:06.582222	2026-04-08 08:19:06.582222	2026-04-08 08:19:06.582222
0e01e3ed-d09a-4fa6-a8fd-969e746e07ec	\N	admin	LOGOUT	auth	用户登出	0:0:0:0:0:0:0:1	2026-04-08 08:22:57.705695	2026-04-08 08:22:57.706206	2026-04-08 08:22:57.706206
80c68584-4da0-4583-a817-2811be228e1e	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	LOGIN	auth	用户登录成功	0:0:0:0:0:0:0:1	2026-04-08 08:23:04.560135	2026-04-08 08:23:04.560135	2026-04-08 08:23:04.560135
ef3504fb-9c82-4336-a253-d251656d6ae0	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	LOGIN	auth	用户登录成功	0:0:0:0:0:0:0:1	2026-04-08 15:51:51.750104	2026-04-08 15:51:51.751615	2026-04-08 15:51:51.751615
d7242e02-f9de-4f05-b689-e5863671595b	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	LOGIN	auth	用户登录成功	0:0:0:0:0:0:0:1	2026-04-08 15:55:04.499026	2026-04-08 15:55:04.499026	2026-04-08 15:55:04.499026
ff938294-78ea-4faa-8f2d-6b36a5e94c37	\N	admin	LOGOUT	auth	用户登出	0:0:0:0:0:0:0:1	2026-04-08 16:27:20.06593	2026-04-08 16:27:20.06593	2026-04-08 16:27:20.06593
3c0dfbd6-6758-4435-81dc-db52f10a2fcc	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	LOGIN	auth	用户登录成功	0:0:0:0:0:0:0:1	2026-04-08 16:27:26.08563	2026-04-08 16:27:26.086625	2026-04-08 16:27:26.086625
6d721f42-dda1-4c89-95bb-9431d684517b	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	LOGIN	auth	用户登录成功	127.0.0.1	2026-04-08 17:32:46.009473	2026-04-08 17:32:46.009473	2026-04-08 17:32:46.009473
3f2236d0-57d7-41a3-aebb-d8300e40214e	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	LOGIN	auth	用户登录成功	127.0.0.1	2026-04-09 07:23:03.054774	2026-04-09 07:23:03.057296	2026-04-09 07:23:03.057296
41b47628-81c0-4311-8d61-095dab9b475c	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	CREATE	FactoryCalendar	["通用","SA",2026,""]	127.0.0.1	2026-04-09 07:23:26.978054	2026-04-09 07:23:26.978054	2026-04-09 07:23:26.978054
5ab78b86-50db-4850-bfc1-1d05f251e05e	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	UPDATE	FactoryCalendar	["0a9f8af2-c011-4c0a-8ad5-b0414afe4611","白班","08:00:43","20:00:43",1]	127.0.0.1	2026-04-09 07:30:09.864052	2026-04-09 07:30:09.864723	2026-04-09 07:30:09.864723
a3d46839-c344-4458-be07-6a4279d6c1e6	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	CREATE	FactoryCalendar	["夏季","SJ",2026,""]	127.0.0.1	2026-04-09 07:31:11.384208	2026-04-09 07:31:11.385208	2026-04-09 07:31:11.385208
1b3bfb18-d68e-4954-8b11-a30cf062b5a9	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	CREATE	FactoryCalendar	["夏季","SJ",2026,""]	127.0.0.1	2026-04-09 07:31:13.723549	2026-04-09 07:31:13.724494	2026-04-09 07:31:13.724494
7b3a8912-fa1f-4af5-b741-51773de8316b	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	UPDATE	FactoryCalendar	["0a9f8af2-c011-4c0a-8ad5-b0414afe4611","2026-01-02","RESTDAY",""]	127.0.0.1	2026-04-09 07:37:55.047175	2026-04-09 07:37:55.047175	2026-04-09 07:37:55.047175
9882c39e-5723-4461-bd83-2c817324ab5d	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	UPDATE	FactoryCalendar	["0a9f8af2-c011-4c0a-8ad5-b0414afe4611","2026-01-05","HOLIDAY",""]	127.0.0.1	2026-04-09 07:38:13.676785	2026-04-09 07:38:13.676785	2026-04-09 07:38:13.676785
556cdd18-0bb2-4aac-842d-2601cc0e098a	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	UPDATE	FactoryCalendar	["0a9f8af2-c011-4c0a-8ad5-b0414afe4611","2026-01-03","WORKDAY",""]	127.0.0.1	2026-04-09 07:38:42.40394	2026-04-09 07:38:42.40394	2026-04-09 07:38:42.40394
74518671-7570-45c7-9101-cecd51c14b5c	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	UPDATE	FactoryCalendar	["0a9f8af2-c011-4c0a-8ad5-b0414afe4611","2026-01-06","RESTDAY",""]	127.0.0.1	2026-04-09 07:38:57.798183	2026-04-09 07:38:57.798183	2026-04-09 07:38:57.798183
59c67064-0a29-458f-887d-4f16f9002390	\N	admin	LOGOUT	auth	用户登出	127.0.0.1	2026-04-09 07:52:04.794869	2026-04-09 07:52:04.801232	2026-04-09 07:52:04.801232
b25ed8a3-5c54-47b2-8e3b-edeeb0935d9b	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	LOGIN	auth	用户登录成功	127.0.0.1	2026-04-09 07:52:11.604967	2026-04-09 07:52:11.604967	2026-04-09 07:52:11.604967
629b42be-84a9-4232-973f-345346c0472c	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	DELETE	FactoryCalendar	["0a9f8af2-c011-4c0a-8ad5-b0414afe4611"]	127.0.0.1	2026-04-09 08:01:10.004121	2026-04-09 08:01:10.007287	2026-04-09 08:01:10.007287
5f811fbc-74c1-4365-9de0-6a478a58d10b	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	UPDATE	FactoryCalendar	["8648aa7c-5574-479f-9084-a9ca960b6855","2026-01-02","RESTDAY",""]	127.0.0.1	2026-04-09 08:01:25.851266	2026-04-09 08:01:25.851266	2026-04-09 08:01:25.851266
47977256-9746-49aa-a316-48e5a6f9e038	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	UPDATE	FactoryCalendar	["8648aa7c-5574-479f-9084-a9ca960b6855","2026-01-02","WORKDAY",""]	127.0.0.1	2026-04-09 08:07:18.477703	2026-04-09 08:07:18.481093	2026-04-09 08:07:18.481093
0c673ed0-e962-48bc-be58-6b40a2c00a0a	\N	admin	LOGOUT	auth	用户登出	127.0.0.1	2026-04-09 08:07:31.750878	2026-04-09 08:07:31.750878	2026-04-09 08:07:31.750878
f1421d5d-5207-4f7c-9f7d-ed0c56c3be67	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	LOGIN	auth	用户登录成功	127.0.0.1	2026-04-09 08:07:39.440388	2026-04-09 08:07:39.442249	2026-04-09 08:07:39.442249
7603eaac-de2b-4577-ae8a-625ea97665b9	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	UPDATE	FactoryCalendar	["8648aa7c-5574-479f-9084-a9ca960b6855","2026-01-02","WORKDAY",""]	127.0.0.1	2026-04-09 08:07:44.963575	2026-04-09 08:07:44.964392	2026-04-09 08:07:44.964392
6ba1f7c0-ce03-46c0-b3b2-aef91ef19d65	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	UPDATE	FactoryCalendar	["8648aa7c-5574-479f-9084-a9ca960b6855","2026-01-02","HOLIDAY",""]	127.0.0.1	2026-04-09 08:07:52.937348	2026-04-09 08:07:52.937348	2026-04-09 08:07:52.937348
0d9ff62c-1535-4338-b39d-72cca5c3cab1	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	UPDATE	FactoryCalendar	["8648aa7c-5574-479f-9084-a9ca960b6855","2026-01-02","WORKDAY",""]	127.0.0.1	2026-04-09 08:08:00.499631	2026-04-09 08:08:00.501234	2026-04-09 08:08:00.501234
b91bb9f1-c530-48ba-83ca-5a5986bf2105	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	UPDATE	FactoryCalendar	["8648aa7c-5574-479f-9084-a9ca960b6855","2026-01-02","WORKDAY",""]	127.0.0.1	2026-04-09 08:08:22.023886	2026-04-09 08:08:22.025131	2026-04-09 08:08:22.025131
402ef7bc-397e-43e1-9fbe-8bc7044fb1f1	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	UPDATE	FactoryCalendar	["8648aa7c-5574-479f-9084-a9ca960b6855","2026-01-02","RESTDAY",""]	127.0.0.1	2026-04-09 08:12:59.247883	2026-04-09 08:12:59.247883	2026-04-09 08:12:59.247883
8d5af5c7-5cd9-47fc-acf1-a03410a3899b	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	UPDATE	FactoryCalendar	["8648aa7c-5574-479f-9084-a9ca960b6855","2026-01-02","WORKDAY",""]	127.0.0.1	2026-04-09 08:18:17.063317	2026-04-09 08:18:17.064325	2026-04-09 08:18:17.064325
d861e4a8-927a-4709-9996-1332884657f7	\N	admin	LOGOUT	auth	用户登出	127.0.0.1	2026-04-09 08:18:20.875057	2026-04-09 08:18:20.875057	2026-04-09 08:18:20.875057
9da558a2-ba8f-41d3-bf3b-79985838d41e	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	LOGIN	auth	用户登录成功	127.0.0.1	2026-04-09 08:18:28.117933	2026-04-09 08:18:28.118933	2026-04-09 08:18:28.118933
e4a4e252-4761-47ce-bee8-3890e27deaa6	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	UPDATE	FactoryCalendar	["8648aa7c-5574-479f-9084-a9ca960b6855","2026-01-02","HOLIDAY",""]	127.0.0.1	2026-04-09 08:18:37.436672	2026-04-09 08:18:37.437713	2026-04-09 08:18:37.437713
681188fa-8653-4330-b88b-f587cadd0f34	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	UPDATE	FactoryCalendar	["8648aa7c-5574-479f-9084-a9ca960b6855","2026-01-02","WORKDAY",""]	127.0.0.1	2026-04-09 08:27:49.211928	2026-04-09 08:27:49.529982	2026-04-09 08:27:49.529982
83d7ef94-ba74-4fbc-8751-5a250071427f	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	UPDATE	FactoryCalendar	["8648aa7c-5574-479f-9084-a9ca960b6855","2026-01-05","RESTDAY",""]	127.0.0.1	2026-04-09 08:28:03.612215	2026-04-09 08:28:03.612215	2026-04-09 08:28:03.612215
c0e4c0e5-d50e-4ded-8b53-221fec415529	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	UPDATE	FactoryCalendar	["8648aa7c-5574-479f-9084-a9ca960b6855","2026-01-05","HOLIDAY",""]	127.0.0.1	2026-04-09 09:01:38.946819	2026-04-09 09:01:38.947257	2026-04-09 09:01:38.947257
8184f250-872a-4310-b1d5-05d0ff1f2262	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	UPDATE	FactoryCalendar	["8648aa7c-5574-479f-9084-a9ca960b6855","白班","08:00:45","20:00:45",1,false]	127.0.0.1	2026-04-09 09:02:10.312081	2026-04-09 09:02:10.312613	2026-04-09 09:02:10.312613
588f6352-c8dd-4233-baf3-83f17c030958	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	UPDATE	FactoryCalendar	["8648aa7c-5574-479f-9084-a9ca960b6855","晚班","20:00:11","08:00:11",2,true]	127.0.0.1	2026-04-09 09:02:38.171516	2026-04-09 09:02:38.172102	2026-04-09 09:02:38.172102
429ed3b0-1a3d-4e8c-b474-12a3b576e67e	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	UPDATE	FactoryCalendar	["b01dbfa2-f835-41f8-b08a-9c91fb182554","白班","08:00:00","20:00:00",1,false]	127.0.0.1	2026-04-09 09:05:23.581218	2026-04-09 09:05:23.581218	2026-04-09 09:05:23.581218
58313ca9-1425-482d-9c1b-050f28216d98	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	UPDATE	FactoryCalendar	["9d46b2c4-8e4a-446c-923e-50de4cfa76fa","晚班","20:00:00","08:00:00",2,true]	127.0.0.1	2026-04-09 09:05:28.263554	2026-04-09 09:05:28.263554	2026-04-09 09:05:28.263554
6680244e-de57-42de-a16e-4be61fdfa263	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	CREATE	FactoryCalendar	["通用","SA",2026,""]	127.0.0.1	2026-04-09 09:05:55.49753	2026-04-09 09:05:55.49753	2026-04-09 09:05:55.49753
49e4b297-6417-4a2d-ad40-d7ead232fd96	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	LOGIN	auth	用户登录成功	127.0.0.1	2026-04-09 09:26:46.826367	2026-04-09 09:26:46.828402	2026-04-09 09:26:46.828402
69271e98-8132-400b-b211-b1ea21d05b93	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	UPDATE	FactoryCalendar	["8648aa7c-5574-479f-9084-a9ca960b6855",["2026-04-04","2026-04-05","2026-04-06"],"清明节"]	127.0.0.1	2026-04-09 09:27:15.88272	2026-04-09 09:27:15.884227	2026-04-09 09:27:15.884227
7aae80e7-1e7d-463c-90cb-b5876c9f0388	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	UPDATE	FactoryCalendar	["8648aa7c-5574-479f-9084-a9ca960b6855",["2026-02-10","2026-02-11","2026-02-12","2026-02-13","2026-02-14","2026-02-15","2026-02-16","2026-02-17"],""]	127.0.0.1	2026-04-09 09:33:42.2804	2026-04-09 09:33:42.291609	2026-04-09 09:33:42.291609
febd3093-ab65-45e0-a6ca-4b8aecf1f5f2	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	UPDATE	FactoryCalendar	["a73e562e-fc18-4ac3-b966-d67799e3ae80","SINGLE"]	127.0.0.1	2026-04-09 10:24:43.655441	2026-04-09 10:24:43.65854	2026-04-09 10:24:43.65854
e5e16a73-27f8-4bda-8704-b6399816ac14	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	UPDATE	FactoryCalendar	["a73e562e-fc18-4ac3-b966-d67799e3ae80","DOUBLE"]	127.0.0.1	2026-04-09 10:24:49.363192	2026-04-09 10:24:49.364199	2026-04-09 10:24:49.364199
cc90afd7-6a65-4ad4-aa71-4f4ab9af8c7c	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	LOGIN	auth	用户登录成功	127.0.0.1	2026-04-09 10:40:34.482242	2026-04-09 10:40:34.484396	2026-04-09 10:40:34.484396
8c586562-6626-4dfe-8acf-83c089254f0f	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	UPDATE	FactoryCalendar	["a73e562e-fc18-4ac3-b966-d67799e3ae80","SINGLE"]	127.0.0.1	2026-04-09 11:22:00.523051	2026-04-09 11:22:00.529489	2026-04-09 11:22:00.529489
725d0e2b-a6cf-4bf9-8265-8d4c69e5acdf	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	UPDATE	FactoryCalendar	["a73e562e-fc18-4ac3-b966-d67799e3ae80","DOUBLE"]	127.0.0.1	2026-04-09 11:22:13.468263	2026-04-09 11:22:13.468263	2026-04-09 11:22:13.468263
ca8ae43d-6e34-4366-a31d-030bda96e26c	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	UPDATE	FactoryCalendar	["a73e562e-fc18-4ac3-b966-d67799e3ae80","SINGLE"]	127.0.0.1	2026-04-09 11:25:06.033544	2026-04-09 11:25:06.033544	2026-04-09 11:25:06.033544
d0ebea28-bdd2-4e5b-ad86-5d0ba2447ad4	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	LOGIN	auth	用户登录成功	127.0.0.1	2026-04-09 14:56:16.496676	2026-04-09 14:56:16.497707	2026-04-09 14:56:16.497707
bd998c7e-d92b-4ef3-9c68-edb81626a537	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	CREATE	Workshop	["ZS001","注塑车间","a73e562e-fc18-4ac3-b966-d67799e3ae80","张三",1,""]	127.0.0.1	2026-04-09 14:56:54.086353	2026-04-09 14:56:54.086353	2026-04-09 14:56:54.086353
9340aa25-dcb3-4867-8684-58ea0eb7cd8d	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	RESOURCE_CREATE	Resource	["ZS001","注塑1号机","INJECTION_MACHINE","c924f934-1a2a-40bd-ad22-6897ca9b1e7e",180,"好礼","",100,"IDLE","a73e562e-fc18-4ac3-b966-d67799e3ae80"]	127.0.0.1	2026-04-09 14:57:42.609186	2026-04-09 14:57:42.609186	2026-04-09 14:57:42.609186
aa77c51d-ef29-4ffa-b2f7-3358437bab75	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	DELETE	Workshop	["c924f934-1a2a-40bd-ad22-6897ca9b1e7e"],"error":"该车间下存在注塑机，无法删除"}	127.0.0.1	2026-04-09 14:57:49.514577	2026-04-09 14:57:49.514577	2026-04-09 14:57:49.514577
d8d672ef-0ff3-41dc-946a-2a202fa625f3	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	DELETE	Workshop	["c924f934-1a2a-40bd-ad22-6897ca9b1e7e"],"error":"该车间下存在注塑机，无法删除"}	127.0.0.1	2026-04-09 14:57:56.548517	2026-04-09 14:57:56.549094	2026-04-09 14:57:56.549094
30f6e974-c071-437a-8d3b-d11d74ecb1df	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	RESOURCE_DELETE	Resource	["67fe504c-65ea-4711-96b9-6f450410219d"]	127.0.0.1	2026-04-09 14:58:02.883728	2026-04-09 14:58:02.884729	2026-04-09 14:58:02.884729
516c5a98-1bd5-42a9-b0e5-d9cb345f1d55	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	DELETE	Workshop	["c924f934-1a2a-40bd-ad22-6897ca9b1e7e"]	127.0.0.1	2026-04-09 14:58:06.384726	2026-04-09 14:58:06.384726	2026-04-09 14:58:06.384726
e74aaa40-1643-48e1-8af0-6845e353cc50	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	CREATE	Workshop	["ZSCJ","注塑车间",null,"张三",1,""]	127.0.0.1	2026-04-09 14:58:50.203581	2026-04-09 14:58:50.203581	2026-04-09 14:58:50.203581
68cc77bb-bae9-40e6-b866-d749af1a5fd4	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	RESOURCE_CREATE	Resource	["ZS001","注塑1号机","INJECTION_MACHINE","0ec60c75-a830-4651-ab47-71f7d2b42ad7",180,"","",null,"IDLE","a73e562e-fc18-4ac3-b966-d67799e3ae80"]	127.0.0.1	2026-04-09 15:01:27.808647	2026-04-09 15:01:27.810156	2026-04-09 15:01:27.810156
120c3f75-7308-4827-ab82-2d06008f3cce	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	CREATE	Workshop	["ZSCJ2","注塑车间2",null,"",0,""]	127.0.0.1	2026-04-09 15:22:25.261673	2026-04-09 15:22:25.261673	2026-04-09 15:22:25.261673
9e6a830f-f998-46e0-b238-55ea8723c22e	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	UPDATE	FactoryCalendar	["a73e562e-fc18-4ac3-b966-d67799e3ae80"]	127.0.0.1	2026-04-09 15:28:44.152468	2026-04-09 15:28:44.152468	2026-04-09 15:28:44.152468
2006e117-4e9d-4415-a206-cf24d0033dd3	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	UPDATE	FactoryCalendar	["8648aa7c-5574-479f-9084-a9ca960b6855"]	127.0.0.1	2026-04-09 15:28:54.093318	2026-04-09 15:28:54.09497	2026-04-09 15:28:54.09497
1b4e074b-ecce-4138-ba0e-6c1514676846	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	UPDATE	FactoryCalendar	["a73e562e-fc18-4ac3-b966-d67799e3ae80"]	127.0.0.1	2026-04-09 15:28:58.797841	2026-04-09 15:28:58.797841	2026-04-09 15:28:58.797841
ce7b25f6-ce07-4244-be73-3d45091d43f3	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	UPDATE	FactoryCalendar	["8648aa7c-5574-479f-9084-a9ca960b6855"]	127.0.0.1	2026-04-09 15:29:04.816801	2026-04-09 15:29:04.81796	2026-04-09 15:29:04.81796
e129fde8-46a5-4626-8f98-6f375f3fa460	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	UPDATE	FactoryCalendar	["a73e562e-fc18-4ac3-b966-d67799e3ae80"]	127.0.0.1	2026-04-09 15:29:10.001682	2026-04-09 15:29:10.001682	2026-04-09 15:29:10.001682
3a124a94-4911-43cc-bfb3-169cd32c059f	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	UPDATE	FactoryCalendar	["8648aa7c-5574-479f-9084-a9ca960b6855"]	127.0.0.1	2026-04-09 15:30:21.294623	2026-04-09 15:30:21.295744	2026-04-09 15:30:21.295744
675c0663-39b3-422e-993f-93f88cdddb2a	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	LOGIN	auth	用户登录成功	127.0.0.1	2026-04-09 18:38:07.835624	2026-04-09 18:38:07.837472	2026-04-09 18:38:07.837472
159b85ae-1a6b-4e33-9bb4-5eaefec20af3	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	LOGIN	auth	用户登录成功	127.0.0.1	2026-04-09 19:57:25.550788	2026-04-09 19:57:25.5622	2026-04-09 19:57:25.5622
2ec6f237-b4a7-482d-9d64-8c7f88538525	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	UPDATE	FactoryCalendar	["b01dbfa2-f835-41f8-b08a-9c91fb182554","白班","08:00:00","21:00:00",1,false]	127.0.0.1	2026-04-09 19:57:55.449035	2026-04-09 19:57:55.449035	2026-04-09 19:57:55.449035
5227725d-a8a4-4d9b-854b-f16247ac70eb	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	UPDATE	FactoryCalendar	["b01dbfa2-f835-41f8-b08a-9c91fb182554","白班","08:00:00","20:00:00",1,false]	127.0.0.1	2026-04-09 20:03:48.827328	2026-04-09 20:03:48.827328	2026-04-09 20:03:48.827328
611bee52-953b-4ff1-860a-cebe9398478f	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	UPDATE	FactoryCalendar	["b01dbfa2-f835-41f8-b08a-9c91fb182554","白班","08:00:00","20:01:00",1,false]	127.0.0.1	2026-04-09 20:03:59.209754	2026-04-09 20:03:59.21134	2026-04-09 20:03:59.21134
d73167d4-6767-4cfa-90a3-50c324302852	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	UPDATE	FactoryCalendar	["b01dbfa2-f835-41f8-b08a-9c91fb182554","白班","08:00:00","20:00:00",1,false]	127.0.0.1	2026-04-09 20:10:33.982694	2026-04-09 20:10:33.98328	2026-04-09 20:10:33.98328
faf27dec-4565-473c-b82f-83c7266957af	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	UPDATE	DictItem	["ea4b0469-b8cb-43a0-95e2-cf09beffa6f8","PENDING","待排产","PENDING","订单已创建，尚未排产",false,1,true]	127.0.0.1	2026-04-09 20:15:51.038372	2026-04-09 20:15:51.038372	2026-04-09 20:15:51.038372
3b667fae-be8a-4432-bef3-358e3604d0db	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	UPDATE	DictItem	["ea4b0469-b8cb-43a0-95e2-cf09beffa6f8","PENDING","待排产","PENDING","订单已创建，尚未排产",true,1,true]	127.0.0.1	2026-04-09 20:15:55.459042	2026-04-09 20:15:55.459668	2026-04-09 20:15:55.459668
b8f4b915-0e10-4a1b-b9f1-336b7a91c99f	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	CREATE	DictType	["CUSTYPE","自定义","",true,5]	127.0.0.1	2026-04-09 20:16:42.240292	2026-04-09 20:16:42.241924	2026-04-09 20:16:42.241924
ffbdd53c-da15-4028-8a0d-aa7e83379fbd	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	UPDATE	DictType	["dac94487-9571-4d80-8c0c-f85aa6853962","CUSTYPE","自定义","",true,6]	127.0.0.1	2026-04-09 20:16:58.56252	2026-04-09 20:16:58.56252	2026-04-09 20:16:58.56252
a1832238-8d4e-47a3-b75f-0246a86a3316	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	UPDATE	DictType	["dac94487-9571-4d80-8c0c-f85aa6853962","CUSTYPE","自定义","",true,7]	127.0.0.1	2026-04-09 20:17:08.579338	2026-04-09 20:17:08.579338	2026-04-09 20:17:08.579338
67f61d22-9408-4100-8f4f-3bf574d70102	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	CREATE	DictItem	["dac94487-9571-4d80-8c0c-f85aa6853962","01","开始","01","",true,0,false]	127.0.0.1	2026-04-09 20:17:30.633482	2026-04-09 20:17:30.634483	2026-04-09 20:17:30.634483
2dfcd577-5fc3-48f6-a473-f3aa6039b74a	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	UPDATE	DictItem	["1a158320-5e1e-4842-92b6-71239210139f","01","开始","01","",false,0,false]	127.0.0.1	2026-04-09 20:18:02.020074	2026-04-09 20:18:02.021589	2026-04-09 20:18:02.021589
371bfb0a-085e-4c5a-940e-500a81f585ba	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	UPDATE	DictItem	["1a158320-5e1e-4842-92b6-71239210139f","01","开始","01","",true,0,false]	127.0.0.1	2026-04-09 20:18:02.888201	2026-04-09 20:18:02.888201	2026-04-09 20:18:02.888201
179619fb-4e74-4777-a08c-742f81e48b82	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	LOGIN	auth	用户登录成功	127.0.0.1	2026-04-10 08:25:44.755027	2026-04-10 08:25:44.756753	2026-04-10 08:25:44.756753
3074f19f-9a3b-44f4-ae98-43cecb16f455	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	RESOURCE_CREATE	Resource	["ZS11","qqq","INJECTION_MACHINE","1c56f6a5-575e-4534-9e57-28444fb62f28",null,"","",null,"IDLE",null]	127.0.0.1	2026-04-10 09:12:21.132576	2026-04-10 09:12:21.132576	2026-04-10 09:12:21.132576
4cad8fe1-f570-4c36-b370-fd2a0054db17	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	LOGIN	auth	用户登录成功	127.0.0.1	2026-04-10 09:14:57.263164	2026-04-10 09:14:57.264648	2026-04-10 09:14:57.264648
f4104cbd-4aeb-4dc3-b198-d7a795f13808	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	LOGIN	auth	用户登录成功	127.0.0.1	2026-04-10 14:54:18.175962	2026-04-10 14:54:18.176968	2026-04-10 14:54:18.176968
f358217e-beee-4fd5-b94c-5debad69d080	\N	admin	LOGOUT	auth	用户登出	127.0.0.1	2026-04-10 15:06:50.134791	2026-04-10 15:06:50.137792	2026-04-10 15:06:50.137792
2dda5915-e7e1-4956-86b0-38c05ed59557	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	LOGIN	auth	用户登录成功	127.0.0.1	2026-04-10 15:06:57.224433	2026-04-10 15:06:57.224433	2026-04-10 15:06:57.224433
60e09512-fab3-4284-a212-1b455623f8cb	\N	admin	LOGOUT	auth	用户登出	127.0.0.1	2026-04-10 15:24:09.221736	2026-04-10 15:24:09.229308	2026-04-10 15:24:09.229308
bfe53a8d-7d4d-4ce1-823b-bdc76c0ae402	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	LOGIN	auth	用户登录成功	127.0.0.1	2026-04-10 15:24:15.633794	2026-04-10 15:24:15.633794	2026-04-10 15:24:15.633794
efddd70c-81ae-4aac-95a9-3382a3f1fb6c	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	CREATE	FactoryCalendar	["注塑机日历","ZS",2026,""]	127.0.0.1	2026-04-10 15:47:55.290192	2026-04-10 15:47:55.290192	2026-04-10 15:47:55.290192
54529a69-e028-4641-bbac-2213b8f88708	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	LOGIN	auth	用户登录成功	127.0.0.1	2026-04-10 20:20:53.993631	2026-04-10 20:20:54.000102	2026-04-10 20:20:54.000102
4c6a7001-beac-4949-9501-61f6a7e9d532	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	UPDATE	FactoryCalendar	["ef3ed8f9-4ffd-49cf-b45b-78e3893e9086","SINGLE"]	127.0.0.1	2026-04-10 20:21:35.576904	2026-04-10 20:21:35.57791	2026-04-10 20:21:35.57791
e414e4b6-fb4c-4c5b-864d-54056bf49ef1	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	LOGIN	auth	用户登录成功	127.0.0.1	2026-04-10 21:21:19.643525	2026-04-10 21:21:19.645527	2026-04-10 21:21:19.645527
31f12f52-9b1f-4c7f-b3a0-b595a4f7561d	\N	admin	LOGOUT	auth	用户登出	127.0.0.1	2026-04-10 21:23:10.070713	2026-04-10 21:23:10.070713	2026-04-10 21:23:10.070713
8352bba7-6d48-4585-8405-050926166cf8	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	LOGIN	auth	用户登录成功	127.0.0.1	2026-04-10 21:23:15.8508	2026-04-10 21:23:15.8508	2026-04-10 21:23:15.8508
ca1b49c1-b429-4820-aa7c-3a68e3a1ffff	\N	admin	LOGOUT	auth	用户登出	127.0.0.1	2026-04-10 21:29:58.34816	2026-04-10 21:29:58.34816	2026-04-10 21:29:58.34816
279d255c-849e-4c7d-8cb9-589f747f1a3c	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	LOGIN	auth	用户登录成功	127.0.0.1	2026-04-10 21:30:04.725243	2026-04-10 21:30:04.725243	2026-04-10 21:30:04.725243
367a3199-3647-4821-b1cb-b7441bafd974	\N	admin	LOGOUT	auth	用户登出	127.0.0.1	2026-04-10 21:31:56.293357	2026-04-10 21:31:56.298011	2026-04-10 21:31:56.298011
864468bd-209d-48bc-908b-2af1050557d8	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	LOGIN	auth	用户登录成功	127.0.0.1	2026-04-10 21:32:02.190587	2026-04-10 21:32:02.190587	2026-04-10 21:32:02.190587
f0c8361c-c06b-4502-8c13-c42bc2fcb552	\N	admin	LOGOUT	auth	用户登出	127.0.0.1	2026-04-10 21:35:02.989785	2026-04-10 21:35:02.996984	2026-04-10 21:35:02.996984
e8145220-16bd-4645-8b6e-6cab9fc34cfc	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	LOGIN	auth	用户登录成功	127.0.0.1	2026-04-10 21:35:12.517325	2026-04-10 21:35:12.541365	2026-04-10 21:35:12.541365
6d24aa6e-5096-4ebe-968c-be37294d5eb9	\N	admin	LOGOUT	auth	用户登出	127.0.0.1	2026-04-10 21:44:57.621666	2026-04-10 21:44:57.626266	2026-04-10 21:44:57.626266
bf241d45-6cf6-495a-a895-7da6a61f9ac9	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	LOGIN	auth	用户登录成功	127.0.0.1	2026-04-10 21:45:03.366073	2026-04-10 21:45:03.366073	2026-04-10 21:45:03.366073
2cf8affe-5823-4d85-8088-5761bb4a702e	\N	admin	LOGOUT	auth	用户登出	127.0.0.1	2026-04-10 21:49:59.639048	2026-04-10 21:49:59.642682	2026-04-10 21:49:59.642682
c1a087ca-b309-4d7b-b0bd-a5036373f27f	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	LOGIN	auth	用户登录成功	127.0.0.1	2026-04-10 21:50:06.789238	2026-04-10 21:50:06.789238	2026-04-10 21:50:06.789238
fa9b0694-de75-4aec-8c90-b748f421f492	cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	LOGIN	auth	用户登录成功	127.0.0.1	2026-04-11 07:44:58.778512	2026-04-11 07:44:58.780698	2026-04-11 07:44:58.780698
\.


--
-- TOC entry 3717 (class 0 OID 17404)
-- Dependencies: 231
-- Data for Name: calendar_dates; Type: TABLE DATA; Schema: public; Owner: aps_user
--

COPY public.calendar_dates (id, calendar_id, date, date_type, label, create_time, update_time) FROM stdin;
efea48cc-8178-42c6-824e-89997a255ff1	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-01-01	WORKDAY	\N	2026-04-09 09:05:55.498537	2026-04-09 09:05:55.498537
d592d90f-6308-484c-9407-b9866e0f2cc9	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-01-02	WORKDAY	\N	2026-04-09 09:05:55.498537	2026-04-09 09:05:55.498537
51c709f6-611b-4422-ab90-78f22a473a18	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-01-04	RESTDAY	\N	2026-04-09 09:05:55.498537	2026-04-09 09:05:55.498537
a0ddfb9b-6b84-4c94-b60a-51b13c9759d5	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-01-05	WORKDAY	\N	2026-04-09 09:05:55.498537	2026-04-09 09:05:55.498537
e8058696-39ea-46e3-8fc1-36961db6d307	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-01-06	WORKDAY	\N	2026-04-09 09:05:55.498537	2026-04-09 09:05:55.498537
7d343431-d4d6-4111-9814-8a14d5dadb9d	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-01-07	WORKDAY	\N	2026-04-09 09:05:55.499539	2026-04-09 09:05:55.499539
d0126462-bce5-46ae-855a-18ab81f4c217	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-01-08	WORKDAY	\N	2026-04-09 09:05:55.499539	2026-04-09 09:05:55.499539
791d129d-924d-4bc6-96bc-5fde5a41cf87	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-01-09	WORKDAY	\N	2026-04-09 09:05:55.499539	2026-04-09 09:05:55.499539
dfa93ae6-eb9a-40e4-a91e-04ade960ce23	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-01-11	RESTDAY	\N	2026-04-09 09:05:55.499539	2026-04-09 09:05:55.499539
ad3675da-7aac-4902-a463-3e3a0238a076	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-01-12	WORKDAY	\N	2026-04-09 09:05:55.499539	2026-04-09 09:05:55.499539
e4b0d9f2-81d6-46ed-9582-8df9892ff8bc	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-01-13	WORKDAY	\N	2026-04-09 09:05:55.499539	2026-04-09 09:05:55.499539
7d98373a-937c-461e-a782-f2a60e944842	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-01-14	WORKDAY	\N	2026-04-09 09:05:55.499539	2026-04-09 09:05:55.499539
ebc8b029-dbfb-493b-bc40-5118f91dda6a	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-01-15	WORKDAY	\N	2026-04-09 09:05:55.499539	2026-04-09 09:05:55.499539
eb08def9-a7be-45d5-a10a-0836e2b5d331	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-01-16	WORKDAY	\N	2026-04-09 09:05:55.499539	2026-04-09 09:05:55.499539
39a695c7-2bdd-423a-b8e8-adcd29413c7a	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-01-18	RESTDAY	\N	2026-04-09 09:05:55.499539	2026-04-09 09:05:55.499539
0141a406-c597-4b26-8fa4-6a1690c679c7	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-01-19	WORKDAY	\N	2026-04-09 09:05:55.500536	2026-04-09 09:05:55.500536
669f69bf-dcea-4b15-8133-c9d59afb465e	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-01-20	WORKDAY	\N	2026-04-09 09:05:55.500536	2026-04-09 09:05:55.500536
2eaa3c6b-a0bc-4036-ba4a-7568026a7107	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-01-21	WORKDAY	\N	2026-04-09 09:05:55.500536	2026-04-09 09:05:55.500536
5149aff6-2e21-4723-9370-224d1957c0cb	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-01-22	WORKDAY	\N	2026-04-09 09:05:55.500536	2026-04-09 09:05:55.500536
0c7dd307-652d-4ee0-8d67-a5d57765e274	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-01-23	WORKDAY	\N	2026-04-09 09:05:55.500536	2026-04-09 09:05:55.500536
c4b584c1-b0c6-4eb2-97b2-a2053bdb11d1	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-01-25	RESTDAY	\N	2026-04-09 09:05:55.500536	2026-04-09 09:05:55.500536
66c58fea-f569-439b-87f3-708e0591fb0b	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-01-26	WORKDAY	\N	2026-04-09 09:05:55.500536	2026-04-09 09:05:55.500536
fa7810b1-5496-4474-a4e7-447d93ed505d	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-01-27	WORKDAY	\N	2026-04-09 09:05:55.500536	2026-04-09 09:05:55.500536
6c87ff2b-d6c3-4c7f-8868-160204a53afe	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-01-28	WORKDAY	\N	2026-04-09 09:05:55.500536	2026-04-09 09:05:55.500536
6c2a8314-a3fe-439a-a1ca-b4a34b11f0fd	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-01-29	WORKDAY	\N	2026-04-09 09:05:55.500536	2026-04-09 09:05:55.500536
c8fb6442-72e2-4ace-82f4-3b9eb88dc701	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-01-30	WORKDAY	\N	2026-04-09 09:05:55.500536	2026-04-09 09:05:55.500536
f685b04f-b459-4995-a54a-a0b4f9f31947	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-02-01	RESTDAY	\N	2026-04-09 09:05:55.501535	2026-04-09 09:05:55.501535
11c2d52e-e3e6-43cc-9098-018a7c6e60f5	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-02-02	WORKDAY	\N	2026-04-09 09:05:55.501535	2026-04-09 09:05:55.501535
eb520aff-e7f2-4f5e-b1b3-45315d313b35	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-02-03	WORKDAY	\N	2026-04-09 09:05:55.501535	2026-04-09 09:05:55.501535
363d770e-4c66-401e-a479-94de5e257159	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-02-04	WORKDAY	\N	2026-04-09 09:05:55.501535	2026-04-09 09:05:55.501535
4cdf33bf-ed6c-4ac2-bd44-485914eba42c	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-02-05	WORKDAY	\N	2026-04-09 09:05:55.501535	2026-04-09 09:05:55.501535
c84b45b9-29b5-4794-bf4d-5ac4d12627c4	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-02-06	WORKDAY	\N	2026-04-09 09:05:55.501535	2026-04-09 09:05:55.501535
46025c76-6921-491b-8fd6-0e5bec5fee9f	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-02-08	RESTDAY	\N	2026-04-09 09:05:55.501535	2026-04-09 09:05:55.501535
c09affca-399c-4499-bcb1-21fff7a1a1db	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-02-09	WORKDAY	\N	2026-04-09 09:05:55.501535	2026-04-09 09:05:55.501535
03f84066-c2be-4890-9860-bc4adc39fd3f	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-02-10	WORKDAY	\N	2026-04-09 09:05:55.501535	2026-04-09 09:05:55.501535
1a830bc1-13b2-4544-89e6-d7ac4c82bb1e	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-02-11	WORKDAY	\N	2026-04-09 09:05:55.501535	2026-04-09 09:05:55.501535
0b360f06-4b5a-41e2-a8e9-42d357b22ca3	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-02-12	WORKDAY	\N	2026-04-09 09:05:55.501535	2026-04-09 09:05:55.501535
8bb38d83-103f-42de-bf94-f6d86557b4ef	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-02-13	WORKDAY	\N	2026-04-09 09:05:55.502537	2026-04-09 09:05:55.502537
18c17ae2-f610-4d1a-b977-9621ba0e56e6	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-02-15	RESTDAY	\N	2026-04-09 09:05:55.502537	2026-04-09 09:05:55.502537
fb4c3e89-4034-4f96-9a78-d0d9ba1593d0	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-02-16	WORKDAY	\N	2026-04-09 09:05:55.502537	2026-04-09 09:05:55.502537
a093487d-42e9-4ae8-8a67-e4ef57c26836	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-02-17	WORKDAY	\N	2026-04-09 09:05:55.502537	2026-04-09 09:05:55.502537
8d0cc520-f623-4627-8797-76e84ee2fb5e	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-02-18	WORKDAY	\N	2026-04-09 09:05:55.502537	2026-04-09 09:05:55.502537
198ba8ec-82b4-4b04-a345-21592648bf13	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-02-19	WORKDAY	\N	2026-04-09 09:05:55.502537	2026-04-09 09:05:55.502537
a9f7a0c9-53b7-43bf-a8e2-0d624d4d7b63	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-02-20	WORKDAY	\N	2026-04-09 09:05:55.502537	2026-04-09 09:05:55.502537
f3cc074d-291a-447f-ac2c-c637a1682ab3	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-02-22	RESTDAY	\N	2026-04-09 09:05:55.502537	2026-04-09 09:05:55.502537
ac02e55d-cf84-47e4-892c-f4d471b7f41c	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-02-23	WORKDAY	\N	2026-04-09 09:05:55.502537	2026-04-09 09:05:55.502537
fc42666c-7c45-4b30-a586-5b339ae48e8f	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-02-24	WORKDAY	\N	2026-04-09 09:05:55.502537	2026-04-09 09:05:55.502537
75e8fe54-8453-455d-bf8e-d9f67e53d500	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-02-25	WORKDAY	\N	2026-04-09 09:05:55.502537	2026-04-09 09:05:55.502537
2a6800bd-8db6-400a-ba5a-ca2a6c195809	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-02-26	WORKDAY	\N	2026-04-09 09:05:55.502537	2026-04-09 09:05:55.502537
d764b5cb-f668-45ea-89b1-3915c3047a54	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-02-27	WORKDAY	\N	2026-04-09 09:05:55.502537	2026-04-09 09:05:55.502537
c791e9dc-c99a-4a68-9d82-123f98cd71eb	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-03-01	RESTDAY	\N	2026-04-09 09:05:55.502537	2026-04-09 09:05:55.502537
e4efefe2-4883-451e-9574-21de9f001709	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-03-02	WORKDAY	\N	2026-04-09 09:05:55.502537	2026-04-09 09:05:55.502537
3fb7f1a6-6d8e-49a6-b039-043c4837d423	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-03-03	WORKDAY	\N	2026-04-09 09:05:55.502537	2026-04-09 09:05:55.502537
611b68e1-1793-4888-a86d-c501286ec19a	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-03-04	WORKDAY	\N	2026-04-09 09:05:55.503536	2026-04-09 09:05:55.503536
3e089231-6d85-4511-942b-8ffa48cc46da	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-03-05	WORKDAY	\N	2026-04-09 09:05:55.503536	2026-04-09 09:05:55.503536
ade50df9-5e08-4316-aaba-53c97ac993b2	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-03-06	WORKDAY	\N	2026-04-09 09:05:55.503536	2026-04-09 09:05:55.503536
18958930-84ef-4aa4-b087-9ee87007f112	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-03-08	RESTDAY	\N	2026-04-09 09:05:55.503536	2026-04-09 09:05:55.503536
b6ec206f-abc7-4476-b320-a1a016cd46b0	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-03-09	WORKDAY	\N	2026-04-09 09:05:55.503536	2026-04-09 09:05:55.503536
3886cc2f-5a54-4221-8e5a-5f3d7f22aecd	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-03-10	WORKDAY	\N	2026-04-09 09:05:55.503536	2026-04-09 09:05:55.503536
bbf92f42-51af-4aac-b57a-1ac87176daf1	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-03-11	WORKDAY	\N	2026-04-09 09:05:55.503536	2026-04-09 09:05:55.503536
ef671022-c506-4d06-a6d7-41819b8de262	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-03-12	WORKDAY	\N	2026-04-09 09:05:55.504048	2026-04-09 09:05:55.504048
21d67cfe-06aa-4ddb-bdf4-8de0bcee30c3	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-03-13	WORKDAY	\N	2026-04-09 09:05:55.504048	2026-04-09 09:05:55.504048
d4f1834a-4e47-44bd-a125-463d241ab586	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-03-15	RESTDAY	\N	2026-04-09 09:05:55.50472	2026-04-09 09:05:55.50472
3c807035-ad7c-4c35-8f02-49cbdaee64fd	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-03-16	WORKDAY	\N	2026-04-09 09:05:55.50472	2026-04-09 09:05:55.50472
2f028de3-fe61-47a3-99ae-609aa3a7c3a7	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-03-17	WORKDAY	\N	2026-04-09 09:05:55.50472	2026-04-09 09:05:55.50472
b13ee5f3-18c7-422f-b8cb-a8b43a7d4d9c	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-03-18	WORKDAY	\N	2026-04-09 09:05:55.50472	2026-04-09 09:05:55.50472
5fcf53b3-60be-47d4-b82b-781c0ce3e628	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-03-19	WORKDAY	\N	2026-04-09 09:05:55.50472	2026-04-09 09:05:55.50472
27bca6ed-11ee-4b43-bb8d-69a57bec3562	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-03-20	WORKDAY	\N	2026-04-09 09:05:55.50472	2026-04-09 09:05:55.50472
21ef0372-6378-4ef8-b19e-8cf596d9314a	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-03-22	RESTDAY	\N	2026-04-09 09:05:55.50472	2026-04-09 09:05:55.50472
1af57412-bce6-4ab7-ae15-e5903ec7a656	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-03-23	WORKDAY	\N	2026-04-09 09:05:55.50472	2026-04-09 09:05:55.50472
bd60bfce-a3fe-4289-867c-2af403838fc7	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-03-24	WORKDAY	\N	2026-04-09 09:05:55.50472	2026-04-09 09:05:55.50472
92aafcea-2928-457b-8e6d-3e7b780bba46	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-03-25	WORKDAY	\N	2026-04-09 09:05:55.50472	2026-04-09 09:05:55.50472
5cf6db5d-70b3-46dc-9fdf-5ccc8ab6ec48	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-03-26	WORKDAY	\N	2026-04-09 09:05:55.50472	2026-04-09 09:05:55.50472
f68d2ac6-3396-4c18-b6b1-24491dd18ecf	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-03-27	WORKDAY	\N	2026-04-09 09:05:55.505512	2026-04-09 09:05:55.505512
583db217-5b17-4fc3-9b5b-6105272714f0	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-03-29	RESTDAY	\N	2026-04-09 09:05:55.505512	2026-04-09 09:05:55.505512
2abba281-e6e5-4bca-a510-0ed517acf566	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-03-30	WORKDAY	\N	2026-04-09 09:05:55.505512	2026-04-09 09:05:55.505512
56e3e015-2805-4b4a-a6c7-cccaf9a863f9	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-03-31	WORKDAY	\N	2026-04-09 09:05:55.505512	2026-04-09 09:05:55.505512
874f7557-5d11-46c8-9a41-13ba9810d29b	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-04-01	WORKDAY	\N	2026-04-09 09:05:55.505512	2026-04-09 09:05:55.505512
29ea75c5-659d-44f9-836f-7d775597d360	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-04-02	WORKDAY	\N	2026-04-09 09:05:55.505512	2026-04-09 09:05:55.505512
576bf2f9-aac8-443c-9823-aae39db02265	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-04-03	WORKDAY	\N	2026-04-09 09:05:55.505512	2026-04-09 09:05:55.505512
4dcc64ee-f81e-47dd-822b-d74f92b64279	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-04-05	RESTDAY	\N	2026-04-09 09:05:55.505512	2026-04-09 09:05:55.505512
1700babe-c202-4769-a2a3-1c05362f7c14	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-04-06	WORKDAY	\N	2026-04-09 09:05:55.505512	2026-04-09 09:05:55.505512
dc89a20f-8559-4545-9981-8924d01b2f31	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-04-07	WORKDAY	\N	2026-04-09 09:05:55.505512	2026-04-09 09:05:55.505512
2342480f-e853-4d58-b6cb-d144fc2673c0	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-04-08	WORKDAY	\N	2026-04-09 09:05:55.505512	2026-04-09 09:05:55.505512
eed21c42-719c-4138-be1d-5b9814140402	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-04-09	WORKDAY	\N	2026-04-09 09:05:55.505512	2026-04-09 09:05:55.505512
b14e6f43-16c4-4b70-8b4d-599352813426	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-04-10	WORKDAY	\N	2026-04-09 09:05:55.505512	2026-04-09 09:05:55.505512
5afe924e-f597-43a7-b63c-511384290e16	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-04-12	RESTDAY	\N	2026-04-09 09:05:55.505512	2026-04-09 09:05:55.505512
1706ad3f-a731-40a6-98be-94d97c081098	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-04-13	WORKDAY	\N	2026-04-09 09:05:55.505512	2026-04-09 09:05:55.505512
f90fd5ef-71be-455a-8e45-8247c53132df	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-04-14	WORKDAY	\N	2026-04-09 09:05:55.505512	2026-04-09 09:05:55.505512
6aff5018-c24f-422b-85fd-df93b9e5ca10	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-04-15	WORKDAY	\N	2026-04-09 09:05:55.505512	2026-04-09 09:05:55.505512
8a833e6f-3f2e-4879-a192-3d9accf3a3c3	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-04-16	WORKDAY	\N	2026-04-09 09:05:55.505512	2026-04-09 09:05:55.505512
63d898cf-61b1-4523-9326-d6a01d82c51d	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-04-17	WORKDAY	\N	2026-04-09 09:05:55.505512	2026-04-09 09:05:55.505512
df57f24e-d302-4846-9df9-30bd00887ffe	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-04-19	RESTDAY	\N	2026-04-09 09:05:55.506731	2026-04-09 09:05:55.506731
686b657a-62d8-4b5d-b27f-ff1a2ee53dc7	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-04-20	WORKDAY	\N	2026-04-09 09:05:55.506731	2026-04-09 09:05:55.506731
23f03c43-a0a9-45ad-9acb-481ff127cba9	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-04-21	WORKDAY	\N	2026-04-09 09:05:55.506731	2026-04-09 09:05:55.506731
5ae53c4a-d421-46a2-a0f2-f77d59da1020	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-04-22	WORKDAY	\N	2026-04-09 09:05:55.506731	2026-04-09 09:05:55.506731
3ce30703-a2f3-475d-b60d-abb20aaee760	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-04-23	WORKDAY	\N	2026-04-09 09:05:55.506731	2026-04-09 09:05:55.506731
89cc0063-2f7b-4e0c-9bee-3c44d2b10044	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-04-24	WORKDAY	\N	2026-04-09 09:05:55.506731	2026-04-09 09:05:55.506731
5e3112cd-e864-4634-b880-6d04e1c87bb6	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-04-26	RESTDAY	\N	2026-04-09 09:05:55.506731	2026-04-09 09:05:55.506731
026ae982-4642-4e9c-b38d-c676ae9dd66c	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-04-27	WORKDAY	\N	2026-04-09 09:05:55.509245	2026-04-09 09:05:55.509245
4ef93d4e-7cce-4895-833e-3b681519e8b0	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-04-28	WORKDAY	\N	2026-04-09 09:05:55.510244	2026-04-09 09:05:55.510244
848e3cd9-f000-4338-b7cf-adff624d1fc7	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-04-29	WORKDAY	\N	2026-04-09 09:05:55.510244	2026-04-09 09:05:55.510244
e7b02687-011f-49c0-9be3-09deeb0653b5	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-04-30	WORKDAY	\N	2026-04-09 09:05:55.510244	2026-04-09 09:05:55.510244
fb8e5492-5359-4c9c-b3a8-be1566dac5b8	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-05-01	WORKDAY	\N	2026-04-09 09:05:55.510244	2026-04-09 09:05:55.510244
50d34d4b-0377-40ea-9c97-d00a0bb2e02d	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-05-03	RESTDAY	\N	2026-04-09 09:05:55.51075	2026-04-09 09:05:55.51075
2f9ec69f-6f9c-47b0-9f9e-26f685caa0a6	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-05-04	WORKDAY	\N	2026-04-09 09:05:55.51075	2026-04-09 09:05:55.51075
08094887-faf7-40a6-9413-0eefd0e138c1	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-05-05	WORKDAY	\N	2026-04-09 09:05:55.51075	2026-04-09 09:05:55.51075
221c6fab-6b82-4aee-8944-e39eacd189bf	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-05-06	WORKDAY	\N	2026-04-09 09:05:55.51075	2026-04-09 09:05:55.51075
44746f25-1ee3-4af1-aeb8-61ea732d193f	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-05-07	WORKDAY	\N	2026-04-09 09:05:55.51075	2026-04-09 09:05:55.51075
612a09e7-7694-4963-bb5c-ae3aaee8a24d	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-05-08	WORKDAY	\N	2026-04-09 09:05:55.51075	2026-04-09 09:05:55.51075
0273392c-d001-4540-85e5-30ee565de7a0	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-05-10	RESTDAY	\N	2026-04-09 09:05:55.511092	2026-04-09 09:05:55.511092
a4c7ace0-080b-498d-b7da-49d50a8a6460	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-05-11	WORKDAY	\N	2026-04-09 09:05:55.511092	2026-04-09 09:05:55.511092
607b7283-8f0b-44dd-a005-2a4e1cbff83c	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-05-12	WORKDAY	\N	2026-04-09 09:05:55.511092	2026-04-09 09:05:55.511092
4feb7927-b41a-4c9e-ad58-5a6537414e81	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-05-13	WORKDAY	\N	2026-04-09 09:05:55.511092	2026-04-09 09:05:55.511092
4ff1cbcf-5e37-4780-b80f-55132725a81b	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-05-14	WORKDAY	\N	2026-04-09 09:05:55.511092	2026-04-09 09:05:55.511092
7388eebb-f6a9-405d-9585-476b93b0d699	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-05-15	WORKDAY	\N	2026-04-09 09:05:55.511092	2026-04-09 09:05:55.511092
3688f918-6227-43db-8c52-ff9e4b4d8e62	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-05-17	RESTDAY	\N	2026-04-09 09:05:55.511597	2026-04-09 09:05:55.511597
9bdcb281-ec79-409d-881b-ed152034730e	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-05-18	WORKDAY	\N	2026-04-09 09:05:55.511597	2026-04-09 09:05:55.511597
50bf99ac-471e-4970-9ea3-f82c9a46a590	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-05-19	WORKDAY	\N	2026-04-09 09:05:55.511597	2026-04-09 09:05:55.511597
20b1c501-b951-45c4-951c-ca92a922176f	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-05-20	WORKDAY	\N	2026-04-09 09:05:55.511597	2026-04-09 09:05:55.511597
2e0fe87f-180b-44cc-91d4-6ebbf2616b69	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-05-21	WORKDAY	\N	2026-04-09 09:05:55.511597	2026-04-09 09:05:55.511597
01ac81fb-c0cc-4013-a56a-c1a4b118e3dd	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-05-22	WORKDAY	\N	2026-04-09 09:05:55.511597	2026-04-09 09:05:55.511597
1bf3a7c5-aad6-4b47-9792-5c4cd6b493ad	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-05-24	RESTDAY	\N	2026-04-09 09:05:55.511597	2026-04-09 09:05:55.511597
a9a93493-c0f5-4326-b68a-76de351b1d05	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-05-25	WORKDAY	\N	2026-04-09 09:05:55.511597	2026-04-09 09:05:55.511597
5a087ef7-745c-4142-83c1-8e1f34af2238	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-05-26	WORKDAY	\N	2026-04-09 09:05:55.511597	2026-04-09 09:05:55.511597
b432a865-eee8-48b6-867d-fb537723f119	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-05-27	WORKDAY	\N	2026-04-09 09:05:55.511597	2026-04-09 09:05:55.511597
b353956d-2eb7-4c08-951e-ae33ae35f2e1	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-05-28	WORKDAY	\N	2026-04-09 09:05:55.511597	2026-04-09 09:05:55.511597
5dfc6e8c-21b1-41c6-aa4b-795b03aeac74	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-05-29	WORKDAY	\N	2026-04-09 09:05:55.511597	2026-04-09 09:05:55.511597
d41820af-e955-4900-9712-a7338133f46e	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-05-31	RESTDAY	\N	2026-04-09 09:05:55.512607	2026-04-09 09:05:55.512607
af0f819e-ae2d-42e1-8632-36baa3d6e68e	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-06-01	WORKDAY	\N	2026-04-09 09:05:55.512607	2026-04-09 09:05:55.512607
0822c765-6f13-4e20-bd6d-0870710ea5e3	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-06-02	WORKDAY	\N	2026-04-09 09:05:55.512607	2026-04-09 09:05:55.512607
33439f1a-fe6b-49f9-80d4-25f3a15f610a	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-06-03	WORKDAY	\N	2026-04-09 09:05:55.512607	2026-04-09 09:05:55.512607
114d7ce2-f69a-4f62-b7ea-7078f1e7a6c7	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-06-04	WORKDAY	\N	2026-04-09 09:05:55.512607	2026-04-09 09:05:55.512607
5eccb0de-5706-4d5e-8c2d-804537cd3657	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-06-05	WORKDAY	\N	2026-04-09 09:05:55.512607	2026-04-09 09:05:55.512607
611f5b98-49f5-4073-9c5d-339d2c096fd1	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-06-07	RESTDAY	\N	2026-04-09 09:05:55.512607	2026-04-09 09:05:55.512607
d9f425ac-274c-41a9-9c87-5b267c7c4a2f	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-06-08	WORKDAY	\N	2026-04-09 09:05:55.512607	2026-04-09 09:05:55.512607
471ee48f-98da-4d5e-995b-71a93b42bd64	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-06-09	WORKDAY	\N	2026-04-09 09:05:55.513146	2026-04-09 09:05:55.513146
36343e84-4594-439d-b095-86524ef3c9ec	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-06-10	WORKDAY	\N	2026-04-09 09:05:55.513146	2026-04-09 09:05:55.513146
3b96ecab-a717-441d-8dd3-493842332c72	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-06-11	WORKDAY	\N	2026-04-09 09:05:55.513146	2026-04-09 09:05:55.513146
68e33ca7-aeec-493a-ae7c-8fd8d35f5677	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-06-12	WORKDAY	\N	2026-04-09 09:05:55.513146	2026-04-09 09:05:55.513146
c54be8fe-dbb9-433b-a5f4-f1396f77fad4	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-06-14	RESTDAY	\N	2026-04-09 09:05:55.513146	2026-04-09 09:05:55.513146
cd64d59e-1b4b-411a-8136-7872cabe9186	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-06-15	WORKDAY	\N	2026-04-09 09:05:55.513146	2026-04-09 09:05:55.513146
30c08e71-e4f0-4bf5-a60d-2d84c0ca648a	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-06-16	WORKDAY	\N	2026-04-09 09:05:55.513146	2026-04-09 09:05:55.513146
650e19cf-77ce-4f4a-9ee4-8f913e6870f6	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-06-17	WORKDAY	\N	2026-04-09 09:05:55.513146	2026-04-09 09:05:55.513146
b837e46e-c3c5-4389-ad7d-788fb301b896	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-06-18	WORKDAY	\N	2026-04-09 09:05:55.513146	2026-04-09 09:05:55.513146
bd22ebe1-9e71-4db9-a870-575d2da94e5f	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-06-19	WORKDAY	\N	2026-04-09 09:05:55.513146	2026-04-09 09:05:55.513146
6e4a1e2d-193b-4061-b059-c7fb0418add9	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-06-21	RESTDAY	\N	2026-04-09 09:05:55.513654	2026-04-09 09:05:55.513654
35e63ef6-c7bc-440c-82a0-8b29de170c2c	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-06-22	WORKDAY	\N	2026-04-09 09:05:55.513654	2026-04-09 09:05:55.513654
94465be4-d05a-40f3-abdc-d1047d22152f	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-06-23	WORKDAY	\N	2026-04-09 09:05:55.513654	2026-04-09 09:05:55.513654
1614c44f-4028-494a-aab8-74f8d2b3ee3d	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-06-24	WORKDAY	\N	2026-04-09 09:05:55.513654	2026-04-09 09:05:55.513654
d74ed327-14c8-4d0c-8ef4-8a8ba45a2eda	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-06-25	WORKDAY	\N	2026-04-09 09:05:55.513654	2026-04-09 09:05:55.513654
411ca85a-58ed-4e9b-aec5-56f0e3a87e41	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-06-26	WORKDAY	\N	2026-04-09 09:05:55.513654	2026-04-09 09:05:55.513654
31363c37-a213-4435-bd9e-1fe7f227826c	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-06-28	RESTDAY	\N	2026-04-09 09:05:55.513654	2026-04-09 09:05:55.513654
7bf0736b-5e86-4398-914a-4431f717b3c5	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-06-29	WORKDAY	\N	2026-04-09 09:05:55.514177	2026-04-09 09:05:55.514177
fe65ad27-e5e3-4fda-9c38-8f788c8c7f54	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-06-30	WORKDAY	\N	2026-04-09 09:05:55.514177	2026-04-09 09:05:55.514177
cce0c9db-e2b3-45f5-a0a7-09cbc02c55df	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-07-01	WORKDAY	\N	2026-04-09 09:05:55.514177	2026-04-09 09:05:55.514177
beef0992-41a2-449f-bb7f-c629ad361793	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-07-02	WORKDAY	\N	2026-04-09 09:05:55.514177	2026-04-09 09:05:55.514177
1eb7731f-0ad3-42be-96ad-5586f3fa7c33	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-07-03	WORKDAY	\N	2026-04-09 09:05:55.514177	2026-04-09 09:05:55.514177
d3e31786-1da1-44a6-80fc-978f69e57e3f	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-07-05	RESTDAY	\N	2026-04-09 09:05:55.514177	2026-04-09 09:05:55.514177
b2ed977d-fc6f-4063-9596-9a761f08dada	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-07-06	WORKDAY	\N	2026-04-09 09:05:55.514177	2026-04-09 09:05:55.514177
f27de67c-d3ad-47f3-a3e7-83b5d6c4ab0c	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-07-07	WORKDAY	\N	2026-04-09 09:05:55.514177	2026-04-09 09:05:55.514177
3c6f15d2-2e08-4fd2-bbcf-1ceb4d2cb81d	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-07-08	WORKDAY	\N	2026-04-09 09:05:55.514177	2026-04-09 09:05:55.514177
39ca27ee-db40-48d7-b61a-c8f68f56c3e2	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-07-09	WORKDAY	\N	2026-04-09 09:05:55.514177	2026-04-09 09:05:55.514177
0bf66124-1d74-4673-8bfb-a151af94e656	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-07-10	WORKDAY	\N	2026-04-09 09:05:55.514177	2026-04-09 09:05:55.514177
a87d7b0c-e22c-4289-a396-2a16215b95b3	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-07-12	RESTDAY	\N	2026-04-09 09:05:55.514177	2026-04-09 09:05:55.514177
26161bca-beb7-4a12-a975-2336410bda8d	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-07-13	WORKDAY	\N	2026-04-09 09:05:55.514177	2026-04-09 09:05:55.514177
bf338257-05b6-45d2-a7b6-759a6a06b293	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-07-14	WORKDAY	\N	2026-04-09 09:05:55.514177	2026-04-09 09:05:55.514177
726fc727-adbe-4823-b15f-9c54285cfdc4	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-07-15	WORKDAY	\N	2026-04-09 09:05:55.514177	2026-04-09 09:05:55.514177
6ca23a31-95ac-42b1-8138-28e7c0efe062	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-07-16	WORKDAY	\N	2026-04-09 09:05:55.514177	2026-04-09 09:05:55.514177
02abca84-f76f-4330-ae51-3bf0bd19f13f	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-07-17	WORKDAY	\N	2026-04-09 09:05:55.514177	2026-04-09 09:05:55.514177
50ac5e40-a223-4c82-b361-298f60ad1070	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-07-19	RESTDAY	\N	2026-04-09 09:05:55.514177	2026-04-09 09:05:55.514177
2cece98b-28fd-47c3-af73-d5158b21124a	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-07-20	WORKDAY	\N	2026-04-09 09:05:55.514177	2026-04-09 09:05:55.514177
7710f436-6a27-44ab-9e11-0e423f2f9de3	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-07-21	WORKDAY	\N	2026-04-09 09:05:55.514177	2026-04-09 09:05:55.514177
388b28e2-1bc3-43b9-ab99-6daab49799f8	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-07-22	WORKDAY	\N	2026-04-09 09:05:55.514177	2026-04-09 09:05:55.514177
a4aadacf-0a3a-427d-ba56-9a67b65dd85d	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-07-23	WORKDAY	\N	2026-04-09 09:05:55.514177	2026-04-09 09:05:55.514177
70326dcd-2328-440c-94e4-506fb7ff3569	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-07-24	WORKDAY	\N	2026-04-09 09:05:55.514177	2026-04-09 09:05:55.514177
0223ff1f-7282-4cb2-888e-6f9e7d0c0d46	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-07-26	RESTDAY	\N	2026-04-09 09:05:55.514177	2026-04-09 09:05:55.514177
3fe1bc4f-007b-4746-b1c6-64aa30a6861e	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-07-27	WORKDAY	\N	2026-04-09 09:05:55.514177	2026-04-09 09:05:55.514177
7b45a466-591b-497b-8187-ed294f64b570	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-07-28	WORKDAY	\N	2026-04-09 09:05:55.514177	2026-04-09 09:05:55.514177
f3019693-561e-4b60-9b7f-8cb09cceeac5	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-07-29	WORKDAY	\N	2026-04-09 09:05:55.514177	2026-04-09 09:05:55.514177
952747f4-d96a-4981-8c55-04f8c8e4a94c	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-07-30	WORKDAY	\N	2026-04-09 09:05:55.514177	2026-04-09 09:05:55.514177
0f66dbbd-f5cb-421c-9b90-a6b3ee174b1d	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-07-31	WORKDAY	\N	2026-04-09 09:05:55.514177	2026-04-09 09:05:55.514177
a605e009-aa89-4016-bb68-1df1578c3031	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-08-02	RESTDAY	\N	2026-04-09 09:05:55.5157	2026-04-09 09:05:55.5157
90238dde-4b81-437d-a89e-3e3a4eb9fe86	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-08-03	WORKDAY	\N	2026-04-09 09:05:55.5157	2026-04-09 09:05:55.5157
ff074321-9532-41e6-885a-1783c9c019f6	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-08-04	WORKDAY	\N	2026-04-09 09:05:55.5157	2026-04-09 09:05:55.5157
9b2771d5-6a7a-467c-838d-4fec087de79a	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-08-05	WORKDAY	\N	2026-04-09 09:05:55.5157	2026-04-09 09:05:55.5157
ffe28dd3-388d-4451-af4e-70dc8d9f352b	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-08-06	WORKDAY	\N	2026-04-09 09:05:55.5157	2026-04-09 09:05:55.5157
d61f3dea-7852-49e5-b2f3-95375fa82701	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-08-07	WORKDAY	\N	2026-04-09 09:05:55.5157	2026-04-09 09:05:55.5157
f40cba96-5e21-401f-8e65-8398fa4549e9	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-08-09	RESTDAY	\N	2026-04-09 09:05:55.516072	2026-04-09 09:05:55.516072
40e35457-a069-4bbe-9e2d-9147a3539756	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-08-10	WORKDAY	\N	2026-04-09 09:05:55.516072	2026-04-09 09:05:55.516072
d9c6201c-b9bb-44e3-8496-5f6911c69e49	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-08-11	WORKDAY	\N	2026-04-09 09:05:55.516072	2026-04-09 09:05:55.516072
acff2c33-a7b9-45b2-8c0f-37bbf012776c	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-08-12	WORKDAY	\N	2026-04-09 09:05:55.516072	2026-04-09 09:05:55.516072
6adb44a3-bf32-460a-a3ac-d16d3b48ad67	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-08-13	WORKDAY	\N	2026-04-09 09:05:55.516072	2026-04-09 09:05:55.516072
69b199f6-a83a-4846-a066-2b03068f5700	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-08-14	WORKDAY	\N	2026-04-09 09:05:55.516072	2026-04-09 09:05:55.516072
7a4fc88b-7783-499b-af8f-c9aa2b550dcb	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-08-16	RESTDAY	\N	2026-04-09 09:05:55.516072	2026-04-09 09:05:55.516072
fab3a48d-9815-46b6-86e2-39a84232f06b	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-08-17	WORKDAY	\N	2026-04-09 09:05:55.516072	2026-04-09 09:05:55.516072
74506ed0-77e6-4e4b-8313-6e4c387e1dba	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-08-18	WORKDAY	\N	2026-04-09 09:05:55.516072	2026-04-09 09:05:55.516072
b28ae15b-0c05-45e3-b6af-129958d6b60a	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-08-19	WORKDAY	\N	2026-04-09 09:05:55.516072	2026-04-09 09:05:55.516072
3a1b5822-4072-4a65-be72-d24896a8eb32	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-08-20	WORKDAY	\N	2026-04-09 09:05:55.516072	2026-04-09 09:05:55.516072
69ef6708-ec89-4dd8-bc2e-0663df9eec06	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-08-21	WORKDAY	\N	2026-04-09 09:05:55.516712	2026-04-09 09:05:55.516712
bda4f97f-9e2e-45e3-8730-e0be01fdb2be	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-08-23	RESTDAY	\N	2026-04-09 09:05:55.516712	2026-04-09 09:05:55.516712
0640f628-f40b-4823-8bc9-ef8ef1edb93d	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-08-24	WORKDAY	\N	2026-04-09 09:05:55.516712	2026-04-09 09:05:55.516712
c2a379a1-10c5-41e8-aa56-689be6c9ccef	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-08-25	WORKDAY	\N	2026-04-09 09:05:55.516712	2026-04-09 09:05:55.516712
5120d2e6-ebb5-4edd-a0b8-c8784c340002	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-08-26	WORKDAY	\N	2026-04-09 09:05:55.516712	2026-04-09 09:05:55.516712
9e2a02e3-9412-401b-a9f9-d3e6df703b56	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-08-27	WORKDAY	\N	2026-04-09 09:05:55.516712	2026-04-09 09:05:55.516712
28b4429d-a6d5-4b15-97b9-12c784807b1d	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-08-28	WORKDAY	\N	2026-04-09 09:05:55.516712	2026-04-09 09:05:55.516712
e7035e75-df3c-4efd-a1cc-28610eb27c15	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-08-30	RESTDAY	\N	2026-04-09 09:05:55.516712	2026-04-09 09:05:55.516712
5b34afcc-ba38-4bef-ba1d-ddf50c513c0f	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-08-31	WORKDAY	\N	2026-04-09 09:05:55.517711	2026-04-09 09:05:55.517711
ec3e3fed-084b-4f88-8041-04deccf69656	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-09-01	WORKDAY	\N	2026-04-09 09:05:55.517711	2026-04-09 09:05:55.517711
9aeffe69-e2e4-4dc6-a239-8ad4fe3f8f81	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-09-02	WORKDAY	\N	2026-04-09 09:05:55.517711	2026-04-09 09:05:55.517711
ec24bc84-3f0b-428f-8b41-4921e9063b34	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-09-03	WORKDAY	\N	2026-04-09 09:05:55.517711	2026-04-09 09:05:55.517711
1356e33e-7d70-4556-a94f-c62747fdc693	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-09-04	WORKDAY	\N	2026-04-09 09:05:55.517711	2026-04-09 09:05:55.517711
8d5f92d6-b6bc-4be4-93a9-9f7124c3eac9	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-09-06	RESTDAY	\N	2026-04-09 09:05:55.517711	2026-04-09 09:05:55.517711
bf3bad89-62b9-4040-be49-ba32e4dac169	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-09-07	WORKDAY	\N	2026-04-09 09:05:55.517711	2026-04-09 09:05:55.517711
4f6ac149-58c1-44a6-8f16-8a1547b17dc8	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-09-08	WORKDAY	\N	2026-04-09 09:05:55.517711	2026-04-09 09:05:55.517711
d46f846a-217f-4d78-8b12-7650e220984c	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-09-09	WORKDAY	\N	2026-04-09 09:05:55.517711	2026-04-09 09:05:55.517711
968b6fa5-951b-4cce-ae76-266ad4a8c540	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-09-10	WORKDAY	\N	2026-04-09 09:05:55.517711	2026-04-09 09:05:55.517711
d68f523d-57d3-4601-b446-a74154cb16f7	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-09-11	WORKDAY	\N	2026-04-09 09:05:55.517711	2026-04-09 09:05:55.517711
f61c8af1-466a-4ea5-880c-e4eba9712fdd	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-09-13	RESTDAY	\N	2026-04-09 09:05:55.517711	2026-04-09 09:05:55.517711
fedd9ed9-4239-424f-a7a3-dce2c9a22f66	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-09-14	WORKDAY	\N	2026-04-09 09:05:55.517711	2026-04-09 09:05:55.517711
59bde8aa-bc51-4bb5-b466-fcad6a8e89f9	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-09-15	WORKDAY	\N	2026-04-09 09:05:55.517711	2026-04-09 09:05:55.517711
093f23e2-c9ad-4e92-9074-4fff1cccd9e6	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-09-16	WORKDAY	\N	2026-04-09 09:05:55.517711	2026-04-09 09:05:55.517711
9e5ae0b9-8378-410d-91f0-cb98142c5817	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-09-17	WORKDAY	\N	2026-04-09 09:05:55.517711	2026-04-09 09:05:55.517711
74606e1b-0702-46ed-bb49-f500280ac30e	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-09-18	WORKDAY	\N	2026-04-09 09:05:55.517711	2026-04-09 09:05:55.517711
0d830c42-9f07-4d79-bb11-3f92e4dc9565	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-09-20	RESTDAY	\N	2026-04-09 09:05:55.517711	2026-04-09 09:05:55.517711
8c9bf818-4c66-4774-96a6-e162b3dac4dc	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-09-21	WORKDAY	\N	2026-04-09 09:05:55.517711	2026-04-09 09:05:55.517711
965f7b49-20cf-488a-a664-9a2c3939f3c0	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-09-22	WORKDAY	\N	2026-04-09 09:05:55.517711	2026-04-09 09:05:55.517711
b4bb0a1f-3bac-44ab-89a0-431a96341172	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-09-23	WORKDAY	\N	2026-04-09 09:05:55.517711	2026-04-09 09:05:55.517711
10a79727-9c13-4b26-8152-c010ade31046	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-09-24	WORKDAY	\N	2026-04-09 09:05:55.517711	2026-04-09 09:05:55.517711
cc99ecd4-6b01-48eb-8a87-d8d5ed338533	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-09-25	WORKDAY	\N	2026-04-09 09:05:55.517711	2026-04-09 09:05:55.517711
ebe25b6d-b620-44db-952d-05cd0e1850b9	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-09-27	RESTDAY	\N	2026-04-09 09:05:55.517711	2026-04-09 09:05:55.517711
1d7bec13-1d6a-45c0-a2f5-4ba694831351	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-09-28	WORKDAY	\N	2026-04-09 09:05:55.517711	2026-04-09 09:05:55.517711
1874c3d9-f0e0-4d2c-91ea-7c025a8a4307	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-09-29	WORKDAY	\N	2026-04-09 09:05:55.517711	2026-04-09 09:05:55.517711
29e6a02a-a9bc-4d55-a299-298ed8ed3afb	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-09-30	WORKDAY	\N	2026-04-09 09:05:55.517711	2026-04-09 09:05:55.517711
d1f3ee8a-3091-4f92-b803-ac6211f6cc6b	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-10-01	WORKDAY	\N	2026-04-09 09:05:55.517711	2026-04-09 09:05:55.517711
c8ed9da3-7085-4c51-91a9-93b3aa31c689	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-10-02	WORKDAY	\N	2026-04-09 09:05:55.517711	2026-04-09 09:05:55.517711
ae6c5a69-ffe4-4b63-b46b-3c40dbfa2bd3	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-10-04	RESTDAY	\N	2026-04-09 09:05:55.517711	2026-04-09 09:05:55.517711
3edf645b-863f-441d-ba0f-99b1da77a49f	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-10-05	WORKDAY	\N	2026-04-09 09:05:55.519327	2026-04-09 09:05:55.519327
5551d48e-42d7-46b6-bac4-3eefa0c819d4	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-10-06	WORKDAY	\N	2026-04-09 09:05:55.519327	2026-04-09 09:05:55.519327
54eb8d80-c636-4613-8d79-a94c4c0cf781	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-10-07	WORKDAY	\N	2026-04-09 09:05:55.519327	2026-04-09 09:05:55.519327
a5feb9f9-70b5-4fcf-8bfa-f28509b66087	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-10-08	WORKDAY	\N	2026-04-09 09:05:55.519327	2026-04-09 09:05:55.519327
bc9cb3a7-d16b-4981-8375-f0b8399ce5c3	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-10-09	WORKDAY	\N	2026-04-09 09:05:55.519327	2026-04-09 09:05:55.519327
21035090-209e-4c13-a7e2-6e0372d88bcf	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-10-11	RESTDAY	\N	2026-04-09 09:05:55.519327	2026-04-09 09:05:55.519327
5c70635d-52fe-435f-a0cc-61169f53ced0	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-10-12	WORKDAY	\N	2026-04-09 09:05:55.519327	2026-04-09 09:05:55.519327
dc4bc9b4-b37a-4688-a7d0-a9de597309d9	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-10-13	WORKDAY	\N	2026-04-09 09:05:55.519327	2026-04-09 09:05:55.519327
ddd70a4c-cd94-4fbf-8799-6d31114626eb	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-10-14	WORKDAY	\N	2026-04-09 09:05:55.519327	2026-04-09 09:05:55.519327
c939d088-4653-4ebe-8bfe-10322cfda892	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-10-15	WORKDAY	\N	2026-04-09 09:05:55.519327	2026-04-09 09:05:55.519327
0e421224-202f-467e-909c-09ccff345e0b	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-10-16	WORKDAY	\N	2026-04-09 09:05:55.519327	2026-04-09 09:05:55.519327
5ec1cad2-8589-4677-91ce-f17e5d0a6e24	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-10-18	RESTDAY	\N	2026-04-09 09:05:55.519327	2026-04-09 09:05:55.519327
7849dd9f-a92f-4c32-8d9f-bfcc259a7197	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-10-19	WORKDAY	\N	2026-04-09 09:05:55.519327	2026-04-09 09:05:55.519327
70193bb1-67c9-4aae-b065-ae5298ee032a	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-10-20	WORKDAY	\N	2026-04-09 09:05:55.519327	2026-04-09 09:05:55.519327
e43cf2c6-35aa-498d-95dc-e751cce1c2ca	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-10-21	WORKDAY	\N	2026-04-09 09:05:55.519327	2026-04-09 09:05:55.519327
794266ee-674b-41ee-b220-b480c56dcab1	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-10-22	WORKDAY	\N	2026-04-09 09:05:55.519327	2026-04-09 09:05:55.519327
940edd5c-99f9-469d-ae67-bf40bf547c04	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-10-23	WORKDAY	\N	2026-04-09 09:05:55.519327	2026-04-09 09:05:55.519327
c026955b-7278-4169-8e4b-b4166eb6ccdf	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-10-25	RESTDAY	\N	2026-04-09 09:05:55.519327	2026-04-09 09:05:55.519327
bf7c32e4-17fe-4dc8-beb2-1ce457d6b8ea	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-10-26	WORKDAY	\N	2026-04-09 09:05:55.519327	2026-04-09 09:05:55.519327
a11d6047-13b3-481b-ac75-d5e97d4801e5	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-10-27	WORKDAY	\N	2026-04-09 09:05:55.519327	2026-04-09 09:05:55.519327
4bd7454b-fca0-4b01-9137-20740ebb0883	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-10-28	WORKDAY	\N	2026-04-09 09:05:55.519327	2026-04-09 09:05:55.519327
6ad58960-bfc5-418e-955b-93ec073e3df0	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-10-29	WORKDAY	\N	2026-04-09 09:05:55.519327	2026-04-09 09:05:55.519327
15fdc71e-ffa9-4159-a309-68287ce07fc2	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-10-30	WORKDAY	\N	2026-04-09 09:05:55.519327	2026-04-09 09:05:55.519327
e5734905-dc24-43c6-9316-98a005524212	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-11-01	RESTDAY	\N	2026-04-09 09:05:55.519327	2026-04-09 09:05:55.519327
44cd4870-8c15-43f8-8d94-a01731b0cba1	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-11-02	WORKDAY	\N	2026-04-09 09:05:55.520338	2026-04-09 09:05:55.520338
18602e0e-34c3-41b4-a18e-4554c98eb637	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-11-03	WORKDAY	\N	2026-04-09 09:05:55.520338	2026-04-09 09:05:55.520338
37112dde-c23b-4e28-8b98-fc4cc5dfdfba	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-11-04	WORKDAY	\N	2026-04-09 09:05:55.520338	2026-04-09 09:05:55.520338
f64d4414-8b84-448f-a5af-2fad60f51c58	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-11-05	WORKDAY	\N	2026-04-09 09:05:55.520338	2026-04-09 09:05:55.520338
824f5592-4c06-4f9e-86a3-56349526ad7a	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-11-06	WORKDAY	\N	2026-04-09 09:05:55.520338	2026-04-09 09:05:55.520338
edd471f1-8c33-49a0-ae17-c6f56900fd24	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-11-08	RESTDAY	\N	2026-04-09 09:05:55.520338	2026-04-09 09:05:55.520338
6df31ff7-29e8-44c9-898f-489e6b479abb	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-11-09	WORKDAY	\N	2026-04-09 09:05:55.520338	2026-04-09 09:05:55.520338
c775ebbf-c595-459a-8677-3dce101c0492	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-11-10	WORKDAY	\N	2026-04-09 09:05:55.520338	2026-04-09 09:05:55.520338
e7e5d91d-e353-47a5-954f-632cd6ec2cd8	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-11-11	WORKDAY	\N	2026-04-09 09:05:55.520338	2026-04-09 09:05:55.520338
8c5757b2-b2f9-444e-b434-72c7c9601752	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-11-12	WORKDAY	\N	2026-04-09 09:05:55.520338	2026-04-09 09:05:55.520338
e8c903d9-ac8f-4cd3-b51e-1d065841f2fd	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-11-13	WORKDAY	\N	2026-04-09 09:05:55.520338	2026-04-09 09:05:55.520338
c6dac19f-481e-4355-b6d7-53cbfd852472	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-11-15	RESTDAY	\N	2026-04-09 09:05:55.520338	2026-04-09 09:05:55.520338
46290c06-cbfa-4ced-8328-c50d3c61f412	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-11-16	WORKDAY	\N	2026-04-09 09:05:55.520338	2026-04-09 09:05:55.520338
f521db3c-a87c-4842-8277-eec864d69ff6	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-11-17	WORKDAY	\N	2026-04-09 09:05:55.520338	2026-04-09 09:05:55.520338
c10cf18c-f777-4644-bd28-bd69d9448264	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-11-18	WORKDAY	\N	2026-04-09 09:05:55.520338	2026-04-09 09:05:55.520338
d3baf22f-f5dc-468e-8f52-61767074abe8	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-11-19	WORKDAY	\N	2026-04-09 09:05:55.520338	2026-04-09 09:05:55.520338
f57e6685-c9f4-4c90-bfe0-33becfbabaa7	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-11-20	WORKDAY	\N	2026-04-09 09:05:55.520338	2026-04-09 09:05:55.520338
076dd072-acfc-4f67-9da8-1e974efb05bf	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-11-22	RESTDAY	\N	2026-04-09 09:05:55.521544	2026-04-09 09:05:55.521544
79365af7-5642-4d50-a964-d1e1c16d407d	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-11-23	WORKDAY	\N	2026-04-09 09:05:55.521544	2026-04-09 09:05:55.521544
d3daaeee-9f2f-4355-8252-7ff202a6ea4c	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-11-24	WORKDAY	\N	2026-04-09 09:05:55.521544	2026-04-09 09:05:55.521544
9f8a0423-e2c9-4ae1-81f7-0033f44d902e	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-11-25	WORKDAY	\N	2026-04-09 09:05:55.521544	2026-04-09 09:05:55.521544
8b4d03e8-63c3-47c2-937d-44f4b4eebf8c	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-11-26	WORKDAY	\N	2026-04-09 09:05:55.521544	2026-04-09 09:05:55.521544
02b1e24f-3cda-46d4-92cf-6c63cf604f14	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-11-27	WORKDAY	\N	2026-04-09 09:05:55.521544	2026-04-09 09:05:55.521544
277c5760-3621-42c6-8f8b-eda2ee192b4b	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-11-29	RESTDAY	\N	2026-04-09 09:05:55.521544	2026-04-09 09:05:55.521544
6ed7ca23-5515-4356-a175-8c0c51102c23	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-11-30	WORKDAY	\N	2026-04-09 09:05:55.521544	2026-04-09 09:05:55.521544
e52ab076-66fb-4ee4-8bf3-016083cfc548	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-12-01	WORKDAY	\N	2026-04-09 09:05:55.521544	2026-04-09 09:05:55.521544
31c34cbe-048e-4cab-ad76-c4a37fd96e94	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-12-02	WORKDAY	\N	2026-04-09 09:05:55.521544	2026-04-09 09:05:55.521544
c9215623-c695-45d4-924d-e41ddd6568c4	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-12-03	WORKDAY	\N	2026-04-09 09:05:55.521544	2026-04-09 09:05:55.521544
00a7f962-2e38-4b48-9ef2-c6260c2f63c9	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-12-04	WORKDAY	\N	2026-04-09 09:05:55.521544	2026-04-09 09:05:55.521544
e2f58cbd-9622-46c9-9f49-19f34ba7067d	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-12-06	RESTDAY	\N	2026-04-09 09:05:55.521544	2026-04-09 09:05:55.521544
718ec158-8482-4fa6-aec0-57f6928164ac	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-12-07	WORKDAY	\N	2026-04-09 09:05:55.522035	2026-04-09 09:05:55.522035
84b41fd2-471d-4d1d-87b9-790e91f1e146	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-12-08	WORKDAY	\N	2026-04-09 09:05:55.522035	2026-04-09 09:05:55.522035
2089f906-59d6-4354-837b-3e80891a9064	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-12-09	WORKDAY	\N	2026-04-09 09:05:55.522035	2026-04-09 09:05:55.522035
6ed619d0-4287-42a4-96d6-c7dc514cab01	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-12-10	WORKDAY	\N	2026-04-09 09:05:55.522035	2026-04-09 09:05:55.522035
96188100-e7b4-4e6d-a03e-a020ef0fca29	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-12-11	WORKDAY	\N	2026-04-09 09:05:55.522035	2026-04-09 09:05:55.522035
5812436d-a5b3-4caf-9760-f3fad1ce44ce	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-12-13	RESTDAY	\N	2026-04-09 09:05:55.522035	2026-04-09 09:05:55.522035
bfcd2858-401d-4a43-8ed0-4b65cfdb61cf	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-12-14	WORKDAY	\N	2026-04-09 09:05:55.522035	2026-04-09 09:05:55.522035
75845d9d-1a40-4dfd-b600-de5cc4fe3ebc	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-12-15	WORKDAY	\N	2026-04-09 09:05:55.522035	2026-04-09 09:05:55.522035
564cd983-c6f0-4230-8502-fe4175e75051	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-12-16	WORKDAY	\N	2026-04-09 09:05:55.522035	2026-04-09 09:05:55.522035
cf600eae-e4e5-457c-be67-2d242b6136e2	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-12-17	WORKDAY	\N	2026-04-09 09:05:55.522035	2026-04-09 09:05:55.522035
c0cdd0f5-eedf-4e02-b9f6-1a01d6d54d9d	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-12-18	WORKDAY	\N	2026-04-09 09:05:55.522035	2026-04-09 09:05:55.522035
9af05b52-675b-49a8-ac2f-0dc484456eed	8648aa7c-5574-479f-9084-a9ca960b6855	2026-01-05	HOLIDAY		2026-04-09 07:31:11.317769	2026-04-09 07:31:11.317769
f3d1be33-98db-4de7-a40a-93fb77d2568f	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-12-20	RESTDAY	\N	2026-04-09 09:05:55.522035	2026-04-09 09:05:55.522035
6dee76ed-29b0-4c56-ab9d-702e7024b9b1	8648aa7c-5574-479f-9084-a9ca960b6855	2026-01-02	WORKDAY		2026-04-09 07:31:11.317769	2026-04-09 07:31:11.317769
e0441364-d1bd-40e7-a6ea-4bcc49788384	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-12-21	WORKDAY	\N	2026-04-09 09:05:55.522035	2026-04-09 09:05:55.522035
832edb12-4971-41be-a5a1-8af28a99f83c	8648aa7c-5574-479f-9084-a9ca960b6855	2026-02-10	HOLIDAY		2026-04-09 07:31:11.333851	2026-04-09 07:31:11.333851
65463254-dddc-459d-aad4-ecb7c9b6ca81	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-12-22	WORKDAY	\N	2026-04-09 09:05:55.522035	2026-04-09 09:05:55.522035
e0a88bb8-8dea-4a9f-95b2-47b1b728978b	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-12-23	WORKDAY	\N	2026-04-09 09:05:55.522035	2026-04-09 09:05:55.522035
ff94546f-e79b-4633-9b99-93a18f0d11f8	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-12-24	WORKDAY	\N	2026-04-09 09:05:55.522035	2026-04-09 09:05:55.522035
b3f89241-8eea-4de6-aef7-18f2c8e8fefb	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-12-25	WORKDAY	\N	2026-04-09 09:05:55.522543	2026-04-09 09:05:55.522543
a499f5ff-aa20-4b49-a84b-da30bfb6cdcd	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-12-27	RESTDAY	\N	2026-04-09 09:05:55.522543	2026-04-09 09:05:55.522543
3d410074-8eb7-43c9-94be-32589e1c1e00	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-12-28	WORKDAY	\N	2026-04-09 09:05:55.522543	2026-04-09 09:05:55.522543
68cddbec-ceb5-494c-b311-8e7c5aacdc80	8648aa7c-5574-479f-9084-a9ca960b6855	2026-01-01	WORKDAY	\N	2026-04-09 07:31:11.316771	2026-04-09 07:31:11.316771
aafbfde8-ac12-4c95-ac1f-7db21c465c93	8648aa7c-5574-479f-9084-a9ca960b6855	2026-01-03	RESTDAY	\N	2026-04-09 07:31:11.317769	2026-04-09 07:31:11.317769
121a5e98-ec16-4e24-95d6-dbc97fefc41b	8648aa7c-5574-479f-9084-a9ca960b6855	2026-01-04	RESTDAY	\N	2026-04-09 07:31:11.317769	2026-04-09 07:31:11.317769
ab0f33ab-5d7f-4b90-b56d-fc4f25284da1	8648aa7c-5574-479f-9084-a9ca960b6855	2026-01-06	WORKDAY	\N	2026-04-09 07:31:11.317769	2026-04-09 07:31:11.317769
2fbab72e-bc66-427b-a16e-3dc528cd0c39	8648aa7c-5574-479f-9084-a9ca960b6855	2026-01-07	WORKDAY	\N	2026-04-09 07:31:11.317769	2026-04-09 07:31:11.317769
52fdcd81-e50d-4477-8c7b-9196ac5ce6f2	8648aa7c-5574-479f-9084-a9ca960b6855	2026-01-08	WORKDAY	\N	2026-04-09 07:31:11.317769	2026-04-09 07:31:11.317769
48ba5fe5-5b87-4763-8150-c3b4e099e871	8648aa7c-5574-479f-9084-a9ca960b6855	2026-01-09	WORKDAY	\N	2026-04-09 07:31:11.317769	2026-04-09 07:31:11.317769
83401aa4-b869-4c52-a374-33ddf2331654	8648aa7c-5574-479f-9084-a9ca960b6855	2026-01-10	RESTDAY	\N	2026-04-09 07:31:11.317769	2026-04-09 07:31:11.317769
34cb63b6-f159-48f0-a741-d6e7330f1197	8648aa7c-5574-479f-9084-a9ca960b6855	2026-01-11	RESTDAY	\N	2026-04-09 07:31:11.317769	2026-04-09 07:31:11.317769
8eb6cc23-48ed-442d-8a1c-5f18fd5719e3	8648aa7c-5574-479f-9084-a9ca960b6855	2026-01-12	WORKDAY	\N	2026-04-09 07:31:11.317769	2026-04-09 07:31:11.317769
226ec39f-b82e-4fbf-a82c-0b8ff7574595	8648aa7c-5574-479f-9084-a9ca960b6855	2026-01-13	WORKDAY	\N	2026-04-09 07:31:11.317769	2026-04-09 07:31:11.317769
84bf25f7-b649-4ea4-b792-63eb25cfaaf7	8648aa7c-5574-479f-9084-a9ca960b6855	2026-01-14	WORKDAY	\N	2026-04-09 07:31:11.322768	2026-04-09 07:31:11.322768
7b74a024-fb9b-4216-95d5-0d1ac02766d4	8648aa7c-5574-479f-9084-a9ca960b6855	2026-01-15	WORKDAY	\N	2026-04-09 07:31:11.322768	2026-04-09 07:31:11.322768
8d37c38a-93de-43e8-8f9a-0a0df969fd74	8648aa7c-5574-479f-9084-a9ca960b6855	2026-01-16	WORKDAY	\N	2026-04-09 07:31:11.322768	2026-04-09 07:31:11.322768
43466334-7d97-453b-afa1-84568469106c	8648aa7c-5574-479f-9084-a9ca960b6855	2026-01-17	RESTDAY	\N	2026-04-09 07:31:11.322768	2026-04-09 07:31:11.322768
4c361edc-f370-4dea-baea-b8fdd52406a1	8648aa7c-5574-479f-9084-a9ca960b6855	2026-01-18	RESTDAY	\N	2026-04-09 07:31:11.322768	2026-04-09 07:31:11.322768
fbd4f56b-5e8b-41de-9ae7-9feee65a91de	8648aa7c-5574-479f-9084-a9ca960b6855	2026-01-19	WORKDAY	\N	2026-04-09 07:31:11.32877	2026-04-09 07:31:11.32877
457b9a19-bb92-47d4-85dd-7d1cd39442c7	8648aa7c-5574-479f-9084-a9ca960b6855	2026-01-20	WORKDAY	\N	2026-04-09 07:31:11.32877	2026-04-09 07:31:11.32877
60e3ebf8-0947-4c39-8bb7-9d7542fdbaf4	8648aa7c-5574-479f-9084-a9ca960b6855	2026-01-21	WORKDAY	\N	2026-04-09 07:31:11.32877	2026-04-09 07:31:11.32877
e10f3646-6c76-4185-99a7-20f0973ad005	8648aa7c-5574-479f-9084-a9ca960b6855	2026-01-22	WORKDAY	\N	2026-04-09 07:31:11.32877	2026-04-09 07:31:11.32877
7c9a6824-0a61-4ea3-8e39-d903112444bf	8648aa7c-5574-479f-9084-a9ca960b6855	2026-01-23	WORKDAY	\N	2026-04-09 07:31:11.32877	2026-04-09 07:31:11.32877
9392a142-998c-4445-83b5-4a95a2548077	8648aa7c-5574-479f-9084-a9ca960b6855	2026-01-24	RESTDAY	\N	2026-04-09 07:31:11.32877	2026-04-09 07:31:11.32877
920f3d9f-980e-424b-9486-99113e1d0337	8648aa7c-5574-479f-9084-a9ca960b6855	2026-01-25	RESTDAY	\N	2026-04-09 07:31:11.32877	2026-04-09 07:31:11.32877
777fef8f-a801-4102-b72e-273b69aacab9	8648aa7c-5574-479f-9084-a9ca960b6855	2026-01-26	WORKDAY	\N	2026-04-09 07:31:11.32877	2026-04-09 07:31:11.32877
09dee8c9-e401-4680-8078-79ffd807d493	8648aa7c-5574-479f-9084-a9ca960b6855	2026-01-27	WORKDAY	\N	2026-04-09 07:31:11.32877	2026-04-09 07:31:11.32877
8156c44c-c81b-47ef-9fbb-beeec2270bc4	8648aa7c-5574-479f-9084-a9ca960b6855	2026-01-28	WORKDAY	\N	2026-04-09 07:31:11.32877	2026-04-09 07:31:11.32877
8b5322a2-2898-4dd1-9a2a-4c861c708346	8648aa7c-5574-479f-9084-a9ca960b6855	2026-01-29	WORKDAY	\N	2026-04-09 07:31:11.32877	2026-04-09 07:31:11.32877
5bd66a6f-3bfc-4941-b2c3-95708d264d5a	8648aa7c-5574-479f-9084-a9ca960b6855	2026-01-30	WORKDAY	\N	2026-04-09 07:31:11.32877	2026-04-09 07:31:11.32877
2d7f3f20-2e62-4756-bb1d-6f3011f9e508	8648aa7c-5574-479f-9084-a9ca960b6855	2026-01-31	RESTDAY	\N	2026-04-09 07:31:11.32877	2026-04-09 07:31:11.32877
608213b5-84ae-4a69-99a5-42883d4c3039	8648aa7c-5574-479f-9084-a9ca960b6855	2026-02-01	RESTDAY	\N	2026-04-09 07:31:11.32877	2026-04-09 07:31:11.32877
343630f9-39dc-4646-9c84-38f9ba68e269	8648aa7c-5574-479f-9084-a9ca960b6855	2026-02-02	WORKDAY	\N	2026-04-09 07:31:11.32877	2026-04-09 07:31:11.32877
89bef4fa-9db9-4411-a755-f06653ac1103	8648aa7c-5574-479f-9084-a9ca960b6855	2026-02-03	WORKDAY	\N	2026-04-09 07:31:11.332853	2026-04-09 07:31:11.332853
34ccae69-5592-4cab-9423-a36b1af219fa	8648aa7c-5574-479f-9084-a9ca960b6855	2026-02-04	WORKDAY	\N	2026-04-09 07:31:11.332853	2026-04-09 07:31:11.332853
c01a3781-7994-49c6-acdb-7513b23bcbea	8648aa7c-5574-479f-9084-a9ca960b6855	2026-02-05	WORKDAY	\N	2026-04-09 07:31:11.333851	2026-04-09 07:31:11.333851
d69b53e8-52c2-476d-b96c-a1f13a8bb967	8648aa7c-5574-479f-9084-a9ca960b6855	2026-02-06	WORKDAY	\N	2026-04-09 07:31:11.333851	2026-04-09 07:31:11.333851
6355605b-eb89-4035-9f06-4a123a9dabb7	8648aa7c-5574-479f-9084-a9ca960b6855	2026-02-07	RESTDAY	\N	2026-04-09 07:31:11.333851	2026-04-09 07:31:11.333851
4b2a73a9-1f0d-4a77-b0b9-0a5b8ace5c15	8648aa7c-5574-479f-9084-a9ca960b6855	2026-02-08	RESTDAY	\N	2026-04-09 07:31:11.333851	2026-04-09 07:31:11.333851
f905f40b-50fd-4c6e-9a77-89949a1186a7	8648aa7c-5574-479f-9084-a9ca960b6855	2026-02-09	WORKDAY	\N	2026-04-09 07:31:11.333851	2026-04-09 07:31:11.333851
ea150f9c-a6d6-43c9-b78b-5d083c82d489	8648aa7c-5574-479f-9084-a9ca960b6855	2026-02-18	WORKDAY	\N	2026-04-09 07:31:11.333851	2026-04-09 07:31:11.333851
c231694f-e464-4e9e-9531-d8d8c17bb475	8648aa7c-5574-479f-9084-a9ca960b6855	2026-02-19	WORKDAY	\N	2026-04-09 07:31:11.333851	2026-04-09 07:31:11.333851
6042fbaa-76ce-47e3-889a-cd838575b801	8648aa7c-5574-479f-9084-a9ca960b6855	2026-02-20	WORKDAY	\N	2026-04-09 07:31:11.333851	2026-04-09 07:31:11.333851
67c3b0c7-e732-454c-a548-33918e78bdf6	8648aa7c-5574-479f-9084-a9ca960b6855	2026-02-21	RESTDAY	\N	2026-04-09 07:31:11.333851	2026-04-09 07:31:11.333851
70b23632-289f-40f1-8ce1-02a97efb500e	8648aa7c-5574-479f-9084-a9ca960b6855	2026-02-22	RESTDAY	\N	2026-04-09 07:31:11.333851	2026-04-09 07:31:11.333851
b1d650a9-3bed-4561-afca-68dd2e684d5c	8648aa7c-5574-479f-9084-a9ca960b6855	2026-02-23	WORKDAY	\N	2026-04-09 07:31:11.333851	2026-04-09 07:31:11.333851
be99ebbd-9838-4f87-9a0b-3f93a357dc7d	8648aa7c-5574-479f-9084-a9ca960b6855	2026-02-24	WORKDAY	\N	2026-04-09 07:31:11.333851	2026-04-09 07:31:11.333851
5f2763af-cbe1-4d96-a2b6-3d24adecd108	8648aa7c-5574-479f-9084-a9ca960b6855	2026-02-25	WORKDAY	\N	2026-04-09 07:31:11.333851	2026-04-09 07:31:11.333851
2a48c80e-e29e-45b5-b933-d4cc69738e26	8648aa7c-5574-479f-9084-a9ca960b6855	2026-02-26	WORKDAY	\N	2026-04-09 07:31:11.333851	2026-04-09 07:31:11.333851
22c19d63-0eb4-4a0e-a225-9f9bac4c8fc3	8648aa7c-5574-479f-9084-a9ca960b6855	2026-02-27	WORKDAY	\N	2026-04-09 07:31:11.333851	2026-04-09 07:31:11.333851
922d249f-1a15-4216-b900-ab606be9f5ce	8648aa7c-5574-479f-9084-a9ca960b6855	2026-02-28	RESTDAY	\N	2026-04-09 07:31:11.333851	2026-04-09 07:31:11.333851
4836cfea-86fe-4b06-af4c-f1542e3e97da	8648aa7c-5574-479f-9084-a9ca960b6855	2026-03-01	RESTDAY	\N	2026-04-09 07:31:11.333851	2026-04-09 07:31:11.333851
c681a244-88a1-4f5e-b244-7bfbd427abfe	8648aa7c-5574-479f-9084-a9ca960b6855	2026-03-02	WORKDAY	\N	2026-04-09 07:31:11.333851	2026-04-09 07:31:11.333851
b003c141-fcba-4573-a653-1f3fce44aa42	8648aa7c-5574-479f-9084-a9ca960b6855	2026-03-03	WORKDAY	\N	2026-04-09 07:31:11.333851	2026-04-09 07:31:11.333851
e3414303-677c-4db1-9eda-27c024272236	8648aa7c-5574-479f-9084-a9ca960b6855	2026-03-04	WORKDAY	\N	2026-04-09 07:31:11.333851	2026-04-09 07:31:11.333851
0645f6c1-6716-4dad-b564-8a44e69e32b8	8648aa7c-5574-479f-9084-a9ca960b6855	2026-03-05	WORKDAY	\N	2026-04-09 07:31:11.333851	2026-04-09 07:31:11.333851
f636f10e-a0fe-4d67-85cd-1ff81376f65b	8648aa7c-5574-479f-9084-a9ca960b6855	2026-03-06	WORKDAY	\N	2026-04-09 07:31:11.333851	2026-04-09 07:31:11.333851
22f0b986-365b-4f4d-bb78-01a45e7c26e1	8648aa7c-5574-479f-9084-a9ca960b6855	2026-03-07	RESTDAY	\N	2026-04-09 07:31:11.333851	2026-04-09 07:31:11.333851
8e13aeef-d349-4716-bc00-da82f21c51e5	8648aa7c-5574-479f-9084-a9ca960b6855	2026-03-08	RESTDAY	\N	2026-04-09 07:31:11.333851	2026-04-09 07:31:11.333851
55e107b8-21ed-4aef-a653-71093cb84302	8648aa7c-5574-479f-9084-a9ca960b6855	2026-03-09	WORKDAY	\N	2026-04-09 07:31:11.333851	2026-04-09 07:31:11.333851
56a2a085-9d4d-498e-b971-c4c526dfd769	8648aa7c-5574-479f-9084-a9ca960b6855	2026-03-10	WORKDAY	\N	2026-04-09 07:31:11.333851	2026-04-09 07:31:11.333851
30f6ef8b-fde5-45d4-b20c-5137bd2a9196	8648aa7c-5574-479f-9084-a9ca960b6855	2026-03-11	WORKDAY	\N	2026-04-09 07:31:11.333851	2026-04-09 07:31:11.333851
ee57dc35-77f4-4b2d-b763-f685ea11fcd7	8648aa7c-5574-479f-9084-a9ca960b6855	2026-03-12	WORKDAY	\N	2026-04-09 07:31:11.333851	2026-04-09 07:31:11.333851
de5583cc-ac5c-4a49-94ea-49086b9d7c1e	8648aa7c-5574-479f-9084-a9ca960b6855	2026-03-13	WORKDAY	\N	2026-04-09 07:31:11.333851	2026-04-09 07:31:11.333851
feeac8c9-7531-4fad-950e-e6858dc6a53c	8648aa7c-5574-479f-9084-a9ca960b6855	2026-03-14	RESTDAY	\N	2026-04-09 07:31:11.333851	2026-04-09 07:31:11.333851
02fe9b50-1821-4355-868b-f1cbed52c865	8648aa7c-5574-479f-9084-a9ca960b6855	2026-03-15	RESTDAY	\N	2026-04-09 07:31:11.333851	2026-04-09 07:31:11.333851
6deb5f18-86dc-45e6-b90e-52831363c6a7	8648aa7c-5574-479f-9084-a9ca960b6855	2026-03-16	WORKDAY	\N	2026-04-09 07:31:11.333851	2026-04-09 07:31:11.333851
aa4fa1b5-447c-4546-bfa5-10a212652014	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-12-29	WORKDAY	\N	2026-04-09 09:05:55.522543	2026-04-09 09:05:55.522543
375dbb7d-cc91-40c7-9278-1a85d5767806	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-12-30	WORKDAY	\N	2026-04-09 09:05:55.522543	2026-04-09 09:05:55.522543
f4d0c65e-98f5-4e3c-b7e7-0aeedf5c20fe	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-12-19	WORKDAY	\N	2026-04-09 09:05:55.522035	2026-04-09 09:05:55.522035
25096ced-3233-4093-ad25-9039f9db6aad	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-12-26	WORKDAY	\N	2026-04-09 09:05:55.522543	2026-04-09 09:05:55.522543
e19aff3f-f6f2-4be8-89a1-5ecdae9d0780	8648aa7c-5574-479f-9084-a9ca960b6855	2026-03-17	WORKDAY	\N	2026-04-09 07:31:11.333851	2026-04-09 07:31:11.333851
b9272b24-e602-4393-8215-3db332e9c107	8648aa7c-5574-479f-9084-a9ca960b6855	2026-03-18	WORKDAY	\N	2026-04-09 07:31:11.333851	2026-04-09 07:31:11.333851
cea38456-e850-4bb9-b1e0-4d86a24e5d0d	8648aa7c-5574-479f-9084-a9ca960b6855	2026-03-19	WORKDAY	\N	2026-04-09 07:31:11.333851	2026-04-09 07:31:11.333851
2fdfcae3-8a04-4c37-90bc-a59154056591	8648aa7c-5574-479f-9084-a9ca960b6855	2026-03-20	WORKDAY	\N	2026-04-09 07:31:11.333851	2026-04-09 07:31:11.333851
24d93ee8-1905-44b1-9382-a90a700bf590	8648aa7c-5574-479f-9084-a9ca960b6855	2026-03-21	RESTDAY	\N	2026-04-09 07:31:11.333851	2026-04-09 07:31:11.333851
386484a1-00a4-488f-b1df-69eb23fb3c7f	8648aa7c-5574-479f-9084-a9ca960b6855	2026-03-22	RESTDAY	\N	2026-04-09 07:31:11.333851	2026-04-09 07:31:11.333851
5d7466c0-3aee-4317-a661-233f06821274	8648aa7c-5574-479f-9084-a9ca960b6855	2026-03-23	WORKDAY	\N	2026-04-09 07:31:11.333851	2026-04-09 07:31:11.333851
7daefa89-b718-46f0-ad18-10bb911d746e	8648aa7c-5574-479f-9084-a9ca960b6855	2026-03-24	WORKDAY	\N	2026-04-09 07:31:11.333851	2026-04-09 07:31:11.333851
b93e51ec-c688-4822-9655-e280612305af	8648aa7c-5574-479f-9084-a9ca960b6855	2026-03-25	WORKDAY	\N	2026-04-09 07:31:11.333851	2026-04-09 07:31:11.333851
e2d0bfef-4de4-4185-9eb0-f0bb127cf05d	8648aa7c-5574-479f-9084-a9ca960b6855	2026-03-26	WORKDAY	\N	2026-04-09 07:31:11.333851	2026-04-09 07:31:11.333851
f72fcc8e-ec35-49ce-a5bf-6dfb528c8246	8648aa7c-5574-479f-9084-a9ca960b6855	2026-03-27	WORKDAY	\N	2026-04-09 07:31:11.333851	2026-04-09 07:31:11.333851
1e569e2d-b6c8-4f53-ad9e-3ca21453ff5a	8648aa7c-5574-479f-9084-a9ca960b6855	2026-03-28	RESTDAY	\N	2026-04-09 07:31:11.333851	2026-04-09 07:31:11.333851
a108221a-b2c4-43c5-8564-55d889e3263c	8648aa7c-5574-479f-9084-a9ca960b6855	2026-03-29	RESTDAY	\N	2026-04-09 07:31:11.333851	2026-04-09 07:31:11.333851
23cbfe99-f213-446c-9d60-d9420c7b04d4	8648aa7c-5574-479f-9084-a9ca960b6855	2026-03-30	WORKDAY	\N	2026-04-09 07:31:11.333851	2026-04-09 07:31:11.333851
9d0cab4b-57cd-468e-b0d4-461cb79773c6	8648aa7c-5574-479f-9084-a9ca960b6855	2026-03-31	WORKDAY	\N	2026-04-09 07:31:11.333851	2026-04-09 07:31:11.333851
27950196-7865-4bbc-8f54-15b8213d9cca	8648aa7c-5574-479f-9084-a9ca960b6855	2026-04-01	WORKDAY	\N	2026-04-09 07:31:11.333851	2026-04-09 07:31:11.333851
1691152f-ddbc-42f0-aca7-2338236abd77	8648aa7c-5574-479f-9084-a9ca960b6855	2026-04-02	WORKDAY	\N	2026-04-09 07:31:11.333851	2026-04-09 07:31:11.333851
e4bc0cd2-5144-402f-b282-bd62363fb90d	8648aa7c-5574-479f-9084-a9ca960b6855	2026-04-03	WORKDAY	\N	2026-04-09 07:31:11.333851	2026-04-09 07:31:11.333851
adedc240-8428-42e9-a9f7-7ab528b01c2e	8648aa7c-5574-479f-9084-a9ca960b6855	2026-04-07	WORKDAY	\N	2026-04-09 07:31:11.333851	2026-04-09 07:31:11.333851
17c490c6-b013-4e50-9160-12eb26f1f208	8648aa7c-5574-479f-9084-a9ca960b6855	2026-04-08	WORKDAY	\N	2026-04-09 07:31:11.333851	2026-04-09 07:31:11.333851
f913b159-edcb-4206-8eef-693badfb4b68	8648aa7c-5574-479f-9084-a9ca960b6855	2026-04-09	WORKDAY	\N	2026-04-09 07:31:11.333851	2026-04-09 07:31:11.333851
5d0c32f0-7c47-42a2-95b6-8fa3fa107c82	8648aa7c-5574-479f-9084-a9ca960b6855	2026-04-10	WORKDAY	\N	2026-04-09 07:31:11.333851	2026-04-09 07:31:11.333851
afb390b5-f3c3-4dcf-b7c0-b7410d9afee4	8648aa7c-5574-479f-9084-a9ca960b6855	2026-04-11	RESTDAY	\N	2026-04-09 07:31:11.333851	2026-04-09 07:31:11.333851
e6c91dd7-77a3-4fe3-b889-690ae8804b25	8648aa7c-5574-479f-9084-a9ca960b6855	2026-04-12	RESTDAY	\N	2026-04-09 07:31:11.333851	2026-04-09 07:31:11.333851
1360e7ad-86d9-470c-99e8-171ee72bddc8	8648aa7c-5574-479f-9084-a9ca960b6855	2026-04-13	WORKDAY	\N	2026-04-09 07:31:11.333851	2026-04-09 07:31:11.333851
42e572d9-fdec-48b5-8e44-7e74f1ff27f2	8648aa7c-5574-479f-9084-a9ca960b6855	2026-04-14	WORKDAY	\N	2026-04-09 07:31:11.333851	2026-04-09 07:31:11.333851
15513e38-ce8e-47ea-a849-08c1c7bc041e	8648aa7c-5574-479f-9084-a9ca960b6855	2026-04-15	WORKDAY	\N	2026-04-09 07:31:11.333851	2026-04-09 07:31:11.333851
c19f95cf-906b-4d2b-a857-7653b8885075	8648aa7c-5574-479f-9084-a9ca960b6855	2026-04-16	WORKDAY	\N	2026-04-09 07:31:11.333851	2026-04-09 07:31:11.333851
d52fe945-bb84-4a45-b49e-49be5ccb4b2c	8648aa7c-5574-479f-9084-a9ca960b6855	2026-04-17	WORKDAY	\N	2026-04-09 07:31:11.333851	2026-04-09 07:31:11.333851
9850d136-f861-46dc-8f25-cfdb94ff9001	8648aa7c-5574-479f-9084-a9ca960b6855	2026-04-18	RESTDAY	\N	2026-04-09 07:31:11.333851	2026-04-09 07:31:11.333851
32ea559f-8cdd-4822-9adc-484b0c9148a8	8648aa7c-5574-479f-9084-a9ca960b6855	2026-04-19	RESTDAY	\N	2026-04-09 07:31:11.333851	2026-04-09 07:31:11.333851
e4198332-4775-4432-89df-a57c512a8a3f	8648aa7c-5574-479f-9084-a9ca960b6855	2026-04-20	WORKDAY	\N	2026-04-09 07:31:11.333851	2026-04-09 07:31:11.333851
2319f15d-26c5-42fa-a439-1d8bd1a5cbd4	8648aa7c-5574-479f-9084-a9ca960b6855	2026-04-21	WORKDAY	\N	2026-04-09 07:31:11.333851	2026-04-09 07:31:11.333851
2838ed39-86cc-4cb0-a579-d55772e46ba1	8648aa7c-5574-479f-9084-a9ca960b6855	2026-04-22	WORKDAY	\N	2026-04-09 07:31:11.333851	2026-04-09 07:31:11.333851
22c81713-94ae-441e-9514-ab0d2ccc1fce	8648aa7c-5574-479f-9084-a9ca960b6855	2026-04-23	WORKDAY	\N	2026-04-09 07:31:11.333851	2026-04-09 07:31:11.333851
575a46fb-abae-4a51-a271-b55089908cac	8648aa7c-5574-479f-9084-a9ca960b6855	2026-04-24	WORKDAY	\N	2026-04-09 07:31:11.333851	2026-04-09 07:31:11.333851
b41c0f54-bf94-44dc-b40c-1b27f11afbb6	8648aa7c-5574-479f-9084-a9ca960b6855	2026-04-25	RESTDAY	\N	2026-04-09 07:31:11.333851	2026-04-09 07:31:11.333851
41641687-4d08-476f-90bb-cbbc912b4e83	8648aa7c-5574-479f-9084-a9ca960b6855	2026-04-26	RESTDAY	\N	2026-04-09 07:31:11.333851	2026-04-09 07:31:11.333851
ab02997f-9fe5-450f-b04d-c5cfcf3d83e6	8648aa7c-5574-479f-9084-a9ca960b6855	2026-04-27	WORKDAY	\N	2026-04-09 07:31:11.333851	2026-04-09 07:31:11.333851
08080dd7-ac29-4824-a598-73f8db1a8045	8648aa7c-5574-479f-9084-a9ca960b6855	2026-04-28	WORKDAY	\N	2026-04-09 07:31:11.333851	2026-04-09 07:31:11.333851
870ef46e-b68e-435c-adfd-e4f96ae24a54	8648aa7c-5574-479f-9084-a9ca960b6855	2026-04-29	WORKDAY	\N	2026-04-09 07:31:11.333851	2026-04-09 07:31:11.333851
98d6fa8d-e027-40b2-95f6-1cffe9e3d621	8648aa7c-5574-479f-9084-a9ca960b6855	2026-04-30	WORKDAY	\N	2026-04-09 07:31:11.333851	2026-04-09 07:31:11.333851
f3a9ef09-2a6b-4dfc-8d98-04b8b076c265	8648aa7c-5574-479f-9084-a9ca960b6855	2026-05-01	WORKDAY	\N	2026-04-09 07:31:11.333851	2026-04-09 07:31:11.333851
3cdc23dd-d94f-40ec-a585-5c27b30a02de	8648aa7c-5574-479f-9084-a9ca960b6855	2026-05-02	RESTDAY	\N	2026-04-09 07:31:11.333851	2026-04-09 07:31:11.333851
d76a6ece-e590-48fa-b814-ed1665a938a9	8648aa7c-5574-479f-9084-a9ca960b6855	2026-05-03	RESTDAY	\N	2026-04-09 07:31:11.333851	2026-04-09 07:31:11.333851
b30f73b3-e0e0-4a67-a3dd-b9b4c85bd8e7	8648aa7c-5574-479f-9084-a9ca960b6855	2026-05-04	WORKDAY	\N	2026-04-09 07:31:11.357313	2026-04-09 07:31:11.357313
06e2f9c7-77c9-46f2-9a09-7282b8d6ecec	8648aa7c-5574-479f-9084-a9ca960b6855	2026-05-05	WORKDAY	\N	2026-04-09 07:31:11.357313	2026-04-09 07:31:11.357313
7688fa61-c1ce-44cd-aeaa-179ab128db92	8648aa7c-5574-479f-9084-a9ca960b6855	2026-05-06	WORKDAY	\N	2026-04-09 07:31:11.357313	2026-04-09 07:31:11.357313
3d270aa6-11b0-4c74-81b0-f396a7f3c0cd	8648aa7c-5574-479f-9084-a9ca960b6855	2026-05-07	WORKDAY	\N	2026-04-09 07:31:11.357313	2026-04-09 07:31:11.357313
3c029455-0eb5-4680-8065-cd81c2840221	8648aa7c-5574-479f-9084-a9ca960b6855	2026-05-08	WORKDAY	\N	2026-04-09 07:31:11.357313	2026-04-09 07:31:11.357313
e567fe7f-64ec-4081-b67c-118f95057d72	8648aa7c-5574-479f-9084-a9ca960b6855	2026-05-09	RESTDAY	\N	2026-04-09 07:31:11.357313	2026-04-09 07:31:11.357313
b7b7d731-4d11-4e77-8853-efb40cd26802	8648aa7c-5574-479f-9084-a9ca960b6855	2026-05-10	RESTDAY	\N	2026-04-09 07:31:11.357313	2026-04-09 07:31:11.357313
e297bfb8-3b8f-4da7-b0c7-52bcc623300f	8648aa7c-5574-479f-9084-a9ca960b6855	2026-05-11	WORKDAY	\N	2026-04-09 07:31:11.357313	2026-04-09 07:31:11.357313
f21a4814-76e0-4ef8-9c88-6971d04f531a	8648aa7c-5574-479f-9084-a9ca960b6855	2026-05-12	WORKDAY	\N	2026-04-09 07:31:11.357313	2026-04-09 07:31:11.357313
0ccf8797-a0c0-4f30-95a3-e1438ff0a48a	8648aa7c-5574-479f-9084-a9ca960b6855	2026-05-13	WORKDAY	\N	2026-04-09 07:31:11.357313	2026-04-09 07:31:11.357313
454bf21e-a29c-4e63-b418-fbab263dd9f5	8648aa7c-5574-479f-9084-a9ca960b6855	2026-05-14	WORKDAY	\N	2026-04-09 07:31:11.357313	2026-04-09 07:31:11.357313
413e04cb-a9d4-4620-8302-e59423b85315	8648aa7c-5574-479f-9084-a9ca960b6855	2026-05-15	WORKDAY	\N	2026-04-09 07:31:11.357313	2026-04-09 07:31:11.357313
40f92c7e-a62e-4aae-a8b3-cb0cca627e61	8648aa7c-5574-479f-9084-a9ca960b6855	2026-05-16	RESTDAY	\N	2026-04-09 07:31:11.366312	2026-04-09 07:31:11.366312
ab52a4ea-980f-43a0-8bc5-e0f0e39f7032	8648aa7c-5574-479f-9084-a9ca960b6855	2026-05-17	RESTDAY	\N	2026-04-09 07:31:11.366312	2026-04-09 07:31:11.366312
e7482b83-e325-437e-8882-4150e783ec6b	8648aa7c-5574-479f-9084-a9ca960b6855	2026-05-18	WORKDAY	\N	2026-04-09 07:31:11.366312	2026-04-09 07:31:11.366312
235d5dd5-1eca-416c-99e6-5196f6e1262a	8648aa7c-5574-479f-9084-a9ca960b6855	2026-05-19	WORKDAY	\N	2026-04-09 07:31:11.366312	2026-04-09 07:31:11.366312
95936425-b8bd-4bcb-88c2-fddbb9e2309e	8648aa7c-5574-479f-9084-a9ca960b6855	2026-05-20	WORKDAY	\N	2026-04-09 07:31:11.366312	2026-04-09 07:31:11.366312
060ba246-a05a-451f-9300-27cd497db17a	8648aa7c-5574-479f-9084-a9ca960b6855	2026-05-21	WORKDAY	\N	2026-04-09 07:31:11.366312	2026-04-09 07:31:11.366312
62dea570-9018-4016-9007-832de9615f29	8648aa7c-5574-479f-9084-a9ca960b6855	2026-05-22	WORKDAY	\N	2026-04-09 07:31:11.366312	2026-04-09 07:31:11.366312
b8501a70-984f-4f2b-aad5-1b09189661ad	8648aa7c-5574-479f-9084-a9ca960b6855	2026-05-23	RESTDAY	\N	2026-04-09 07:31:11.366312	2026-04-09 07:31:11.366312
f1a837b0-d318-494e-b92d-e6ee692de20e	8648aa7c-5574-479f-9084-a9ca960b6855	2026-05-24	RESTDAY	\N	2026-04-09 07:31:11.366312	2026-04-09 07:31:11.366312
52a20af5-a834-4a56-b56b-c0fcd82a266a	8648aa7c-5574-479f-9084-a9ca960b6855	2026-05-25	WORKDAY	\N	2026-04-09 07:31:11.366312	2026-04-09 07:31:11.366312
7f4c2bf1-0f00-4945-8ebd-241fceb23197	8648aa7c-5574-479f-9084-a9ca960b6855	2026-05-26	WORKDAY	\N	2026-04-09 07:31:11.366312	2026-04-09 07:31:11.366312
3296c7bc-243e-4e10-a551-590c2beda864	8648aa7c-5574-479f-9084-a9ca960b6855	2026-05-27	WORKDAY	\N	2026-04-09 07:31:11.366312	2026-04-09 07:31:11.366312
bfa5f5db-eaf1-4073-9063-6e7f2a05ade6	8648aa7c-5574-479f-9084-a9ca960b6855	2026-05-28	WORKDAY	\N	2026-04-09 07:31:11.366312	2026-04-09 07:31:11.366312
2ae96eb1-bf41-47fd-857b-a96a36a75e99	8648aa7c-5574-479f-9084-a9ca960b6855	2026-05-29	WORKDAY	\N	2026-04-09 07:31:11.366312	2026-04-09 07:31:11.366312
994d96f3-8349-49fe-a5ec-d9c7711f2092	8648aa7c-5574-479f-9084-a9ca960b6855	2026-05-30	RESTDAY	\N	2026-04-09 07:31:11.366312	2026-04-09 07:31:11.366312
4810699c-7731-4ec7-b2d9-40fbb382a2a4	8648aa7c-5574-479f-9084-a9ca960b6855	2026-05-31	RESTDAY	\N	2026-04-09 07:31:11.366312	2026-04-09 07:31:11.366312
a8b1bc15-236f-4ae3-89d2-67af0c671906	8648aa7c-5574-479f-9084-a9ca960b6855	2026-06-01	WORKDAY	\N	2026-04-09 07:31:11.366312	2026-04-09 07:31:11.366312
06cbee99-65fe-46f5-9a59-f30c0d536cdb	8648aa7c-5574-479f-9084-a9ca960b6855	2026-06-02	WORKDAY	\N	2026-04-09 07:31:11.366312	2026-04-09 07:31:11.366312
bb52f051-2aac-44a2-83c4-29e8aa244271	8648aa7c-5574-479f-9084-a9ca960b6855	2026-06-03	WORKDAY	\N	2026-04-09 07:31:11.366312	2026-04-09 07:31:11.366312
2d14a294-d6d5-4b62-8af5-2abd05ef7dc4	8648aa7c-5574-479f-9084-a9ca960b6855	2026-06-04	WORKDAY	\N	2026-04-09 07:31:11.366312	2026-04-09 07:31:11.366312
2f61dc4f-72bd-4296-82b3-c6c820a9b9e5	8648aa7c-5574-479f-9084-a9ca960b6855	2026-06-05	WORKDAY	\N	2026-04-09 07:31:11.366312	2026-04-09 07:31:11.366312
8917aec7-0cdf-4882-a570-d9d9cd0d979c	8648aa7c-5574-479f-9084-a9ca960b6855	2026-06-06	RESTDAY	\N	2026-04-09 07:31:11.366312	2026-04-09 07:31:11.366312
649e0a3c-7be4-4116-97e7-c9339b69ba46	8648aa7c-5574-479f-9084-a9ca960b6855	2026-06-07	RESTDAY	\N	2026-04-09 07:31:11.366312	2026-04-09 07:31:11.366312
07c138a2-1941-4c74-a3c9-6b2f9b58b710	8648aa7c-5574-479f-9084-a9ca960b6855	2026-06-08	WORKDAY	\N	2026-04-09 07:31:11.366312	2026-04-09 07:31:11.366312
47b81b4c-1407-4d3f-affa-34b15f758883	8648aa7c-5574-479f-9084-a9ca960b6855	2026-06-09	WORKDAY	\N	2026-04-09 07:31:11.366312	2026-04-09 07:31:11.366312
53dc8062-52e1-4477-8fa6-a8eab125edb2	8648aa7c-5574-479f-9084-a9ca960b6855	2026-06-10	WORKDAY	\N	2026-04-09 07:31:11.366312	2026-04-09 07:31:11.366312
895cd621-f8f4-439b-adc1-b37e72b8a1b9	8648aa7c-5574-479f-9084-a9ca960b6855	2026-06-11	WORKDAY	\N	2026-04-09 07:31:11.366312	2026-04-09 07:31:11.366312
dd63a3a3-6732-4958-a251-e9141e286261	8648aa7c-5574-479f-9084-a9ca960b6855	2026-06-12	WORKDAY	\N	2026-04-09 07:31:11.366312	2026-04-09 07:31:11.366312
6d028ea0-315b-422a-ab12-3aee04f5e88b	8648aa7c-5574-479f-9084-a9ca960b6855	2026-06-13	RESTDAY	\N	2026-04-09 07:31:11.366312	2026-04-09 07:31:11.366312
b012cafc-7f1a-4ddc-9aa2-ac3f52a1589c	8648aa7c-5574-479f-9084-a9ca960b6855	2026-06-14	RESTDAY	\N	2026-04-09 07:31:11.366312	2026-04-09 07:31:11.366312
cec19c81-4523-4656-9e41-6823a423290e	8648aa7c-5574-479f-9084-a9ca960b6855	2026-06-15	WORKDAY	\N	2026-04-09 07:31:11.366312	2026-04-09 07:31:11.366312
5b97b853-a8e7-4b93-a6ce-751d11c1bde6	8648aa7c-5574-479f-9084-a9ca960b6855	2026-06-16	WORKDAY	\N	2026-04-09 07:31:11.366312	2026-04-09 07:31:11.366312
190dd674-a65d-4248-afe6-88b2191f3b9b	8648aa7c-5574-479f-9084-a9ca960b6855	2026-06-17	WORKDAY	\N	2026-04-09 07:31:11.366312	2026-04-09 07:31:11.366312
650dcb6c-ef31-47f5-9fe0-237b920bad54	8648aa7c-5574-479f-9084-a9ca960b6855	2026-06-18	WORKDAY	\N	2026-04-09 07:31:11.366312	2026-04-09 07:31:11.366312
9c19597d-9b8f-4042-b2a2-5ddf4d018ca9	8648aa7c-5574-479f-9084-a9ca960b6855	2026-06-19	WORKDAY	\N	2026-04-09 07:31:11.366312	2026-04-09 07:31:11.366312
a12f1bcd-1b05-4af6-85fb-a69d9ff1b26a	8648aa7c-5574-479f-9084-a9ca960b6855	2026-06-20	RESTDAY	\N	2026-04-09 07:31:11.366312	2026-04-09 07:31:11.366312
f0f5da3e-4601-4ad0-ac11-bb53052d62c5	8648aa7c-5574-479f-9084-a9ca960b6855	2026-06-21	RESTDAY	\N	2026-04-09 07:31:11.366312	2026-04-09 07:31:11.366312
4eab2e46-18d5-45ab-8632-e5ed3c79dd53	8648aa7c-5574-479f-9084-a9ca960b6855	2026-06-22	WORKDAY	\N	2026-04-09 07:31:11.366312	2026-04-09 07:31:11.366312
802de98f-7459-4fb4-9973-619e81ff1c5f	8648aa7c-5574-479f-9084-a9ca960b6855	2026-06-23	WORKDAY	\N	2026-04-09 07:31:11.366312	2026-04-09 07:31:11.366312
e768f7d1-4206-40db-b069-1df305993e2b	8648aa7c-5574-479f-9084-a9ca960b6855	2026-06-24	WORKDAY	\N	2026-04-09 07:31:11.366312	2026-04-09 07:31:11.366312
29154677-232b-4c79-8f22-87035e331f21	8648aa7c-5574-479f-9084-a9ca960b6855	2026-06-25	WORKDAY	\N	2026-04-09 07:31:11.366312	2026-04-09 07:31:11.366312
952db444-7c18-4c73-ab02-109fc7e03773	8648aa7c-5574-479f-9084-a9ca960b6855	2026-06-26	WORKDAY	\N	2026-04-09 07:31:11.366312	2026-04-09 07:31:11.366312
3f6f8124-1f97-4835-aafa-ac6e55060a57	8648aa7c-5574-479f-9084-a9ca960b6855	2026-06-27	RESTDAY	\N	2026-04-09 07:31:11.366312	2026-04-09 07:31:11.366312
686e6fa9-8fb6-4103-a5d1-a0d8390bdddc	8648aa7c-5574-479f-9084-a9ca960b6855	2026-06-28	RESTDAY	\N	2026-04-09 07:31:11.366312	2026-04-09 07:31:11.366312
b74f871e-3c26-4ea7-a85c-71f888f072f9	8648aa7c-5574-479f-9084-a9ca960b6855	2026-06-29	WORKDAY	\N	2026-04-09 07:31:11.366312	2026-04-09 07:31:11.366312
4476f043-5e82-4f2f-a64c-8a973ca87add	8648aa7c-5574-479f-9084-a9ca960b6855	2026-06-30	WORKDAY	\N	2026-04-09 07:31:11.366312	2026-04-09 07:31:11.366312
4b2c4693-26d5-42af-ace4-eec797a32be3	8648aa7c-5574-479f-9084-a9ca960b6855	2026-07-01	WORKDAY	\N	2026-04-09 07:31:11.366312	2026-04-09 07:31:11.366312
3aeca5ed-8896-4795-9678-f4609d467d15	8648aa7c-5574-479f-9084-a9ca960b6855	2026-07-02	WORKDAY	\N	2026-04-09 07:31:11.366312	2026-04-09 07:31:11.366312
b9254b49-7272-4f84-98ac-95727bf44d5c	8648aa7c-5574-479f-9084-a9ca960b6855	2026-07-03	WORKDAY	\N	2026-04-09 07:31:11.366312	2026-04-09 07:31:11.366312
520fe5cd-1293-4387-bc24-45194fa42cc2	8648aa7c-5574-479f-9084-a9ca960b6855	2026-07-04	RESTDAY	\N	2026-04-09 07:31:11.366312	2026-04-09 07:31:11.366312
20717616-dc92-4f94-b64a-edc046ce7261	8648aa7c-5574-479f-9084-a9ca960b6855	2026-07-05	RESTDAY	\N	2026-04-09 07:31:11.366312	2026-04-09 07:31:11.366312
75602588-a72f-40bb-a82a-44c3d695b3c8	8648aa7c-5574-479f-9084-a9ca960b6855	2026-07-06	WORKDAY	\N	2026-04-09 07:31:11.366312	2026-04-09 07:31:11.366312
57ef490c-95e7-43fa-9950-d3f64fbc3429	8648aa7c-5574-479f-9084-a9ca960b6855	2026-07-07	WORKDAY	\N	2026-04-09 07:31:11.366312	2026-04-09 07:31:11.366312
e0abcf4a-4a9e-4126-82ae-f09f6472a08a	8648aa7c-5574-479f-9084-a9ca960b6855	2026-07-08	WORKDAY	\N	2026-04-09 07:31:11.366312	2026-04-09 07:31:11.366312
d1fcea86-2d79-4c24-8425-b86270e9e164	8648aa7c-5574-479f-9084-a9ca960b6855	2026-07-09	WORKDAY	\N	2026-04-09 07:31:11.366312	2026-04-09 07:31:11.366312
b1effdf8-c6ad-4786-a9a0-12078374e62c	8648aa7c-5574-479f-9084-a9ca960b6855	2026-07-10	WORKDAY	\N	2026-04-09 07:31:11.366312	2026-04-09 07:31:11.366312
e46765b1-be15-4963-9480-47c8ca36b77f	8648aa7c-5574-479f-9084-a9ca960b6855	2026-07-11	RESTDAY	\N	2026-04-09 07:31:11.366312	2026-04-09 07:31:11.366312
242fc3e5-6580-4797-9e77-ae5697e50020	8648aa7c-5574-479f-9084-a9ca960b6855	2026-07-12	RESTDAY	\N	2026-04-09 07:31:11.366312	2026-04-09 07:31:11.366312
fd47be17-4685-46c0-bc69-9fd53caaed63	8648aa7c-5574-479f-9084-a9ca960b6855	2026-07-13	WORKDAY	\N	2026-04-09 07:31:11.366312	2026-04-09 07:31:11.366312
c24f0cdd-28cb-4cb5-9a6c-3623c968ae58	8648aa7c-5574-479f-9084-a9ca960b6855	2026-07-14	WORKDAY	\N	2026-04-09 07:31:11.366312	2026-04-09 07:31:11.366312
fc1b4fe4-2e08-4d1f-8d31-03a6bf9d3206	8648aa7c-5574-479f-9084-a9ca960b6855	2026-07-15	WORKDAY	\N	2026-04-09 07:31:11.366312	2026-04-09 07:31:11.366312
f618ea1a-e998-46cf-8fb0-4185ba3abfba	8648aa7c-5574-479f-9084-a9ca960b6855	2026-07-16	WORKDAY	\N	2026-04-09 07:31:11.366312	2026-04-09 07:31:11.366312
a6bad4f5-05e0-4c9f-abf0-f483c544912e	8648aa7c-5574-479f-9084-a9ca960b6855	2026-07-17	WORKDAY	\N	2026-04-09 07:31:11.366312	2026-04-09 07:31:11.366312
057f78f3-268d-4522-806e-e62e1b1aa78f	8648aa7c-5574-479f-9084-a9ca960b6855	2026-07-18	RESTDAY	\N	2026-04-09 07:31:11.366312	2026-04-09 07:31:11.366312
ebed0dd7-de13-4b01-93e2-cf8ae23fe8ed	8648aa7c-5574-479f-9084-a9ca960b6855	2026-07-19	RESTDAY	\N	2026-04-09 07:31:11.366312	2026-04-09 07:31:11.366312
99d0c66d-1a8e-4df1-a932-08d89454b85b	8648aa7c-5574-479f-9084-a9ca960b6855	2026-07-20	WORKDAY	\N	2026-04-09 07:31:11.366312	2026-04-09 07:31:11.366312
a526ccf9-7a0c-40a3-9229-3ec598086132	8648aa7c-5574-479f-9084-a9ca960b6855	2026-07-21	WORKDAY	\N	2026-04-09 07:31:11.366312	2026-04-09 07:31:11.366312
83d4917f-6040-4be1-bd33-14c02b3a163f	8648aa7c-5574-479f-9084-a9ca960b6855	2026-07-22	WORKDAY	\N	2026-04-09 07:31:11.366312	2026-04-09 07:31:11.366312
2627ab52-dcc8-4541-98c9-840179975ba9	8648aa7c-5574-479f-9084-a9ca960b6855	2026-07-23	WORKDAY	\N	2026-04-09 07:31:11.366312	2026-04-09 07:31:11.366312
1a8d11f0-cebd-499c-919b-2fe8858bab93	8648aa7c-5574-479f-9084-a9ca960b6855	2026-07-24	WORKDAY	\N	2026-04-09 07:31:11.366312	2026-04-09 07:31:11.366312
e49beb9f-326f-48d8-853a-2486ae59d3a0	8648aa7c-5574-479f-9084-a9ca960b6855	2026-07-25	RESTDAY	\N	2026-04-09 07:31:11.366312	2026-04-09 07:31:11.366312
83aa9cf5-64b4-4d24-b70c-f1673e2f635f	8648aa7c-5574-479f-9084-a9ca960b6855	2026-07-26	RESTDAY	\N	2026-04-09 07:31:11.366312	2026-04-09 07:31:11.366312
6f414fc6-23c0-48ce-a136-940ad355e3ca	8648aa7c-5574-479f-9084-a9ca960b6855	2026-07-27	WORKDAY	\N	2026-04-09 07:31:11.366312	2026-04-09 07:31:11.366312
832a1558-6949-46dd-87b2-5a473ab7ff9b	8648aa7c-5574-479f-9084-a9ca960b6855	2026-07-28	WORKDAY	\N	2026-04-09 07:31:11.366312	2026-04-09 07:31:11.366312
c5135cfc-dad0-4c88-8b8e-199fb1a8dddf	8648aa7c-5574-479f-9084-a9ca960b6855	2026-07-29	WORKDAY	\N	2026-04-09 07:31:11.366312	2026-04-09 07:31:11.366312
85727ff0-9bf4-4da5-b8d9-6bb7e4db1f7f	8648aa7c-5574-479f-9084-a9ca960b6855	2026-07-30	WORKDAY	\N	2026-04-09 07:31:11.366312	2026-04-09 07:31:11.366312
f27e10b5-562c-4751-aff0-a1533a31767a	8648aa7c-5574-479f-9084-a9ca960b6855	2026-07-31	WORKDAY	\N	2026-04-09 07:31:11.366312	2026-04-09 07:31:11.366312
918bd7fe-6189-41de-b228-bdd9630f70e7	8648aa7c-5574-479f-9084-a9ca960b6855	2026-08-01	RESTDAY	\N	2026-04-09 07:31:11.366312	2026-04-09 07:31:11.366312
6399c1e9-f2a8-4ae9-aab8-d970622f7f2b	8648aa7c-5574-479f-9084-a9ca960b6855	2026-08-02	RESTDAY	\N	2026-04-09 07:31:11.366312	2026-04-09 07:31:11.366312
2df8e44c-3279-4cdc-8b42-67d51d45e3dc	8648aa7c-5574-479f-9084-a9ca960b6855	2026-08-03	WORKDAY	\N	2026-04-09 07:31:11.366312	2026-04-09 07:31:11.366312
7523a96d-e6c3-40eb-b67c-b3a9d9be7223	8648aa7c-5574-479f-9084-a9ca960b6855	2026-08-04	WORKDAY	\N	2026-04-09 07:31:11.366312	2026-04-09 07:31:11.366312
bb90a259-17b6-4955-9a71-d1bd46174328	8648aa7c-5574-479f-9084-a9ca960b6855	2026-08-05	WORKDAY	\N	2026-04-09 07:31:11.366312	2026-04-09 07:31:11.366312
d2f77378-1c54-4de9-9da8-c5b86000c2bd	8648aa7c-5574-479f-9084-a9ca960b6855	2026-08-06	WORKDAY	\N	2026-04-09 07:31:11.366312	2026-04-09 07:31:11.366312
df4162f7-197b-4f3d-a04b-4134204727f9	8648aa7c-5574-479f-9084-a9ca960b6855	2026-08-07	WORKDAY	\N	2026-04-09 07:31:11.366312	2026-04-09 07:31:11.366312
24b8ed99-f586-43ac-a5d9-f6f6b0034b2a	8648aa7c-5574-479f-9084-a9ca960b6855	2026-08-08	RESTDAY	\N	2026-04-09 07:31:11.366312	2026-04-09 07:31:11.366312
d9fa5f77-42c3-4252-96db-e315a3697c6a	8648aa7c-5574-479f-9084-a9ca960b6855	2026-08-09	RESTDAY	\N	2026-04-09 07:31:11.366312	2026-04-09 07:31:11.366312
215528a1-3554-4263-a88d-5bcf86b01b4d	8648aa7c-5574-479f-9084-a9ca960b6855	2026-08-10	WORKDAY	\N	2026-04-09 07:31:11.366312	2026-04-09 07:31:11.366312
f83ff15f-ad82-40fb-9bdf-c5a86d4d02fb	8648aa7c-5574-479f-9084-a9ca960b6855	2026-08-11	WORKDAY	\N	2026-04-09 07:31:11.366312	2026-04-09 07:31:11.366312
9b3f3068-a037-40a7-8caa-2224bc92e8a7	8648aa7c-5574-479f-9084-a9ca960b6855	2026-08-12	WORKDAY	\N	2026-04-09 07:31:11.366312	2026-04-09 07:31:11.366312
52745ae2-38e6-4a89-8508-7efd4f12261c	8648aa7c-5574-479f-9084-a9ca960b6855	2026-08-13	WORKDAY	\N	2026-04-09 07:31:11.366312	2026-04-09 07:31:11.366312
054e6340-e5d7-42a7-b4f8-ecd51775ba1c	8648aa7c-5574-479f-9084-a9ca960b6855	2026-08-14	WORKDAY	\N	2026-04-09 07:31:11.366312	2026-04-09 07:31:11.366312
f14a2413-aa0d-4935-b909-0be1d65a5d3c	8648aa7c-5574-479f-9084-a9ca960b6855	2026-08-15	RESTDAY	\N	2026-04-09 07:31:11.366312	2026-04-09 07:31:11.366312
04d60b24-19cc-4d3a-a6b4-db2153e33220	8648aa7c-5574-479f-9084-a9ca960b6855	2026-08-16	RESTDAY	\N	2026-04-09 07:31:11.366312	2026-04-09 07:31:11.366312
e6c66f39-eb79-4c96-9de6-633990c8e27b	8648aa7c-5574-479f-9084-a9ca960b6855	2026-08-17	WORKDAY	\N	2026-04-09 07:31:11.366312	2026-04-09 07:31:11.366312
6f1c6816-83fe-43bf-b831-2e66ccd7fd14	8648aa7c-5574-479f-9084-a9ca960b6855	2026-08-18	WORKDAY	\N	2026-04-09 07:31:11.366312	2026-04-09 07:31:11.366312
7f6509e8-ac58-464d-8707-993295745444	8648aa7c-5574-479f-9084-a9ca960b6855	2026-08-19	WORKDAY	\N	2026-04-09 07:31:11.366312	2026-04-09 07:31:11.366312
05a31ca1-3116-4c0f-91ca-b495cc711973	8648aa7c-5574-479f-9084-a9ca960b6855	2026-08-20	WORKDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
1620849c-d65a-432d-ac37-98b003e7b87d	8648aa7c-5574-479f-9084-a9ca960b6855	2026-08-21	WORKDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
5d26749d-7862-47a1-b5bb-23836509c13e	8648aa7c-5574-479f-9084-a9ca960b6855	2026-08-22	RESTDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
d11e79eb-437f-497c-954a-415dfe7b9825	8648aa7c-5574-479f-9084-a9ca960b6855	2026-08-23	RESTDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
d429406d-df36-42f6-9c6d-591c809395cb	8648aa7c-5574-479f-9084-a9ca960b6855	2026-08-24	WORKDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
342f4582-6f39-4337-aa0c-5f001828be41	8648aa7c-5574-479f-9084-a9ca960b6855	2026-08-25	WORKDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
113cc930-c12e-4b08-a247-0e08a81f6fa7	8648aa7c-5574-479f-9084-a9ca960b6855	2026-08-26	WORKDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
bb9ff0d4-0b55-4463-b938-8eee2c35f7e2	8648aa7c-5574-479f-9084-a9ca960b6855	2026-08-27	WORKDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
86b74259-5219-44f5-a22e-5901ff46d3e9	8648aa7c-5574-479f-9084-a9ca960b6855	2026-08-28	WORKDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
ade9e51d-bcf7-460d-b00c-9ebb56fd5004	8648aa7c-5574-479f-9084-a9ca960b6855	2026-08-29	RESTDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
2d4b3703-b695-4169-af0f-99ba7f12df60	8648aa7c-5574-479f-9084-a9ca960b6855	2026-08-30	RESTDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
2839b86a-14e5-45b8-9aeb-02e713e0ec56	8648aa7c-5574-479f-9084-a9ca960b6855	2026-08-31	WORKDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
3af93e49-6b48-4a96-94f3-0a84ca66de6b	8648aa7c-5574-479f-9084-a9ca960b6855	2026-09-01	WORKDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
7c143767-6cb1-4969-a222-c6dc9f3a0fd3	8648aa7c-5574-479f-9084-a9ca960b6855	2026-09-02	WORKDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
410e02bf-8d1b-41ba-bc63-a8c1de46510d	8648aa7c-5574-479f-9084-a9ca960b6855	2026-09-03	WORKDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
4dd02aaf-7e2c-462b-9dc8-f9f1beddcf9d	8648aa7c-5574-479f-9084-a9ca960b6855	2026-09-04	WORKDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
b3a80ef4-011a-4a61-98e4-2d7f48d1b420	8648aa7c-5574-479f-9084-a9ca960b6855	2026-09-05	RESTDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
d7e07c17-99e6-43f0-a24a-b0fe294d431a	8648aa7c-5574-479f-9084-a9ca960b6855	2026-09-06	RESTDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
f52de20a-6399-40c9-a66e-c6e322ac19f5	8648aa7c-5574-479f-9084-a9ca960b6855	2026-09-07	WORKDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
1dc48424-e07e-4162-88d4-e36f5cb5d5ca	8648aa7c-5574-479f-9084-a9ca960b6855	2026-09-08	WORKDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
9550e1d4-2641-41d2-9708-36796f32774d	8648aa7c-5574-479f-9084-a9ca960b6855	2026-09-09	WORKDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
c12dde88-f3f4-48e3-86c1-8f80d2018074	8648aa7c-5574-479f-9084-a9ca960b6855	2026-09-10	WORKDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
777f2d44-b6bd-4848-9166-9451092a7d33	8648aa7c-5574-479f-9084-a9ca960b6855	2026-09-11	WORKDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
1feebe60-ca08-406a-82c6-bc6ca7529ad1	8648aa7c-5574-479f-9084-a9ca960b6855	2026-09-12	RESTDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
2ae67594-d3eb-483a-917c-3e8fe776546e	8648aa7c-5574-479f-9084-a9ca960b6855	2026-09-13	RESTDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
56a6de56-66a3-4474-9afb-d19846c80abf	8648aa7c-5574-479f-9084-a9ca960b6855	2026-09-14	WORKDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
fc60a37a-6666-484b-8487-8f5bde8d6ab9	8648aa7c-5574-479f-9084-a9ca960b6855	2026-09-15	WORKDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
fa32fd8c-35c3-426d-a501-8b0c19fdd842	8648aa7c-5574-479f-9084-a9ca960b6855	2026-09-16	WORKDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
2766ee07-6e76-4cb3-b14f-9d79292b4327	8648aa7c-5574-479f-9084-a9ca960b6855	2026-09-17	WORKDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
20fe85e7-ed54-4458-89dc-97432973c478	8648aa7c-5574-479f-9084-a9ca960b6855	2026-09-18	WORKDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
27c05b9c-27a7-465c-9651-8b440fc71f95	8648aa7c-5574-479f-9084-a9ca960b6855	2026-09-19	RESTDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
35f58aa4-d20f-4d35-b6c6-da68b882d6a3	8648aa7c-5574-479f-9084-a9ca960b6855	2026-09-20	RESTDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
aa101f40-8220-4451-807f-b12ef62ce751	8648aa7c-5574-479f-9084-a9ca960b6855	2026-09-21	WORKDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
47855555-3da0-4514-815e-9b140569938d	8648aa7c-5574-479f-9084-a9ca960b6855	2026-09-22	WORKDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
0206ef10-f818-4ba8-babc-52543b2dcc60	8648aa7c-5574-479f-9084-a9ca960b6855	2026-09-23	WORKDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
d11d6a59-db62-4c18-a4c0-2cfce82f20f6	8648aa7c-5574-479f-9084-a9ca960b6855	2026-09-24	WORKDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
cfc92571-26a4-48fb-b482-de148b8fa175	8648aa7c-5574-479f-9084-a9ca960b6855	2026-09-25	WORKDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
f5f81c10-54cb-414f-93c2-988ac9f2eeb0	8648aa7c-5574-479f-9084-a9ca960b6855	2026-09-26	RESTDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
66e3ac04-5a8d-457b-9824-d3feac383578	8648aa7c-5574-479f-9084-a9ca960b6855	2026-09-27	RESTDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
80fbe4c3-21bb-43b2-bc5e-1242cee07b21	8648aa7c-5574-479f-9084-a9ca960b6855	2026-09-28	WORKDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
6b2a01d6-22a4-43a1-b589-37e51085e1ef	8648aa7c-5574-479f-9084-a9ca960b6855	2026-09-29	WORKDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
2e5e7326-75c0-46c8-aa35-7d90a23a7c3b	8648aa7c-5574-479f-9084-a9ca960b6855	2026-09-30	WORKDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
3dc15e52-f820-4167-8617-77ce87ec9209	8648aa7c-5574-479f-9084-a9ca960b6855	2026-10-01	WORKDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
7a1ccc45-96c4-4c40-9f84-3702ed71ac16	8648aa7c-5574-479f-9084-a9ca960b6855	2026-10-02	WORKDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
ef7417f0-0346-4bbb-8e50-a37222d98a72	8648aa7c-5574-479f-9084-a9ca960b6855	2026-10-03	RESTDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
e12c06f3-36e8-4e34-8e1d-fae1565d388f	8648aa7c-5574-479f-9084-a9ca960b6855	2026-10-04	RESTDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
41336075-6cd2-4b4d-9ae3-416714aa6ee3	8648aa7c-5574-479f-9084-a9ca960b6855	2026-10-05	WORKDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
6b57a966-1766-43f8-a6ee-0dd678192339	8648aa7c-5574-479f-9084-a9ca960b6855	2026-10-06	WORKDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
bfe772a8-db0b-45ed-96bf-ff13577e8a18	8648aa7c-5574-479f-9084-a9ca960b6855	2026-10-07	WORKDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
2b87aeaa-af9f-4988-ae03-fa58315cc525	8648aa7c-5574-479f-9084-a9ca960b6855	2026-10-08	WORKDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
22bb3a03-e2c0-48c0-8691-ed3c708cb65e	8648aa7c-5574-479f-9084-a9ca960b6855	2026-10-09	WORKDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
6f83c916-c8b4-4759-bfc8-61a340c06d81	8648aa7c-5574-479f-9084-a9ca960b6855	2026-10-10	RESTDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
65b39042-6f88-4ea1-9730-58ff14eb75bb	8648aa7c-5574-479f-9084-a9ca960b6855	2026-10-11	RESTDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
ff6315e1-80e8-4aee-9911-04299531cd1c	8648aa7c-5574-479f-9084-a9ca960b6855	2026-10-12	WORKDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
b98a5fcb-cf28-4dfb-89b5-57c4e3c7fadd	8648aa7c-5574-479f-9084-a9ca960b6855	2026-10-13	WORKDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
fc1cbc34-f636-41c5-a590-ecc4433d9c47	8648aa7c-5574-479f-9084-a9ca960b6855	2026-10-14	WORKDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
a6602242-aac2-42a9-97f2-9df0c1f763fa	8648aa7c-5574-479f-9084-a9ca960b6855	2026-10-15	WORKDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
1eb554a3-673d-4611-823e-4bb87a6789d4	8648aa7c-5574-479f-9084-a9ca960b6855	2026-10-16	WORKDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
04e6e59b-066a-4597-95f8-6c671bff1b64	8648aa7c-5574-479f-9084-a9ca960b6855	2026-10-17	RESTDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
05347e93-c9b7-4b1a-8219-f5d5cb301e56	8648aa7c-5574-479f-9084-a9ca960b6855	2026-10-18	RESTDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
cb451a7b-5284-4278-aec0-10d542ab0ec0	8648aa7c-5574-479f-9084-a9ca960b6855	2026-10-19	WORKDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
07e94800-82e9-4ada-b0aa-c537f83c3513	8648aa7c-5574-479f-9084-a9ca960b6855	2026-10-20	WORKDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
52052fc8-c1e2-4455-87ec-657f1eb69404	8648aa7c-5574-479f-9084-a9ca960b6855	2026-10-21	WORKDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
5d11c393-d5c3-4b38-9d62-8c6b9566cee0	8648aa7c-5574-479f-9084-a9ca960b6855	2026-10-22	WORKDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
325811b7-e632-4f05-8961-c3b9908af1df	8648aa7c-5574-479f-9084-a9ca960b6855	2026-10-23	WORKDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
88897232-0cc8-4c9d-b0b5-6ee033f7f9a1	8648aa7c-5574-479f-9084-a9ca960b6855	2026-10-24	RESTDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
91bd0d85-0a6f-4341-9cad-1d7c1eb7d4c0	8648aa7c-5574-479f-9084-a9ca960b6855	2026-10-25	RESTDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
4cc63c01-27c4-41c1-b0e1-4fea998541e1	8648aa7c-5574-479f-9084-a9ca960b6855	2026-10-26	WORKDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
cdbbea03-f043-4e8f-b9f8-8720bb1dceb1	8648aa7c-5574-479f-9084-a9ca960b6855	2026-10-27	WORKDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
47a6c385-c8a3-4629-8e24-2ac33fa1b1af	8648aa7c-5574-479f-9084-a9ca960b6855	2026-10-28	WORKDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
fa13f692-d54a-418c-91b7-3f89112fbe52	8648aa7c-5574-479f-9084-a9ca960b6855	2026-10-29	WORKDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
81c1cf69-e407-4c0e-8a72-2e066231d370	8648aa7c-5574-479f-9084-a9ca960b6855	2026-10-30	WORKDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
1809c46c-d23c-4013-9fc5-580c43a91a9d	8648aa7c-5574-479f-9084-a9ca960b6855	2026-10-31	RESTDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
8004721b-2cfd-4600-9449-f4689597e76b	8648aa7c-5574-479f-9084-a9ca960b6855	2026-11-01	RESTDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
7a30cab8-eee0-4bdb-afa6-4aba9236593f	8648aa7c-5574-479f-9084-a9ca960b6855	2026-11-02	WORKDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
abe76f4c-49d7-45c0-bf89-dddc5b2b7e7a	8648aa7c-5574-479f-9084-a9ca960b6855	2026-11-03	WORKDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
ed331eef-fea7-4045-adde-9c76e7d6de48	8648aa7c-5574-479f-9084-a9ca960b6855	2026-11-04	WORKDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
be5570f1-0ddd-40c4-98db-b64523a32329	8648aa7c-5574-479f-9084-a9ca960b6855	2026-11-05	WORKDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
8e1accb5-39c4-40d9-84f2-381dfcc30c02	8648aa7c-5574-479f-9084-a9ca960b6855	2026-11-06	WORKDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
d69b6221-7251-4e6d-b88c-1694df205378	8648aa7c-5574-479f-9084-a9ca960b6855	2026-11-07	RESTDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
d98ff47d-0ac5-4367-814d-fec46bbc2642	8648aa7c-5574-479f-9084-a9ca960b6855	2026-11-08	RESTDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
062bbcff-64f6-4d95-ae8c-addefb81a2c4	8648aa7c-5574-479f-9084-a9ca960b6855	2026-11-09	WORKDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
9e860ec2-87af-4541-85d2-9a62f64d2183	8648aa7c-5574-479f-9084-a9ca960b6855	2026-11-10	WORKDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
1f6977b7-535f-4496-9d80-89cae25e5137	8648aa7c-5574-479f-9084-a9ca960b6855	2026-11-11	WORKDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
a9ed8912-8284-490c-9129-36c9e90163da	8648aa7c-5574-479f-9084-a9ca960b6855	2026-11-12	WORKDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
6466a876-3549-40de-be37-d00666028521	8648aa7c-5574-479f-9084-a9ca960b6855	2026-11-13	WORKDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
12d969db-0c64-4e85-a977-f8826bcb7e4a	8648aa7c-5574-479f-9084-a9ca960b6855	2026-11-14	RESTDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
6de5517b-1314-4975-b36d-cbf61b354267	8648aa7c-5574-479f-9084-a9ca960b6855	2026-11-15	RESTDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
9c58b6af-26f7-4193-8432-40e99b8ec9fa	8648aa7c-5574-479f-9084-a9ca960b6855	2026-11-16	WORKDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
c14f4bc9-4259-435f-86b6-c59119027fa8	8648aa7c-5574-479f-9084-a9ca960b6855	2026-11-17	WORKDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
a08a30ca-b389-4120-b442-d972839c7d45	8648aa7c-5574-479f-9084-a9ca960b6855	2026-11-18	WORKDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
e1f7c731-6fc3-4286-a28a-63a54386e112	8648aa7c-5574-479f-9084-a9ca960b6855	2026-11-19	WORKDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
004e3150-1261-4281-abab-6f59f4d20f0b	8648aa7c-5574-479f-9084-a9ca960b6855	2026-11-20	WORKDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
b6b276a1-7803-4b63-8bc8-c23574ac49ff	8648aa7c-5574-479f-9084-a9ca960b6855	2026-11-21	RESTDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
2d2a3102-2aa9-4bc9-bb13-78a649516e6a	8648aa7c-5574-479f-9084-a9ca960b6855	2026-11-22	RESTDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
da3131b0-0838-4f0d-8109-1f46538b63ef	8648aa7c-5574-479f-9084-a9ca960b6855	2026-11-23	WORKDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
0d4ef77c-3786-4e84-b4f6-cec05102d97d	8648aa7c-5574-479f-9084-a9ca960b6855	2026-11-24	WORKDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
3236d722-e7e9-4b35-b709-616d4c2b8c8d	8648aa7c-5574-479f-9084-a9ca960b6855	2026-11-25	WORKDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
0d960a20-6661-477c-8b55-21b5fd093bf7	8648aa7c-5574-479f-9084-a9ca960b6855	2026-11-26	WORKDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
b42d31a2-3d08-4755-8b83-cae02234b887	8648aa7c-5574-479f-9084-a9ca960b6855	2026-11-27	WORKDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
0b330e50-94a8-4d34-8163-ed1246468acc	8648aa7c-5574-479f-9084-a9ca960b6855	2026-11-28	RESTDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
34f62086-05a4-493c-9750-0d03637bb9c8	8648aa7c-5574-479f-9084-a9ca960b6855	2026-11-29	RESTDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
8db2a02c-16bc-4286-a385-1ffe3701d45d	8648aa7c-5574-479f-9084-a9ca960b6855	2026-11-30	WORKDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
d48f7561-ed18-4420-a69e-3ef9900cf9a4	8648aa7c-5574-479f-9084-a9ca960b6855	2026-12-01	WORKDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
b1e919a1-0ab1-4cf6-96ff-baf4dc91f18e	8648aa7c-5574-479f-9084-a9ca960b6855	2026-12-02	WORKDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
30e3211d-b08e-42de-8b18-a77b63b8505d	8648aa7c-5574-479f-9084-a9ca960b6855	2026-12-03	WORKDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
ed7ccdd9-e575-4ebd-bd40-3a8e7fd69d92	8648aa7c-5574-479f-9084-a9ca960b6855	2026-12-04	WORKDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
dd09c817-9dff-4bfc-84c6-2755cfdec7ac	8648aa7c-5574-479f-9084-a9ca960b6855	2026-12-05	RESTDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
5dba2d9b-4ea6-4737-bc1a-989511cee3ff	8648aa7c-5574-479f-9084-a9ca960b6855	2026-12-06	RESTDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
cc475243-48c0-44b7-81ed-29e543b1e5ac	8648aa7c-5574-479f-9084-a9ca960b6855	2026-12-07	WORKDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
bfb61cec-b2e9-4726-809d-fc9a5961b23a	8648aa7c-5574-479f-9084-a9ca960b6855	2026-12-08	WORKDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
7462038d-1547-4c4f-9287-b37c7df1108a	8648aa7c-5574-479f-9084-a9ca960b6855	2026-12-09	WORKDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
ca056036-b6e4-4b4d-b930-3f8c55ba6fb1	8648aa7c-5574-479f-9084-a9ca960b6855	2026-12-10	WORKDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
b79131ed-4ca7-4efa-ace5-560bb8f07577	8648aa7c-5574-479f-9084-a9ca960b6855	2026-12-11	WORKDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
04b6e2f0-f86f-4776-8057-42d9ac5ac03c	8648aa7c-5574-479f-9084-a9ca960b6855	2026-12-12	RESTDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
3caa88ba-cb09-47b9-b003-8f63142daf94	8648aa7c-5574-479f-9084-a9ca960b6855	2026-12-13	RESTDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
548f4f47-aed9-4aaa-9481-8b18290fb93a	8648aa7c-5574-479f-9084-a9ca960b6855	2026-12-14	WORKDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
bb8b85ae-d01e-4ee0-99a0-a049d488bd73	8648aa7c-5574-479f-9084-a9ca960b6855	2026-12-15	WORKDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
9417d852-c687-4d9b-ba06-251aa0acf41b	8648aa7c-5574-479f-9084-a9ca960b6855	2026-12-16	WORKDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
998adc0a-5763-4938-9d58-d2fd760b7481	8648aa7c-5574-479f-9084-a9ca960b6855	2026-12-17	WORKDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
93740b19-0f03-4741-ab88-19662623a5ad	8648aa7c-5574-479f-9084-a9ca960b6855	2026-12-18	WORKDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
e8c1b8fc-b3e1-43a5-bcb7-2124293443df	8648aa7c-5574-479f-9084-a9ca960b6855	2026-12-19	RESTDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
366c78a5-9b47-4367-8988-2a64e79bec6e	8648aa7c-5574-479f-9084-a9ca960b6855	2026-12-20	RESTDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
cda66cb1-66d7-4714-b983-b31d7e546d5b	8648aa7c-5574-479f-9084-a9ca960b6855	2026-12-21	WORKDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
37dc77f5-e7d6-4a0b-8329-b225bd134dcd	8648aa7c-5574-479f-9084-a9ca960b6855	2026-12-22	WORKDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
767c9131-0ee7-44ae-8248-6f3a66279595	8648aa7c-5574-479f-9084-a9ca960b6855	2026-12-23	WORKDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
41a8e275-1056-4274-b9c6-18cd02b55f2d	8648aa7c-5574-479f-9084-a9ca960b6855	2026-12-24	WORKDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
2c701545-15cc-4e5c-8df7-695e39b524f5	8648aa7c-5574-479f-9084-a9ca960b6855	2026-12-25	WORKDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
586f562f-f53a-45b5-93d4-b5cac3d91ed6	8648aa7c-5574-479f-9084-a9ca960b6855	2026-12-26	RESTDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
5dcf274d-3355-4a34-b090-4a2a89aa27cd	8648aa7c-5574-479f-9084-a9ca960b6855	2026-12-27	RESTDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
83c26e3d-27cc-4552-bd48-8b6a32328304	8648aa7c-5574-479f-9084-a9ca960b6855	2026-12-28	WORKDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
eebb6737-e9a9-4141-b693-4dd010a80cc7	8648aa7c-5574-479f-9084-a9ca960b6855	2026-12-29	WORKDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
a868868a-740e-4674-b6cb-bee8be9f30e3	8648aa7c-5574-479f-9084-a9ca960b6855	2026-12-30	WORKDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
56b9ab0c-e8c9-4751-b86a-da53607a559e	8648aa7c-5574-479f-9084-a9ca960b6855	2026-12-31	WORKDAY	\N	2026-04-09 07:31:11.367312	2026-04-09 07:31:11.367312
36849dbb-c244-4a76-ab4f-218495fcf3eb	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-12-31	WORKDAY	\N	2026-04-09 09:05:55.522543	2026-04-09 09:05:55.522543
83509e7b-d9aa-400a-9919-34b433edaa4f	8648aa7c-5574-479f-9084-a9ca960b6855	2026-04-04	HOLIDAY	清明节	2026-04-09 07:31:11.333851	2026-04-09 07:31:11.333851
7a8d1aa3-14ea-4263-897e-6605da1af35f	8648aa7c-5574-479f-9084-a9ca960b6855	2026-04-05	HOLIDAY	清明节	2026-04-09 07:31:11.333851	2026-04-09 07:31:11.333851
5b0e7f61-4adf-4e28-ac1d-08b3c2dcb184	8648aa7c-5574-479f-9084-a9ca960b6855	2026-04-06	HOLIDAY	清明节	2026-04-09 07:31:11.333851	2026-04-09 07:31:11.333851
4bd4928f-0d9e-4b5e-a79d-8ce5ac17a686	8648aa7c-5574-479f-9084-a9ca960b6855	2026-02-11	HOLIDAY		2026-04-09 07:31:11.333851	2026-04-09 07:31:11.333851
21481533-1082-4bcb-8663-add16c2dd068	8648aa7c-5574-479f-9084-a9ca960b6855	2026-02-12	HOLIDAY		2026-04-09 07:31:11.333851	2026-04-09 07:31:11.333851
b45fc30b-0f9e-427b-b228-aa1c8fe643b1	8648aa7c-5574-479f-9084-a9ca960b6855	2026-02-13	HOLIDAY		2026-04-09 07:31:11.333851	2026-04-09 07:31:11.333851
7b6ca7d0-1dd1-4ef5-9694-e47720ab49f6	8648aa7c-5574-479f-9084-a9ca960b6855	2026-02-14	HOLIDAY		2026-04-09 07:31:11.333851	2026-04-09 07:31:11.333851
ea711f78-158d-465f-a2ef-8da26c915cd2	8648aa7c-5574-479f-9084-a9ca960b6855	2026-02-15	HOLIDAY		2026-04-09 07:31:11.333851	2026-04-09 07:31:11.333851
f16edea0-aa86-43db-9db1-1794a7d11b72	8648aa7c-5574-479f-9084-a9ca960b6855	2026-02-16	HOLIDAY		2026-04-09 07:31:11.333851	2026-04-09 07:31:11.333851
a5fed086-7f51-4f7c-a687-3da8240d7cc3	8648aa7c-5574-479f-9084-a9ca960b6855	2026-02-17	HOLIDAY		2026-04-09 07:31:11.333851	2026-04-09 07:31:11.333851
e396af99-8a6a-40c1-924c-c83c1f658dbd	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-01-03	WORKDAY	\N	2026-04-09 09:05:55.498537	2026-04-09 09:05:55.498537
8b1923d0-567e-4d05-852b-59974a8bed96	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-01-10	WORKDAY	\N	2026-04-09 09:05:55.499539	2026-04-09 09:05:55.499539
7f6babba-48ce-405a-93b6-f706cd760432	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-01-17	WORKDAY	\N	2026-04-09 09:05:55.499539	2026-04-09 09:05:55.499539
511f305d-c600-44e8-b392-da6ace2aa590	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-01-24	WORKDAY	\N	2026-04-09 09:05:55.500536	2026-04-09 09:05:55.500536
c9d9a22d-294f-4c0d-b6a7-7218e1c167d6	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-01-31	WORKDAY	\N	2026-04-09 09:05:55.500536	2026-04-09 09:05:55.500536
7fb857ba-5f0c-4fea-856f-9f14f781f45c	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-02-07	WORKDAY	\N	2026-04-09 09:05:55.501535	2026-04-09 09:05:55.501535
40bb4bc1-c08e-4228-9588-c64bb955ffbb	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-02-14	WORKDAY	\N	2026-04-09 09:05:55.502537	2026-04-09 09:05:55.502537
e0ab8c60-dce9-4df6-93c8-a47cea341606	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-02-21	WORKDAY	\N	2026-04-09 09:05:55.502537	2026-04-09 09:05:55.502537
855d3ab6-37db-40f9-80a2-45301332f2fc	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-02-28	WORKDAY	\N	2026-04-09 09:05:55.502537	2026-04-09 09:05:55.502537
1b393e74-608f-4262-976d-b5a90c4bfdb3	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-03-07	WORKDAY	\N	2026-04-09 09:05:55.503536	2026-04-09 09:05:55.503536
a5e223d3-7323-4e26-b201-f3a25afb44a8	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-03-14	WORKDAY	\N	2026-04-09 09:05:55.504048	2026-04-09 09:05:55.504048
7ce745a4-faf3-44d3-a95e-b5d0e3b7b663	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-03-21	WORKDAY	\N	2026-04-09 09:05:55.50472	2026-04-09 09:05:55.50472
5dfceab1-2ac1-4238-93d9-dededc2af758	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-03-28	WORKDAY	\N	2026-04-09 09:05:55.505512	2026-04-09 09:05:55.505512
17c2e634-b493-4ad2-aefa-e0a0a0c67c8c	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-04-04	WORKDAY	\N	2026-04-09 09:05:55.505512	2026-04-09 09:05:55.505512
0975236a-894d-4cee-9e54-efefdd726e92	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-04-11	WORKDAY	\N	2026-04-09 09:05:55.505512	2026-04-09 09:05:55.505512
fe95cc69-236c-4390-a91f-9f158febb323	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-04-18	WORKDAY	\N	2026-04-09 09:05:55.505512	2026-04-09 09:05:55.505512
67be67c4-31a8-4b30-8211-f6511fce465e	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-04-25	WORKDAY	\N	2026-04-09 09:05:55.506731	2026-04-09 09:05:55.506731
aa1ce9da-0410-42d2-bab8-655743c11f1a	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-05-02	WORKDAY	\N	2026-04-09 09:05:55.510244	2026-04-09 09:05:55.510244
a6e7dff5-be36-45d0-8667-3cef41d874be	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-05-09	WORKDAY	\N	2026-04-09 09:05:55.511092	2026-04-09 09:05:55.511092
af4879b2-80b7-40b8-a9a2-1812887378a6	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-05-16	WORKDAY	\N	2026-04-09 09:05:55.511092	2026-04-09 09:05:55.511092
ebf059e1-b871-4e7f-a786-c53082db331e	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-05-23	WORKDAY	\N	2026-04-09 09:05:55.511597	2026-04-09 09:05:55.511597
3fbb8bad-0745-48f4-8c26-53c347783001	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-05-30	WORKDAY	\N	2026-04-09 09:05:55.512607	2026-04-09 09:05:55.512607
b1905ce0-c76b-44ec-8f2b-0b2e3898b6f8	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-06-06	WORKDAY	\N	2026-04-09 09:05:55.512607	2026-04-09 09:05:55.512607
ee2598ce-00da-444e-95a3-29e66f4997be	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-06-13	WORKDAY	\N	2026-04-09 09:05:55.513146	2026-04-09 09:05:55.513146
98c97a04-ea85-4d93-9897-a2abe89a3b32	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-06-20	WORKDAY	\N	2026-04-09 09:05:55.513654	2026-04-09 09:05:55.513654
1f5ebbf4-680e-4fa4-882c-4342036fe2f3	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-06-27	WORKDAY	\N	2026-04-09 09:05:55.513654	2026-04-09 09:05:55.513654
cca792f1-43ed-4dde-a950-c89489b1ae9e	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-07-04	WORKDAY	\N	2026-04-09 09:05:55.514177	2026-04-09 09:05:55.514177
aaa3281c-4002-42d0-a912-14954b20fe30	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-07-11	WORKDAY	\N	2026-04-09 09:05:55.514177	2026-04-09 09:05:55.514177
275ede7a-8594-40d7-8ed9-c855104aa11e	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-07-18	WORKDAY	\N	2026-04-09 09:05:55.514177	2026-04-09 09:05:55.514177
a78f5330-8565-4c6d-b9cd-a7ac10dd6a1e	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-07-25	WORKDAY	\N	2026-04-09 09:05:55.514177	2026-04-09 09:05:55.514177
3f7dbb85-dd63-4f78-b3be-9ee967665038	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-08-01	WORKDAY	\N	2026-04-09 09:05:55.5157	2026-04-09 09:05:55.5157
2f6c1ee1-34bd-4556-a9e0-0be92a6a3b76	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-08-08	WORKDAY	\N	2026-04-09 09:05:55.516072	2026-04-09 09:05:55.516072
f9002258-0e09-4b44-ac4b-66f4a7c9dc4b	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-08-15	WORKDAY	\N	2026-04-09 09:05:55.516072	2026-04-09 09:05:55.516072
a6bbbeb3-73d7-4239-9072-104646b1b64c	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-08-22	WORKDAY	\N	2026-04-09 09:05:55.516712	2026-04-09 09:05:55.516712
ceac4e1d-039b-4ba9-b04e-609c69550e4f	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-08-29	WORKDAY	\N	2026-04-09 09:05:55.516712	2026-04-09 09:05:55.516712
226d5034-e2ff-42e9-b62d-3b061c4de802	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-09-05	WORKDAY	\N	2026-04-09 09:05:55.517711	2026-04-09 09:05:55.517711
78aa5a08-1eaf-4dac-ba91-6d1a42c76d3d	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-09-12	WORKDAY	\N	2026-04-09 09:05:55.517711	2026-04-09 09:05:55.517711
a85cf241-2631-48fd-a640-7bf66a4da03b	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-09-19	WORKDAY	\N	2026-04-09 09:05:55.517711	2026-04-09 09:05:55.517711
d4818735-91ad-42e9-8cc7-8502ae095f7c	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-09-26	WORKDAY	\N	2026-04-09 09:05:55.517711	2026-04-09 09:05:55.517711
f4476116-8fec-4275-9d08-1e7dfdfcabb9	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-10-03	WORKDAY	\N	2026-04-09 09:05:55.517711	2026-04-09 09:05:55.517711
b6864bbf-a40d-4f7e-931f-86dde5fb3d12	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-01-03	WORKDAY	\N	2026-04-10 15:47:55.292826	2026-04-10 15:47:55.292826
ffcf0bce-168b-406c-a488-febe988f281c	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-01-10	WORKDAY	\N	2026-04-10 15:47:55.293334	2026-04-10 15:47:55.293334
1db2eccd-87cf-4f46-a9b5-5971fd991a33	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-01-17	WORKDAY	\N	2026-04-10 15:47:55.294336	2026-04-10 15:47:55.294336
e4d10592-baa8-43a7-9992-13581b18d40e	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-01-24	WORKDAY	\N	2026-04-10 15:47:55.294336	2026-04-10 15:47:55.294336
73fa0577-5fcf-4fde-99d1-4f05d7ef67c6	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-01-31	WORKDAY	\N	2026-04-10 15:47:55.295336	2026-04-10 15:47:55.295336
e0de4092-5050-448f-8035-f4319b4109c7	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-02-07	WORKDAY	\N	2026-04-10 15:47:55.295841	2026-04-10 15:47:55.295841
fa1c259a-7f04-4c3f-a2b2-321fac75f801	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-02-14	WORKDAY	\N	2026-04-10 15:47:55.296345	2026-04-10 15:47:55.296345
e07a9cba-93a2-4052-a6b3-78a20e3ea662	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-02-21	WORKDAY	\N	2026-04-10 15:47:55.299189	2026-04-10 15:47:55.299189
15de742a-2c03-4f12-828e-8fbee4a35f7f	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-10-10	WORKDAY	\N	2026-04-09 09:05:55.519327	2026-04-09 09:05:55.519327
e793c7f0-812a-4e95-82ca-1ecdd4c248fb	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-10-17	WORKDAY	\N	2026-04-09 09:05:55.519327	2026-04-09 09:05:55.519327
32a779b6-06b6-4b15-92a5-dd8954d666c3	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-10-24	WORKDAY	\N	2026-04-09 09:05:55.519327	2026-04-09 09:05:55.519327
6c7b5d65-d4e6-4cbb-9dce-dff5bc92b284	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-10-31	WORKDAY	\N	2026-04-09 09:05:55.519327	2026-04-09 09:05:55.519327
d62f33c5-0bd5-4b42-958a-538f8123cd4c	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-11-07	WORKDAY	\N	2026-04-09 09:05:55.520338	2026-04-09 09:05:55.520338
a446358a-d0e8-4bd6-b412-6aeed67893b0	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-11-14	WORKDAY	\N	2026-04-09 09:05:55.520338	2026-04-09 09:05:55.520338
1f8a2b3a-1405-4d50-b4e4-fc42a55f33e5	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-11-21	WORKDAY	\N	2026-04-09 09:05:55.520338	2026-04-09 09:05:55.520338
7b1116e5-e19e-4f3b-9461-b63da3a6503a	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-11-28	WORKDAY	\N	2026-04-09 09:05:55.521544	2026-04-09 09:05:55.521544
ccc0bdd6-cbaa-4de2-b517-a812759ad7a4	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-12-05	WORKDAY	\N	2026-04-09 09:05:55.521544	2026-04-09 09:05:55.521544
2a506010-c09f-46d2-88a1-0c2cd7719486	a73e562e-fc18-4ac3-b966-d67799e3ae80	2026-12-12	WORKDAY	\N	2026-04-09 09:05:55.522035	2026-04-09 09:05:55.522035
0ff03a4b-2424-483c-b250-bbb44cd668b7	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-01-01	WORKDAY	\N	2026-04-10 15:47:55.291827	2026-04-10 15:47:55.291827
a8c4869b-695a-4b67-9bcc-7197d6d9afec	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-01-02	WORKDAY	\N	2026-04-10 15:47:55.292826	2026-04-10 15:47:55.292826
75a0c81c-101a-462d-aeed-87f47c50c5a1	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-01-04	RESTDAY	\N	2026-04-10 15:47:55.292826	2026-04-10 15:47:55.292826
6c8f91f6-5286-4eab-856c-923e11236e37	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-01-05	WORKDAY	\N	2026-04-10 15:47:55.293334	2026-04-10 15:47:55.293334
0dae2ade-45ec-42fe-b3ca-1ee91bb7a56b	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-01-06	WORKDAY	\N	2026-04-10 15:47:55.293334	2026-04-10 15:47:55.293334
eb0a4568-faa8-4951-8875-358a5b288842	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-01-07	WORKDAY	\N	2026-04-10 15:47:55.293334	2026-04-10 15:47:55.293334
26987962-ee4e-4d68-871c-c6eb70db8774	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-01-08	WORKDAY	\N	2026-04-10 15:47:55.293334	2026-04-10 15:47:55.293334
8047557c-912e-472c-ac90-9caeaee6fbd7	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-01-09	WORKDAY	\N	2026-04-10 15:47:55.293334	2026-04-10 15:47:55.293334
99605c09-6db6-4324-8751-a3bcdbbd4ac7	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-01-11	RESTDAY	\N	2026-04-10 15:47:55.293334	2026-04-10 15:47:55.293334
c61a0044-eb67-457d-8ec8-021dd8a3d3d2	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-01-12	WORKDAY	\N	2026-04-10 15:47:55.294336	2026-04-10 15:47:55.294336
281ca72b-19bd-4bf0-9956-5c4eb7df9d9c	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-01-13	WORKDAY	\N	2026-04-10 15:47:55.294336	2026-04-10 15:47:55.294336
c150509e-1b6f-4ad2-8a76-7fe5e1052811	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-01-14	WORKDAY	\N	2026-04-10 15:47:55.294336	2026-04-10 15:47:55.294336
a6ea9ea3-c03b-4567-af31-f49234fe98ae	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-01-15	WORKDAY	\N	2026-04-10 15:47:55.294336	2026-04-10 15:47:55.294336
1479bb84-bd54-4235-8de5-ab1fb0138cdd	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-01-16	WORKDAY	\N	2026-04-10 15:47:55.294336	2026-04-10 15:47:55.294336
cd20b972-94c3-45ef-ba52-bdc574174c37	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-01-18	RESTDAY	\N	2026-04-10 15:47:55.294336	2026-04-10 15:47:55.294336
f8e1d6be-9c5a-437f-a5fa-963156e6737d	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-01-19	WORKDAY	\N	2026-04-10 15:47:55.294336	2026-04-10 15:47:55.294336
7d57c9ed-fe3b-4bb4-9125-1e6b622c276f	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-01-20	WORKDAY	\N	2026-04-10 15:47:55.294336	2026-04-10 15:47:55.294336
3b0db669-1dca-46e8-a196-cf0e7eec000c	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-01-21	WORKDAY	\N	2026-04-10 15:47:55.294336	2026-04-10 15:47:55.294336
ebc3e4e2-6045-4495-9c35-0b24add721d3	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-01-22	WORKDAY	\N	2026-04-10 15:47:55.294336	2026-04-10 15:47:55.294336
3b935942-40a9-4f57-95a2-19641123aa3f	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-01-23	WORKDAY	\N	2026-04-10 15:47:55.294336	2026-04-10 15:47:55.294336
4d3fb87e-8b0d-4ff6-8180-160029b8d5c1	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-01-25	RESTDAY	\N	2026-04-10 15:47:55.295336	2026-04-10 15:47:55.295336
22f84fab-5779-410b-ad62-2899adcdf312	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-01-26	WORKDAY	\N	2026-04-10 15:47:55.295336	2026-04-10 15:47:55.295336
0a06517a-7b40-47ca-b1b3-61cf73142001	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-01-27	WORKDAY	\N	2026-04-10 15:47:55.295336	2026-04-10 15:47:55.295336
561f79b0-e7a4-4fc7-a2a2-a4d22da3f9f8	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-01-28	WORKDAY	\N	2026-04-10 15:47:55.295336	2026-04-10 15:47:55.295336
657f8286-ab39-4575-bee4-fa377f1bb56d	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-01-29	WORKDAY	\N	2026-04-10 15:47:55.295336	2026-04-10 15:47:55.295336
0aff200a-1653-4635-aa22-4989756fc12e	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-01-30	WORKDAY	\N	2026-04-10 15:47:55.295336	2026-04-10 15:47:55.295336
253e04b2-00d9-4df0-9e8d-a251fc27bb8e	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-02-01	RESTDAY	\N	2026-04-10 15:47:55.295336	2026-04-10 15:47:55.295336
0723dc6d-85ac-4b71-88cf-eab67f197386	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-02-02	WORKDAY	\N	2026-04-10 15:47:55.295336	2026-04-10 15:47:55.295336
2d291bfc-f81e-4e4b-9fd7-171438255039	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-02-03	WORKDAY	\N	2026-04-10 15:47:55.295336	2026-04-10 15:47:55.295336
745800cb-c5ac-41c5-8641-4fb1064f8cc6	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-02-04	WORKDAY	\N	2026-04-10 15:47:55.295841	2026-04-10 15:47:55.295841
c2da8bad-6b67-4b0a-9cf2-31c4b1bdbea6	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-02-05	WORKDAY	\N	2026-04-10 15:47:55.295841	2026-04-10 15:47:55.295841
8ec097fa-5410-4f85-9257-35d0c31e7829	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-02-06	WORKDAY	\N	2026-04-10 15:47:55.295841	2026-04-10 15:47:55.295841
d78219cb-b2a2-48b7-9119-5146e442ef10	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-02-08	RESTDAY	\N	2026-04-10 15:47:55.295841	2026-04-10 15:47:55.295841
cbab37b3-a076-43f0-9d06-6159869d8cb1	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-02-09	WORKDAY	\N	2026-04-10 15:47:55.296345	2026-04-10 15:47:55.296345
07907980-9962-41a5-90c2-db5ca2afc937	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-02-10	WORKDAY	\N	2026-04-10 15:47:55.296345	2026-04-10 15:47:55.296345
29517446-0d3a-4450-8717-3da5dd43a655	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-02-11	WORKDAY	\N	2026-04-10 15:47:55.296345	2026-04-10 15:47:55.296345
724a25f3-2f73-4506-9d9c-4b7f2db4916c	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-02-12	WORKDAY	\N	2026-04-10 15:47:55.296345	2026-04-10 15:47:55.296345
eac3f401-d2fe-4ce8-89a0-328fabe4ef60	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-02-13	WORKDAY	\N	2026-04-10 15:47:55.296345	2026-04-10 15:47:55.296345
0e24ef3a-691b-47ae-b319-f9fb15b128ab	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-02-15	RESTDAY	\N	2026-04-10 15:47:55.296886	2026-04-10 15:47:55.296886
154bdf9d-bf22-484f-a578-581614a88ac6	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-02-16	WORKDAY	\N	2026-04-10 15:47:55.296886	2026-04-10 15:47:55.296886
5ba6bf03-1353-454c-bb20-d66d60c7a1ca	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-02-17	WORKDAY	\N	2026-04-10 15:47:55.296886	2026-04-10 15:47:55.296886
b675a06c-c448-417c-80be-7cac62e40ae7	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-02-18	WORKDAY	\N	2026-04-10 15:47:55.296886	2026-04-10 15:47:55.296886
800e9369-c07d-4445-8ffc-84d68f3b1417	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-02-19	WORKDAY	\N	2026-04-10 15:47:55.298397	2026-04-10 15:47:55.298397
00f59c5b-a9cc-4ec9-8466-8205550a84c5	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-02-20	WORKDAY	\N	2026-04-10 15:47:55.298397	2026-04-10 15:47:55.298397
5c2e43eb-97b0-4531-93d0-c29fa0f17643	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-02-22	RESTDAY	\N	2026-04-10 15:47:55.300194	2026-04-10 15:47:55.300194
1de94e7b-e03c-4c07-8afa-98c057f87878	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-02-23	WORKDAY	\N	2026-04-10 15:47:55.301194	2026-04-10 15:47:55.301194
7855297d-c572-4bab-8d74-b019d8d9a089	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-02-24	WORKDAY	\N	2026-04-10 15:47:55.301194	2026-04-10 15:47:55.301194
259c3891-02b3-4ce2-afc8-69a467c0e53b	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-02-25	WORKDAY	\N	2026-04-10 15:47:55.301194	2026-04-10 15:47:55.301194
2696f6e2-0997-4781-b0f7-361cac93b9e8	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-02-26	WORKDAY	\N	2026-04-10 15:47:55.301194	2026-04-10 15:47:55.301194
4f1189b9-94d7-4a32-b26e-49cb44e8201c	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-02-27	WORKDAY	\N	2026-04-10 15:47:55.301194	2026-04-10 15:47:55.301194
26b33fef-7958-42b3-a03f-7f649c8c073a	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-03-01	RESTDAY	\N	2026-04-10 15:47:55.301194	2026-04-10 15:47:55.301194
3ee786d7-9d31-4290-ace1-91ae9da172c5	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-03-02	WORKDAY	\N	2026-04-10 15:47:55.301194	2026-04-10 15:47:55.301194
c2cb234f-87f0-420a-a5a8-fe95eb2d3d83	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-03-03	WORKDAY	\N	2026-04-10 15:47:55.301194	2026-04-10 15:47:55.301194
a0815989-26d9-4657-a0bd-44c520535cea	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-03-04	WORKDAY	\N	2026-04-10 15:47:55.301194	2026-04-10 15:47:55.301194
3ae1ea1c-94cd-40ee-90a9-8dbb3af29899	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-03-05	WORKDAY	\N	2026-04-10 15:47:55.301194	2026-04-10 15:47:55.301194
b97f146e-0c69-47b1-9777-9951de1a2d4d	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-03-06	WORKDAY	\N	2026-04-10 15:47:55.30178	2026-04-10 15:47:55.30178
2f5b73a1-a83e-4c97-b511-99f1803e80b1	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-03-08	RESTDAY	\N	2026-04-10 15:47:55.30178	2026-04-10 15:47:55.30178
70389c3e-5f3b-4d61-aba1-42c0b379e33e	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-03-09	WORKDAY	\N	2026-04-10 15:47:55.30178	2026-04-10 15:47:55.30178
8859eb93-f6f0-4fa8-b864-1b3750006c29	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-03-10	WORKDAY	\N	2026-04-10 15:47:55.30178	2026-04-10 15:47:55.30178
f2c4dea8-a11e-4425-8e13-ea30aa895f86	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-03-11	WORKDAY	\N	2026-04-10 15:47:55.30178	2026-04-10 15:47:55.30178
1d3d9f8c-6859-417a-91a9-8e0ff9c07ee1	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-03-12	WORKDAY	\N	2026-04-10 15:47:55.30178	2026-04-10 15:47:55.30178
89137252-06d6-4fc6-a136-765cb80ea76e	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-03-13	WORKDAY	\N	2026-04-10 15:47:55.30178	2026-04-10 15:47:55.30178
d596d19f-b7fd-4569-b001-6228abdfd0b9	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-03-15	RESTDAY	\N	2026-04-10 15:47:55.30178	2026-04-10 15:47:55.30178
34291fca-78f9-44b2-95a3-2b1cc1bb7e50	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-03-16	WORKDAY	\N	2026-04-10 15:47:55.30178	2026-04-10 15:47:55.30178
e4949e60-41f7-4945-a419-b163ea7154c3	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-03-17	WORKDAY	\N	2026-04-10 15:47:55.30178	2026-04-10 15:47:55.30178
1bb1959e-d329-4de5-8451-d8a2f00e86d1	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-03-18	WORKDAY	\N	2026-04-10 15:47:55.30178	2026-04-10 15:47:55.30178
3dfdce28-89ff-4794-94b2-7f7f9ef9c45a	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-03-19	WORKDAY	\N	2026-04-10 15:47:55.30178	2026-04-10 15:47:55.30178
97026543-e306-4550-b301-865c42f064ad	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-03-20	WORKDAY	\N	2026-04-10 15:47:55.30178	2026-04-10 15:47:55.30178
5277b8f4-fd36-411a-8257-e322f5c5acce	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-03-22	RESTDAY	\N	2026-04-10 15:47:55.30178	2026-04-10 15:47:55.30178
59fe28a5-f988-4494-a5c7-4a22c288af3d	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-03-23	WORKDAY	\N	2026-04-10 15:47:55.30178	2026-04-10 15:47:55.30178
0340aaef-030a-4bba-8440-e20aa3358c30	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-03-24	WORKDAY	\N	2026-04-10 15:47:55.30178	2026-04-10 15:47:55.30178
3ca1de8c-23bb-45e6-8429-eafe02392293	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-03-25	WORKDAY	\N	2026-04-10 15:47:55.30178	2026-04-10 15:47:55.30178
489ec0a7-f76e-4d69-822d-296bc45b11a6	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-03-26	WORKDAY	\N	2026-04-10 15:47:55.30178	2026-04-10 15:47:55.30178
136ac83c-50f3-4216-a6fd-fc3a42e2e323	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-03-27	WORKDAY	\N	2026-04-10 15:47:55.30178	2026-04-10 15:47:55.30178
031840e7-089d-4c5e-bfdf-c27d08a7d4eb	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-03-29	RESTDAY	\N	2026-04-10 15:47:55.30178	2026-04-10 15:47:55.30178
b7860a48-8cfa-4df2-b81e-86220db94910	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-03-30	WORKDAY	\N	2026-04-10 15:47:55.30178	2026-04-10 15:47:55.30178
a0c4ab8d-5e18-4060-8663-703db358873b	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-03-31	WORKDAY	\N	2026-04-10 15:47:55.302786	2026-04-10 15:47:55.302786
22fcbdf3-bf97-498d-a556-8eabe463e02c	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-04-01	WORKDAY	\N	2026-04-10 15:47:55.302786	2026-04-10 15:47:55.302786
e67da666-5da7-487f-b30d-b4df90c346f1	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-04-02	WORKDAY	\N	2026-04-10 15:47:55.302786	2026-04-10 15:47:55.302786
4c5d8f4d-189a-4c80-b06a-390ae2b036d9	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-04-03	WORKDAY	\N	2026-04-10 15:47:55.302786	2026-04-10 15:47:55.302786
7c8f4903-1778-4fad-ab85-6e7fa2ddf00a	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-04-05	RESTDAY	\N	2026-04-10 15:47:55.302786	2026-04-10 15:47:55.302786
b050aef6-0085-4d91-916e-aaaf899d389d	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-04-06	WORKDAY	\N	2026-04-10 15:47:55.302786	2026-04-10 15:47:55.302786
26acb3c0-2ecf-4289-ac82-de0102c0b747	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-04-07	WORKDAY	\N	2026-04-10 15:47:55.302786	2026-04-10 15:47:55.302786
c0d70acf-f9c3-47ce-a1ec-384a19485a1c	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-04-08	WORKDAY	\N	2026-04-10 15:47:55.302786	2026-04-10 15:47:55.302786
046f6c15-a16d-4a5d-99a3-1f7ad1fe83a4	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-04-09	WORKDAY	\N	2026-04-10 15:47:55.302786	2026-04-10 15:47:55.302786
555f1dcb-36c4-498a-9909-21eab9d70b7b	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-04-10	WORKDAY	\N	2026-04-10 15:47:55.302786	2026-04-10 15:47:55.302786
e93e4ae5-426c-4521-b465-9ee84edb5456	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-04-12	RESTDAY	\N	2026-04-10 15:47:55.302786	2026-04-10 15:47:55.302786
ddabd73d-8eb9-41a4-8b90-53be0b974df9	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-04-13	WORKDAY	\N	2026-04-10 15:47:55.302786	2026-04-10 15:47:55.302786
6854d505-b2f5-48ca-b752-2920b30d6655	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-04-14	WORKDAY	\N	2026-04-10 15:47:55.302786	2026-04-10 15:47:55.302786
f3e0c181-6382-4da7-a8ce-8e817858c422	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-04-15	WORKDAY	\N	2026-04-10 15:47:55.302786	2026-04-10 15:47:55.302786
60caaba1-e9eb-42fa-a6ae-d89364b293a9	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-04-16	WORKDAY	\N	2026-04-10 15:47:55.302786	2026-04-10 15:47:55.302786
5540a86e-3740-470e-867a-804c2a9e352b	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-04-17	WORKDAY	\N	2026-04-10 15:47:55.302786	2026-04-10 15:47:55.302786
5971940b-d809-4999-bd54-1ec25151eb5f	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-04-19	RESTDAY	\N	2026-04-10 15:47:55.302786	2026-04-10 15:47:55.302786
407190e8-c86f-44c7-82ec-68da049e3015	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-04-20	WORKDAY	\N	2026-04-10 15:47:55.302786	2026-04-10 15:47:55.302786
526409ce-df56-4a3d-8357-ae47fbee01df	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-04-21	WORKDAY	\N	2026-04-10 15:47:55.302786	2026-04-10 15:47:55.302786
d7cd738d-e991-4970-ac20-defe91becbb9	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-04-22	WORKDAY	\N	2026-04-10 15:47:55.302786	2026-04-10 15:47:55.302786
8dd90344-1662-43c5-aaaa-6d7b9fa177e2	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-04-23	WORKDAY	\N	2026-04-10 15:47:55.302786	2026-04-10 15:47:55.302786
39166266-4295-4496-8bbe-e41e46f059d8	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-04-24	WORKDAY	\N	2026-04-10 15:47:55.302786	2026-04-10 15:47:55.302786
93cb5371-6a17-43f9-80d9-203bb5871b32	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-04-26	RESTDAY	\N	2026-04-10 15:47:55.303785	2026-04-10 15:47:55.303785
6bdf5d8c-85d0-4bff-abc1-4ab1bc229805	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-04-27	WORKDAY	\N	2026-04-10 15:47:55.303785	2026-04-10 15:47:55.303785
918c288e-bbc5-4df0-8d79-8eec07fff5b4	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-04-28	WORKDAY	\N	2026-04-10 15:47:55.303785	2026-04-10 15:47:55.303785
74545996-eda5-4c86-a810-8f51ba072bad	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-04-29	WORKDAY	\N	2026-04-10 15:47:55.303785	2026-04-10 15:47:55.303785
f7291b8a-7f2e-4d78-85a5-06075310074f	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-04-30	WORKDAY	\N	2026-04-10 15:47:55.303785	2026-04-10 15:47:55.303785
0ff12da7-b912-4719-88ce-5bea5c12355d	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-05-01	WORKDAY	\N	2026-04-10 15:47:55.303785	2026-04-10 15:47:55.303785
0110b405-d010-4690-84bf-e1ea6edf9ce4	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-05-03	RESTDAY	\N	2026-04-10 15:47:55.304298	2026-04-10 15:47:55.304298
92f908b3-346a-431d-b80e-a54610172248	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-05-04	WORKDAY	\N	2026-04-10 15:47:55.304298	2026-04-10 15:47:55.304298
d90777a1-f914-482b-a581-1a26891153e2	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-05-05	WORKDAY	\N	2026-04-10 15:47:55.304298	2026-04-10 15:47:55.304298
ad94f1cc-e1e0-4438-abac-c2415bcaeee4	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-05-06	WORKDAY	\N	2026-04-10 15:47:55.304298	2026-04-10 15:47:55.304298
a0731155-ac9f-4b49-bb2e-e376d7f4c343	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-05-07	WORKDAY	\N	2026-04-10 15:47:55.304298	2026-04-10 15:47:55.304298
ced44f35-75e0-4dab-820d-9659efa4da52	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-05-08	WORKDAY	\N	2026-04-10 15:47:55.304298	2026-04-10 15:47:55.304298
a04c361e-853b-42c3-bb28-f3227b1778c7	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-05-10	RESTDAY	\N	2026-04-10 15:47:55.305299	2026-04-10 15:47:55.305299
61276731-d59f-41b2-ba2e-dc05c1f5d213	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-05-11	WORKDAY	\N	2026-04-10 15:47:55.305299	2026-04-10 15:47:55.305299
504acfdf-d6fa-4122-95c7-2b5232bf7689	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-05-12	WORKDAY	\N	2026-04-10 15:47:55.305299	2026-04-10 15:47:55.305299
ac99d19f-90bb-4923-b528-3f55cc6853c0	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-05-13	WORKDAY	\N	2026-04-10 15:47:55.305299	2026-04-10 15:47:55.305299
8c606e68-4c42-4b79-a79e-4a5102b1102b	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-05-14	WORKDAY	\N	2026-04-10 15:47:55.305299	2026-04-10 15:47:55.305299
cebb9120-b2b4-4b8b-ad92-c9c16e262fe6	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-05-15	WORKDAY	\N	2026-04-10 15:47:55.305299	2026-04-10 15:47:55.305299
c968f491-2ea0-4e41-a2cd-36decd1352c3	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-05-17	RESTDAY	\N	2026-04-10 15:47:55.305299	2026-04-10 15:47:55.305299
18d23907-3ed6-44cc-935c-717869e453ac	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-05-18	WORKDAY	\N	2026-04-10 15:47:55.305299	2026-04-10 15:47:55.305299
25f5e6c3-2d9d-4944-82f0-7e953630c378	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-05-19	WORKDAY	\N	2026-04-10 15:47:55.3063	2026-04-10 15:47:55.3063
5dc9c8b2-2c32-437c-9c46-481c9efc6b9e	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-05-20	WORKDAY	\N	2026-04-10 15:47:55.3063	2026-04-10 15:47:55.3063
63fdc5ed-f571-4e64-97ee-98b3bade67f7	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-05-21	WORKDAY	\N	2026-04-10 15:47:55.3063	2026-04-10 15:47:55.3063
1a82ce71-7909-4e36-a9c8-21ec820a418e	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-05-22	WORKDAY	\N	2026-04-10 15:47:55.3063	2026-04-10 15:47:55.3063
2b333d70-c258-489f-92b7-5e3c4d22780c	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-05-24	RESTDAY	\N	2026-04-10 15:47:55.3063	2026-04-10 15:47:55.3063
37c79737-2960-44a7-9969-84bd708f9695	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-05-25	WORKDAY	\N	2026-04-10 15:47:55.3063	2026-04-10 15:47:55.3063
665f7824-f92e-405f-8883-05e7414d0d98	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-05-26	WORKDAY	\N	2026-04-10 15:47:55.3063	2026-04-10 15:47:55.3063
1ac02c26-c6b4-499f-a8d3-c134e2fee2cd	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-05-27	WORKDAY	\N	2026-04-10 15:47:55.3063	2026-04-10 15:47:55.3063
62be7db5-8983-49bf-ae13-65e204d2bd7d	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-05-28	WORKDAY	\N	2026-04-10 15:47:55.3063	2026-04-10 15:47:55.3063
c93cbd8d-0235-402c-8fa7-99ad80491e59	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-05-29	WORKDAY	\N	2026-04-10 15:47:55.3063	2026-04-10 15:47:55.3063
3f9616c9-1714-46e8-b5ea-d0202a39ffdf	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-05-31	RESTDAY	\N	2026-04-10 15:47:55.3063	2026-04-10 15:47:55.3063
31706e4a-1228-4332-96e2-34ff8e267519	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-06-01	WORKDAY	\N	2026-04-10 15:47:55.3063	2026-04-10 15:47:55.3063
389c6ac3-fbf2-42ac-abac-c7804b89ab26	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-06-02	WORKDAY	\N	2026-04-10 15:47:55.3063	2026-04-10 15:47:55.3063
84a37167-732c-488f-8381-2d0df385683e	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-06-03	WORKDAY	\N	2026-04-10 15:47:55.3063	2026-04-10 15:47:55.3063
8e53c5bf-f09e-4439-a556-d965176275f6	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-06-04	WORKDAY	\N	2026-04-10 15:47:55.3063	2026-04-10 15:47:55.3063
f6b52ac4-646e-4f57-9437-27600745deec	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-06-05	WORKDAY	\N	2026-04-10 15:47:55.3063	2026-04-10 15:47:55.3063
fac4f362-63fd-47e3-aa78-413d5eaf6bd2	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-06-07	RESTDAY	\N	2026-04-10 15:47:55.3063	2026-04-10 15:47:55.3063
aa2c67f2-3c3a-4f9d-8d4d-968be8cc74cd	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-06-08	WORKDAY	\N	2026-04-10 15:47:55.3063	2026-04-10 15:47:55.3063
d03ffaf8-9bdc-429f-9012-d788e9f2e020	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-06-09	WORKDAY	\N	2026-04-10 15:47:55.3063	2026-04-10 15:47:55.3063
441b8bb1-51d9-45cd-8a79-676f463a1322	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-06-10	WORKDAY	\N	2026-04-10 15:47:55.3063	2026-04-10 15:47:55.3063
f7f7f60e-d02c-458a-84d1-52e519615ff1	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-06-11	WORKDAY	\N	2026-04-10 15:47:55.3063	2026-04-10 15:47:55.3063
9eb088f5-262b-4326-b98f-4cc75ea5299d	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-06-12	WORKDAY	\N	2026-04-10 15:47:55.307299	2026-04-10 15:47:55.307299
e2ecd437-422b-41b5-a8d5-5cdd9191bc7e	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-06-14	RESTDAY	\N	2026-04-10 15:47:55.307299	2026-04-10 15:47:55.307299
7c0ea95b-3be2-4fa5-bfac-73cde3178196	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-06-15	WORKDAY	\N	2026-04-10 15:47:55.307299	2026-04-10 15:47:55.307299
80c2d832-658e-4c23-8595-644fc37b3cab	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-06-16	WORKDAY	\N	2026-04-10 15:47:55.307299	2026-04-10 15:47:55.307299
f953c88c-d164-45ac-9b3e-4fa72231e5e2	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-06-17	WORKDAY	\N	2026-04-10 15:47:55.307299	2026-04-10 15:47:55.307299
6005b27a-24ec-4300-b786-ebd41390e0b5	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-06-18	WORKDAY	\N	2026-04-10 15:47:55.307299	2026-04-10 15:47:55.307299
2bb476c3-de92-4a18-a857-fb3d619dea21	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-06-19	WORKDAY	\N	2026-04-10 15:47:55.307299	2026-04-10 15:47:55.307299
891cd2ff-cbf6-4174-b26e-5ba4e8f31756	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-06-21	RESTDAY	\N	2026-04-10 15:47:55.307299	2026-04-10 15:47:55.307299
5f1f5b5e-6074-4cd8-9294-0ce0cba36399	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-06-22	WORKDAY	\N	2026-04-10 15:47:55.307299	2026-04-10 15:47:55.307299
37bf1669-a29c-4ebb-b75c-f5a150c56f08	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-06-23	WORKDAY	\N	2026-04-10 15:47:55.307299	2026-04-10 15:47:55.307299
865f7fef-3db3-4fef-adea-6b56f3e9f9b2	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-06-24	WORKDAY	\N	2026-04-10 15:47:55.307299	2026-04-10 15:47:55.307299
279179c0-32b8-4264-904b-3e82fa74a06a	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-06-25	WORKDAY	\N	2026-04-10 15:47:55.307299	2026-04-10 15:47:55.307299
f8c36935-d3d2-4ed8-b8c1-aa4cfebaf535	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-06-26	WORKDAY	\N	2026-04-10 15:47:55.307299	2026-04-10 15:47:55.307299
e97c653f-7271-4a7b-9ae4-6e26f06b5b05	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-06-28	RESTDAY	\N	2026-04-10 15:47:55.307804	2026-04-10 15:47:55.307804
24cf304b-230e-4d88-bda4-50ea021122b3	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-06-29	WORKDAY	\N	2026-04-10 15:47:55.307804	2026-04-10 15:47:55.307804
bbb760a0-8782-44cb-9110-2cec5d059eff	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-06-30	WORKDAY	\N	2026-04-10 15:47:55.307804	2026-04-10 15:47:55.307804
05965178-4a8d-41a8-9405-143b6330b706	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-07-01	WORKDAY	\N	2026-04-10 15:47:55.307804	2026-04-10 15:47:55.307804
e1965457-8d1d-4ac6-bcec-31942b6a5ae0	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-07-02	WORKDAY	\N	2026-04-10 15:47:55.307804	2026-04-10 15:47:55.307804
e57503ba-9ccb-4752-a1f3-64d399b35b44	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-07-03	WORKDAY	\N	2026-04-10 15:47:55.307804	2026-04-10 15:47:55.307804
bf860f19-e4e5-4fbe-84b8-a2263f64a659	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-07-05	RESTDAY	\N	2026-04-10 15:47:55.307804	2026-04-10 15:47:55.307804
2d109d96-c14e-4658-9ba1-0995750f5867	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-07-06	WORKDAY	\N	2026-04-10 15:47:55.307804	2026-04-10 15:47:55.307804
1dc01eaf-82e9-455e-8708-af0983017d1e	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-07-07	WORKDAY	\N	2026-04-10 15:47:55.307804	2026-04-10 15:47:55.307804
eb379987-d0fb-4882-beab-9a9dba54df54	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-07-08	WORKDAY	\N	2026-04-10 15:47:55.307804	2026-04-10 15:47:55.307804
ce936d75-127b-472d-af46-4a24ef1df99a	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-07-09	WORKDAY	\N	2026-04-10 15:47:55.307804	2026-04-10 15:47:55.307804
8c14e4fd-1ebd-4142-a7bf-48d328ed29ee	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-07-10	WORKDAY	\N	2026-04-10 15:47:55.307804	2026-04-10 15:47:55.307804
627e596f-7f7f-4bcf-8dca-8ad8c10ab20d	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-07-12	RESTDAY	\N	2026-04-10 15:47:55.307804	2026-04-10 15:47:55.307804
cc52442f-402b-44b9-a2bf-4545964d4557	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-07-13	WORKDAY	\N	2026-04-10 15:47:55.307804	2026-04-10 15:47:55.307804
ea7d3273-4f75-4361-bab2-e948b7715252	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-07-14	WORKDAY	\N	2026-04-10 15:47:55.308409	2026-04-10 15:47:55.308409
5d9f54d8-3356-4360-aeb0-b49ed0ee8dce	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-07-15	WORKDAY	\N	2026-04-10 15:47:55.308409	2026-04-10 15:47:55.308409
5dd7a045-8bf0-4901-967b-8e4fd582d146	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-07-16	WORKDAY	\N	2026-04-10 15:47:55.308409	2026-04-10 15:47:55.308409
2e669634-b9d0-4663-a3dd-cf0ca90047c5	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-07-17	WORKDAY	\N	2026-04-10 15:47:55.308409	2026-04-10 15:47:55.308409
101d1072-948a-4bfe-9f26-86929c17bdcf	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-07-19	RESTDAY	\N	2026-04-10 15:47:55.308409	2026-04-10 15:47:55.308409
dbe1c822-c9d7-4c0e-83a5-56d277d8159a	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-07-20	WORKDAY	\N	2026-04-10 15:47:55.308409	2026-04-10 15:47:55.308409
fad27d5e-b416-4281-9c49-071ea33a3cfb	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-07-21	WORKDAY	\N	2026-04-10 15:47:55.308409	2026-04-10 15:47:55.308409
07e70708-d851-462a-8aef-9780146bdf8c	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-07-22	WORKDAY	\N	2026-04-10 15:47:55.308409	2026-04-10 15:47:55.308409
4efe7d6c-df15-4b36-9eb9-d1a39b01177d	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-07-23	WORKDAY	\N	2026-04-10 15:47:55.308409	2026-04-10 15:47:55.308409
327e079e-1fdf-4e7e-9703-1563a43003f9	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-07-24	WORKDAY	\N	2026-04-10 15:47:55.308409	2026-04-10 15:47:55.308409
bddf7d3f-1c8f-4fdd-a3ee-f786781d4598	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-07-26	RESTDAY	\N	2026-04-10 15:47:55.308409	2026-04-10 15:47:55.308409
26798ea3-0344-406c-b25c-d9ec53034cb1	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-07-27	WORKDAY	\N	2026-04-10 15:47:55.308409	2026-04-10 15:47:55.308409
59ecdbed-db92-4bde-8c78-a68f825dee54	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-07-28	WORKDAY	\N	2026-04-10 15:47:55.308409	2026-04-10 15:47:55.308409
08ca5fbe-9058-4281-9be7-ece7a4fc598d	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-07-29	WORKDAY	\N	2026-04-10 15:47:55.308409	2026-04-10 15:47:55.308409
7a829fd2-6423-4ea6-8dc1-4cff564f29bf	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-07-30	WORKDAY	\N	2026-04-10 15:47:55.308409	2026-04-10 15:47:55.308409
45de7c67-ccae-4870-9e11-50d05510108b	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-07-31	WORKDAY	\N	2026-04-10 15:47:55.308409	2026-04-10 15:47:55.308409
511516a4-9d80-4baf-a9ef-4148c4b8bf65	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-08-02	RESTDAY	\N	2026-04-10 15:47:55.308409	2026-04-10 15:47:55.308409
91a32098-6292-4c1b-935a-7da6b3b05190	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-08-03	WORKDAY	\N	2026-04-10 15:47:55.308409	2026-04-10 15:47:55.308409
bf924173-3a33-452e-a2ab-10b344ac1f65	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-08-04	WORKDAY	\N	2026-04-10 15:47:55.308409	2026-04-10 15:47:55.308409
5c4d1d8b-d1b1-48ae-a44e-97c33897e863	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-08-05	WORKDAY	\N	2026-04-10 15:47:55.308409	2026-04-10 15:47:55.308409
96ce5576-1671-47fd-9fd3-3f3aec95f8de	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-08-06	WORKDAY	\N	2026-04-10 15:47:55.308409	2026-04-10 15:47:55.308409
778223d2-a3e9-4356-89d6-a98264309682	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-08-07	WORKDAY	\N	2026-04-10 15:47:55.308409	2026-04-10 15:47:55.308409
ae157a88-4785-4d25-8263-7e98d1e5b7c6	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-08-09	RESTDAY	\N	2026-04-10 15:47:55.308409	2026-04-10 15:47:55.308409
ff1a95db-6440-49a0-9fdd-8fcd4d4cd501	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-08-10	WORKDAY	\N	2026-04-10 15:47:55.308409	2026-04-10 15:47:55.308409
86ed97bd-dc05-4523-b35c-9f7bc45fa487	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-08-11	WORKDAY	\N	2026-04-10 15:47:55.308409	2026-04-10 15:47:55.308409
cc239eed-7178-4952-a38e-1673f6b7240c	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-08-12	WORKDAY	\N	2026-04-10 15:47:55.308409	2026-04-10 15:47:55.308409
f328d736-dc85-471b-959d-6ebc9a14c5e9	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-08-13	WORKDAY	\N	2026-04-10 15:47:55.309417	2026-04-10 15:47:55.309417
ce1bb430-1af3-4cb2-bf78-f6d299905b12	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-08-14	WORKDAY	\N	2026-04-10 15:47:55.309417	2026-04-10 15:47:55.309417
b36d2a0a-efdc-4fcb-b13a-02958d1f6efc	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-08-16	RESTDAY	\N	2026-04-10 15:47:55.309417	2026-04-10 15:47:55.309417
80aebe7d-a6fc-4bbd-ac18-e954e344d1c9	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-08-17	WORKDAY	\N	2026-04-10 15:47:55.309417	2026-04-10 15:47:55.309417
6c57b88b-c5d2-4c93-9ac3-dba8f7d776d6	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-08-18	WORKDAY	\N	2026-04-10 15:47:55.309417	2026-04-10 15:47:55.309417
8905f4de-0404-4bb5-b68a-5694deb31612	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-08-19	WORKDAY	\N	2026-04-10 15:47:55.309417	2026-04-10 15:47:55.309417
4ecdb7cf-322e-4940-8734-f2862493783f	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-08-20	WORKDAY	\N	2026-04-10 15:47:55.309417	2026-04-10 15:47:55.309417
531d0cb5-bffd-4b77-97b0-7ff1174dc23c	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-08-21	WORKDAY	\N	2026-04-10 15:47:55.309417	2026-04-10 15:47:55.309417
ee962575-dfa5-4488-91c2-6d29f8f5b08a	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-08-23	RESTDAY	\N	2026-04-10 15:47:55.309417	2026-04-10 15:47:55.309417
4632f979-87e9-4605-b21d-946959245264	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-08-24	WORKDAY	\N	2026-04-10 15:47:55.309417	2026-04-10 15:47:55.309417
61262304-2a2d-4fd9-a6f3-6fe2691bd546	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-08-25	WORKDAY	\N	2026-04-10 15:47:55.309417	2026-04-10 15:47:55.309417
f7d3b6de-919c-4845-8f23-b00bdc3d718a	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-08-26	WORKDAY	\N	2026-04-10 15:47:55.309417	2026-04-10 15:47:55.309417
c09a994a-4002-4327-82d8-7443b5304de4	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-08-27	WORKDAY	\N	2026-04-10 15:47:55.309417	2026-04-10 15:47:55.309417
6cf3f71b-6579-4ab9-889c-f7f454c9658f	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-08-28	WORKDAY	\N	2026-04-10 15:47:55.309921	2026-04-10 15:47:55.309921
3c3f24b0-f22a-488d-b67b-182e5878713e	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-08-30	RESTDAY	\N	2026-04-10 15:47:55.309921	2026-04-10 15:47:55.309921
421b2a7d-05d6-49ce-b157-3f8780a75333	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-08-31	WORKDAY	\N	2026-04-10 15:47:55.309921	2026-04-10 15:47:55.309921
b324c259-7bf2-47e0-9f75-c2a229f6dbab	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-09-01	WORKDAY	\N	2026-04-10 15:47:55.309921	2026-04-10 15:47:55.309921
18f4dc6d-7a92-4b81-8ce4-2156ecd4163c	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-09-02	WORKDAY	\N	2026-04-10 15:47:55.310229	2026-04-10 15:47:55.310229
59a50dd3-7696-4736-ba50-f789fe8d7ea7	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-09-03	WORKDAY	\N	2026-04-10 15:47:55.310229	2026-04-10 15:47:55.310229
1b5476d9-47e9-436f-b620-741933164c2d	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-09-04	WORKDAY	\N	2026-04-10 15:47:55.310229	2026-04-10 15:47:55.310229
54da1adb-a669-4aae-9103-eed9806418f1	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-09-06	RESTDAY	\N	2026-04-10 15:47:55.310229	2026-04-10 15:47:55.310229
62aee696-111e-48e4-ac8d-c591caa4b87a	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-09-07	WORKDAY	\N	2026-04-10 15:47:55.310229	2026-04-10 15:47:55.310229
2fe1aefd-71c8-4e46-b682-c3d4f923a5f4	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-09-08	WORKDAY	\N	2026-04-10 15:47:55.310229	2026-04-10 15:47:55.310229
cdd1d8d1-4ee0-4787-a1a9-b0035c2e9150	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-09-09	WORKDAY	\N	2026-04-10 15:47:55.310229	2026-04-10 15:47:55.310229
d6ce4d26-7997-4aae-98c5-ddbf2195b9dd	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-09-10	WORKDAY	\N	2026-04-10 15:47:55.310229	2026-04-10 15:47:55.310229
47f04501-7847-4fa3-a756-8faa1af5fc21	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-09-11	WORKDAY	\N	2026-04-10 15:47:55.310229	2026-04-10 15:47:55.310229
a20b6d25-8337-43a9-986d-8ffb84ccb833	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-09-13	RESTDAY	\N	2026-04-10 15:47:55.31074	2026-04-10 15:47:55.31074
a500ae72-36f6-4944-a5f2-fea7054a3bf5	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-09-14	WORKDAY	\N	2026-04-10 15:47:55.31074	2026-04-10 15:47:55.31074
e5daa79b-c2bf-467d-953c-a533844e79d7	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-09-15	WORKDAY	\N	2026-04-10 15:47:55.31074	2026-04-10 15:47:55.31074
7a180a41-f760-49e1-81e5-fad06ae17e0d	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-09-16	WORKDAY	\N	2026-04-10 15:47:55.31074	2026-04-10 15:47:55.31074
861bf441-15ed-409e-a831-90eddd626683	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-09-17	WORKDAY	\N	2026-04-10 15:47:55.31074	2026-04-10 15:47:55.31074
ebcc9a63-f548-43c1-a2e8-9a623c9217dc	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-09-18	WORKDAY	\N	2026-04-10 15:47:55.31074	2026-04-10 15:47:55.31074
373cd757-fb45-4630-b731-210de85fe0ab	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-09-20	RESTDAY	\N	2026-04-10 15:47:55.31074	2026-04-10 15:47:55.31074
f9fe88f5-e0c2-46e5-aa88-6956367efc65	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-09-21	WORKDAY	\N	2026-04-10 15:47:55.31074	2026-04-10 15:47:55.31074
e6ceaf10-a9c3-4da9-ad5f-8f6dfe828eff	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-09-22	WORKDAY	\N	2026-04-10 15:47:55.31074	2026-04-10 15:47:55.31074
25c83771-d87a-499f-8f66-a27ee0f34292	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-09-23	WORKDAY	\N	2026-04-10 15:47:55.31074	2026-04-10 15:47:55.31074
2edf68fb-8420-45ef-bcb0-5aeb133b8c4c	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-09-24	WORKDAY	\N	2026-04-10 15:47:55.31074	2026-04-10 15:47:55.31074
3ad3a343-c61b-45a6-8ed0-2c11ea49aab5	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-09-25	WORKDAY	\N	2026-04-10 15:47:55.31074	2026-04-10 15:47:55.31074
f9a36d54-ec40-481e-9883-0b4590a27391	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-09-27	RESTDAY	\N	2026-04-10 15:47:55.311232	2026-04-10 15:47:55.311232
a9914c17-d0b2-4968-864f-0db1826d4d76	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-09-28	WORKDAY	\N	2026-04-10 15:47:55.311232	2026-04-10 15:47:55.311232
d1c8002e-e738-4c94-a3f3-b709768bfe39	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-09-29	WORKDAY	\N	2026-04-10 15:47:55.311232	2026-04-10 15:47:55.311232
0b90b05b-2b41-4eae-8742-1097319975ca	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-09-30	WORKDAY	\N	2026-04-10 15:47:55.311232	2026-04-10 15:47:55.311232
db5807d4-22cd-420d-bc94-a5661dc73351	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-10-01	WORKDAY	\N	2026-04-10 15:47:55.311232	2026-04-10 15:47:55.311232
e9708fca-2013-443d-a3fa-41a5afa94cb6	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-10-02	WORKDAY	\N	2026-04-10 15:47:55.311232	2026-04-10 15:47:55.311232
e3f2f81e-5ed0-4e0d-aa87-de8fc0c8cb0a	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-10-04	RESTDAY	\N	2026-04-10 15:47:55.311232	2026-04-10 15:47:55.311232
e8f06e12-557a-4d9e-82e6-14ca28507686	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-10-05	WORKDAY	\N	2026-04-10 15:47:55.311232	2026-04-10 15:47:55.311232
652490de-43e4-474d-9212-e4d0a68ed7e2	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-10-06	WORKDAY	\N	2026-04-10 15:47:55.311232	2026-04-10 15:47:55.311232
3450ed07-ae02-4099-830d-aeb0b4e922d2	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-10-07	WORKDAY	\N	2026-04-10 15:47:55.311232	2026-04-10 15:47:55.311232
5cd611b6-2d1e-4293-99a7-79af28ef89b8	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-10-08	WORKDAY	\N	2026-04-10 15:47:55.311232	2026-04-10 15:47:55.311232
e846a771-4fb0-4a79-b127-cd282658c27e	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-10-09	WORKDAY	\N	2026-04-10 15:47:55.311232	2026-04-10 15:47:55.311232
c170dbcc-7bf5-4c70-8eff-976dff28f7ab	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-10-11	RESTDAY	\N	2026-04-10 15:47:55.311232	2026-04-10 15:47:55.311232
8c881756-4274-4879-94dc-0517dd828baf	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-10-12	WORKDAY	\N	2026-04-10 15:47:55.311736	2026-04-10 15:47:55.311736
88a84cc3-a143-4554-8cf1-f400e80691ea	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-10-13	WORKDAY	\N	2026-04-10 15:47:55.311736	2026-04-10 15:47:55.311736
84f8e362-2d56-49a5-aa94-adbb550b3fc1	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-10-14	WORKDAY	\N	2026-04-10 15:47:55.311736	2026-04-10 15:47:55.311736
a38e0027-2ca1-4284-a523-94a3b9635a50	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-10-15	WORKDAY	\N	2026-04-10 15:47:55.311736	2026-04-10 15:47:55.311736
58bc5644-ebf1-458b-a4bd-a37b1557306c	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-10-16	WORKDAY	\N	2026-04-10 15:47:55.311736	2026-04-10 15:47:55.311736
fa16e010-ff73-4a0b-97ac-4fcb89d52a41	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-10-18	RESTDAY	\N	2026-04-10 15:47:55.311736	2026-04-10 15:47:55.311736
a10a1e2f-65b9-4816-bfa5-53dff00fc594	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-10-19	WORKDAY	\N	2026-04-10 15:47:55.311736	2026-04-10 15:47:55.311736
5a750376-39b7-47cf-98e3-d043b6d39e4b	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-10-20	WORKDAY	\N	2026-04-10 15:47:55.311736	2026-04-10 15:47:55.311736
68dbf609-ce02-47fd-8de6-fdab4ec18489	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-10-21	WORKDAY	\N	2026-04-10 15:47:55.311736	2026-04-10 15:47:55.311736
2b7fc6b1-40cc-4abc-b567-471185da9653	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-10-22	WORKDAY	\N	2026-04-10 15:47:55.311736	2026-04-10 15:47:55.311736
98e59c67-2063-4bec-9ff5-79cd46a0d722	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-10-23	WORKDAY	\N	2026-04-10 15:47:55.311736	2026-04-10 15:47:55.311736
36a0660f-05f7-4693-8eff-371674f1aa64	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-10-25	RESTDAY	\N	2026-04-10 15:47:55.311736	2026-04-10 15:47:55.311736
89263e50-3041-467d-bd92-95f5d4b70e7d	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-10-26	WORKDAY	\N	2026-04-10 15:47:55.311736	2026-04-10 15:47:55.311736
975e87a6-783b-4f83-ac97-cfa4265f4e70	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-10-27	WORKDAY	\N	2026-04-10 15:47:55.311736	2026-04-10 15:47:55.311736
713504fb-c4c0-419e-9db3-f3c9c26660d1	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-10-28	WORKDAY	\N	2026-04-10 15:47:55.311736	2026-04-10 15:47:55.311736
a0341c9c-002e-48ce-9d0a-9a46def736ad	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-10-29	WORKDAY	\N	2026-04-10 15:47:55.311736	2026-04-10 15:47:55.311736
74f1de15-f44e-488f-b94c-c3e06b93b482	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-10-30	WORKDAY	\N	2026-04-10 15:47:55.311736	2026-04-10 15:47:55.311736
d3d0f105-402c-419a-8d51-d28dc1a8f580	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-11-01	RESTDAY	\N	2026-04-10 15:47:55.311736	2026-04-10 15:47:55.311736
8aac93ff-719e-42bc-97f2-1e0ccf6e8d06	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-11-02	WORKDAY	\N	2026-04-10 15:47:55.311736	2026-04-10 15:47:55.311736
68857b7f-2e7f-448d-8232-d96513501d28	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-11-03	WORKDAY	\N	2026-04-10 15:47:55.311736	2026-04-10 15:47:55.311736
ff367541-37d1-4388-9b32-06fdafafa3d9	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-11-04	WORKDAY	\N	2026-04-10 15:47:55.311736	2026-04-10 15:47:55.311736
56ad5b32-4f7f-4dfc-b594-98b4759c45dd	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-11-05	WORKDAY	\N	2026-04-10 15:47:55.311736	2026-04-10 15:47:55.311736
8b3bc00d-f826-4148-8372-c661f358a940	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-11-06	WORKDAY	\N	2026-04-10 15:47:55.311736	2026-04-10 15:47:55.311736
2386f1a0-3800-467b-903f-f8f59d762ccf	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-11-08	RESTDAY	\N	2026-04-10 15:47:55.311736	2026-04-10 15:47:55.311736
8c547860-bb6e-488a-a7ac-8bb0e209bd52	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-11-09	WORKDAY	\N	2026-04-10 15:47:55.312748	2026-04-10 15:47:55.312748
17fcde9d-dd61-4d5f-a335-8ce855f236fc	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-11-10	WORKDAY	\N	2026-04-10 15:47:55.312748	2026-04-10 15:47:55.312748
09e41fb9-9948-4a65-9944-a7ddae5b1cb4	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-11-11	WORKDAY	\N	2026-04-10 15:47:55.312748	2026-04-10 15:47:55.312748
923fbfc2-b48f-45e9-a4bd-0f260e7dae77	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-11-12	WORKDAY	\N	2026-04-10 15:47:55.312748	2026-04-10 15:47:55.312748
60baf976-08c6-4310-a2a3-d60192800c07	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-11-13	WORKDAY	\N	2026-04-10 15:47:55.312748	2026-04-10 15:47:55.312748
5ed5da9f-edd8-4ba7-9dc3-58dd5415c4eb	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-11-15	RESTDAY	\N	2026-04-10 15:47:55.312748	2026-04-10 15:47:55.312748
ca45c630-faa0-470d-ae2f-c98dbf89d7a6	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-11-16	WORKDAY	\N	2026-04-10 15:47:55.312748	2026-04-10 15:47:55.312748
46ab542f-51e3-4e4c-970f-bf9142c729ab	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-11-17	WORKDAY	\N	2026-04-10 15:47:55.312748	2026-04-10 15:47:55.312748
f604f8a8-b2f0-441a-aee6-14dab5071de7	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-11-18	WORKDAY	\N	2026-04-10 15:47:55.312748	2026-04-10 15:47:55.312748
7c672dca-baf1-4c49-ab4b-8649337347ef	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-11-19	WORKDAY	\N	2026-04-10 15:47:55.313381	2026-04-10 15:47:55.313381
dafc47f6-ec27-46f6-87ef-c1177ee3c5f4	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-11-20	WORKDAY	\N	2026-04-10 15:47:55.313381	2026-04-10 15:47:55.313381
cd28196a-8528-4028-a51d-6e758befe7bd	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-11-22	RESTDAY	\N	2026-04-10 15:47:55.313381	2026-04-10 15:47:55.313381
49d3aef5-b3b2-48d0-9629-9ca7f2035545	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-11-23	WORKDAY	\N	2026-04-10 15:47:55.313381	2026-04-10 15:47:55.313381
6818b71f-a227-4428-a1d7-ef5adbde6735	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-11-24	WORKDAY	\N	2026-04-10 15:47:55.313381	2026-04-10 15:47:55.313381
de0a60d2-820f-4f0f-bacd-bf59cdc304e0	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-11-25	WORKDAY	\N	2026-04-10 15:47:55.313381	2026-04-10 15:47:55.313381
002913b3-1dca-495b-8cc8-117eeaa1bf95	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-11-26	WORKDAY	\N	2026-04-10 15:47:55.313381	2026-04-10 15:47:55.313381
8d26b71e-3367-4b1d-b719-1135a925babf	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-11-27	WORKDAY	\N	2026-04-10 15:47:55.313381	2026-04-10 15:47:55.313381
424bd4f4-7476-485b-8bf3-b97dc339def0	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-11-29	RESTDAY	\N	2026-04-10 15:47:55.313381	2026-04-10 15:47:55.313381
7ccecf83-6207-44da-8f28-a191019dd386	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-11-30	WORKDAY	\N	2026-04-10 15:47:55.313381	2026-04-10 15:47:55.313381
00553cf4-a045-42c7-a293-21cb79862d2b	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-12-01	WORKDAY	\N	2026-04-10 15:47:55.313381	2026-04-10 15:47:55.313381
d36667a5-70e3-448d-b220-d2a73a8e4c60	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-12-02	WORKDAY	\N	2026-04-10 15:47:55.313381	2026-04-10 15:47:55.313381
5b7e7918-6f2b-41c7-bcde-de0d52f722cc	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-12-03	WORKDAY	\N	2026-04-10 15:47:55.313381	2026-04-10 15:47:55.313381
38db9c75-f6d9-4067-a638-f7ac3e91baee	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-12-04	WORKDAY	\N	2026-04-10 15:47:55.313381	2026-04-10 15:47:55.313381
eb3a8fc8-aef2-44ff-9704-de9b12d29a2e	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-12-06	RESTDAY	\N	2026-04-10 15:47:55.313381	2026-04-10 15:47:55.313381
42f80e85-653d-45f8-ad98-da82dbb2eb88	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-12-07	WORKDAY	\N	2026-04-10 15:47:55.313884	2026-04-10 15:47:55.313884
839636fe-5c43-4ef4-a370-75fbb6561515	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-12-08	WORKDAY	\N	2026-04-10 15:47:55.313884	2026-04-10 15:47:55.313884
4e5d384b-d064-40ec-93a7-6e5c8d844e07	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-12-09	WORKDAY	\N	2026-04-10 15:47:55.313884	2026-04-10 15:47:55.313884
1b15b5df-ea0d-4988-9971-23c9cf9aecd8	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-12-10	WORKDAY	\N	2026-04-10 15:47:55.313884	2026-04-10 15:47:55.313884
da2a3f1c-3353-4d2d-b6a0-d30f555825ca	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-12-11	WORKDAY	\N	2026-04-10 15:47:55.313884	2026-04-10 15:47:55.313884
508ba5e9-a95d-4cc7-935f-81372a9e67e8	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-12-13	RESTDAY	\N	2026-04-10 15:47:55.314514	2026-04-10 15:47:55.314514
dbc9748b-828f-4613-af3d-fc2730e68a7f	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-12-14	WORKDAY	\N	2026-04-10 15:47:55.314514	2026-04-10 15:47:55.314514
183731a7-4210-4d43-abf6-a9eda36354d7	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-12-15	WORKDAY	\N	2026-04-10 15:47:55.314514	2026-04-10 15:47:55.314514
8fb547fe-ee73-451f-bfdb-6979dc701779	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-12-16	WORKDAY	\N	2026-04-10 15:47:55.314514	2026-04-10 15:47:55.314514
9a245029-1828-426b-854c-0dd2f6e5a9e7	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-12-17	WORKDAY	\N	2026-04-10 15:47:55.314514	2026-04-10 15:47:55.314514
b6c83510-1335-4c0f-8883-674314901689	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-12-18	WORKDAY	\N	2026-04-10 15:47:55.314514	2026-04-10 15:47:55.314514
0c1cc609-f35e-41e8-a866-26972a14da65	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-12-20	RESTDAY	\N	2026-04-10 15:47:55.314514	2026-04-10 15:47:55.314514
d5230867-7b67-4327-83e2-cceae0bfcbd7	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-12-21	WORKDAY	\N	2026-04-10 15:47:55.314514	2026-04-10 15:47:55.314514
1f288cac-d6f2-4d20-bdc1-b0ab7ab16b01	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-12-22	WORKDAY	\N	2026-04-10 15:47:55.314514	2026-04-10 15:47:55.314514
530dc1f0-62ee-48de-b036-d3a61c17864f	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-12-23	WORKDAY	\N	2026-04-10 15:47:55.314514	2026-04-10 15:47:55.314514
cee57b57-4bab-4b25-a31c-bc0a5eea1fef	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-12-24	WORKDAY	\N	2026-04-10 15:47:55.314514	2026-04-10 15:47:55.314514
f2270fde-7f04-4722-a3c2-2a671a718686	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-12-25	WORKDAY	\N	2026-04-10 15:47:55.314514	2026-04-10 15:47:55.314514
0f89b2f7-094c-4fb1-9a5d-20b14b765b04	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-12-27	RESTDAY	\N	2026-04-10 15:47:55.314514	2026-04-10 15:47:55.314514
a09c84ab-db33-4679-b798-8060b15f775c	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-12-28	WORKDAY	\N	2026-04-10 15:47:55.314514	2026-04-10 15:47:55.314514
b3c22e06-38cb-43e2-994d-e8732009cbfc	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-12-29	WORKDAY	\N	2026-04-10 15:47:55.314514	2026-04-10 15:47:55.314514
6ad63134-8a07-42ff-b9aa-79b7391c331d	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-12-30	WORKDAY	\N	2026-04-10 15:47:55.314514	2026-04-10 15:47:55.314514
a60ac327-7b8a-408a-894c-966b60a9c6c6	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-12-31	WORKDAY	\N	2026-04-10 15:47:55.314514	2026-04-10 15:47:55.314514
133367b1-f8a0-4a49-8bfa-58f98daf86a7	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-02-28	WORKDAY	\N	2026-04-10 15:47:55.301194	2026-04-10 15:47:55.301194
4231002a-c414-4df5-8486-091656e6f2b0	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-03-07	WORKDAY	\N	2026-04-10 15:47:55.30178	2026-04-10 15:47:55.30178
5a861682-0627-40f4-81b3-dc10341d04a8	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-03-14	WORKDAY	\N	2026-04-10 15:47:55.30178	2026-04-10 15:47:55.30178
2aa062a7-306f-4955-8bcd-46e0c7b17ba7	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-03-21	WORKDAY	\N	2026-04-10 15:47:55.30178	2026-04-10 15:47:55.30178
8949e12d-6c6f-415b-bef1-ca45a9890378	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-03-28	WORKDAY	\N	2026-04-10 15:47:55.30178	2026-04-10 15:47:55.30178
5109eec6-3833-4666-b439-a7be2748d02a	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-04-04	WORKDAY	\N	2026-04-10 15:47:55.302786	2026-04-10 15:47:55.302786
eae21583-16cf-423e-93b2-a761460faeb8	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-04-11	WORKDAY	\N	2026-04-10 15:47:55.302786	2026-04-10 15:47:55.302786
110a14cd-d710-4082-ad0b-e87fd4f3845e	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-04-18	WORKDAY	\N	2026-04-10 15:47:55.302786	2026-04-10 15:47:55.302786
aa1d2668-c79d-45d5-8b99-a8f97d6da2e9	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-04-25	WORKDAY	\N	2026-04-10 15:47:55.303785	2026-04-10 15:47:55.303785
3ce43110-5ef3-4046-a4ea-f25b29ff9e83	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-05-02	WORKDAY	\N	2026-04-10 15:47:55.303785	2026-04-10 15:47:55.303785
0480b7ec-45bb-4071-8d16-33936772ebb8	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-05-09	WORKDAY	\N	2026-04-10 15:47:55.305299	2026-04-10 15:47:55.305299
12796fb8-5b71-48e7-ba27-390c41ffc4ab	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-05-16	WORKDAY	\N	2026-04-10 15:47:55.305299	2026-04-10 15:47:55.305299
2c4379c2-8db8-4d91-acce-b28568490d24	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-05-23	WORKDAY	\N	2026-04-10 15:47:55.3063	2026-04-10 15:47:55.3063
74b514a1-db3b-4f98-9908-f97d6efebacd	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-05-30	WORKDAY	\N	2026-04-10 15:47:55.3063	2026-04-10 15:47:55.3063
53904296-89d8-433e-b622-fdec7c56eb0c	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-06-06	WORKDAY	\N	2026-04-10 15:47:55.3063	2026-04-10 15:47:55.3063
d9cb3983-b5f1-43e6-8b84-67a008844b1f	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-06-13	WORKDAY	\N	2026-04-10 15:47:55.307299	2026-04-10 15:47:55.307299
8a210bac-3056-4283-a3ca-ed28a71cb8df	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-06-20	WORKDAY	\N	2026-04-10 15:47:55.307299	2026-04-10 15:47:55.307299
9dc551c4-4a65-4380-b1db-5be8f25f2ed2	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-06-27	WORKDAY	\N	2026-04-10 15:47:55.307804	2026-04-10 15:47:55.307804
653a7900-17c8-451b-9fb2-75ee463ad55f	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-07-04	WORKDAY	\N	2026-04-10 15:47:55.307804	2026-04-10 15:47:55.307804
aa32f50b-5068-487c-8e9b-32a5fcc65e02	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-07-11	WORKDAY	\N	2026-04-10 15:47:55.307804	2026-04-10 15:47:55.307804
11013182-8004-4e18-89b5-05d0266cc1b1	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-07-18	WORKDAY	\N	2026-04-10 15:47:55.308409	2026-04-10 15:47:55.308409
64dbc3c4-01e7-47ca-ada4-9454fc089def	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-07-25	WORKDAY	\N	2026-04-10 15:47:55.308409	2026-04-10 15:47:55.308409
ef89d108-449a-4c8d-b754-dc1a5e5b0aca	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-08-01	WORKDAY	\N	2026-04-10 15:47:55.308409	2026-04-10 15:47:55.308409
0b65131a-9329-411b-9d4e-bea3838d6a19	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-08-08	WORKDAY	\N	2026-04-10 15:47:55.308409	2026-04-10 15:47:55.308409
0fe32a45-f1bc-4ef2-9cff-ca1504f756a0	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-08-15	WORKDAY	\N	2026-04-10 15:47:55.309417	2026-04-10 15:47:55.309417
0789db7e-1d22-4538-8a9c-0d2d40b58fe3	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-08-22	WORKDAY	\N	2026-04-10 15:47:55.309417	2026-04-10 15:47:55.309417
b371d7ce-6a84-4780-bee0-23d904225a4c	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-08-29	WORKDAY	\N	2026-04-10 15:47:55.309921	2026-04-10 15:47:55.309921
c19411bd-914a-4741-a7ea-dcf5adcb68e4	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-09-05	WORKDAY	\N	2026-04-10 15:47:55.310229	2026-04-10 15:47:55.310229
72500221-ef42-42c2-974a-ea287631fe2b	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-09-12	WORKDAY	\N	2026-04-10 15:47:55.31074	2026-04-10 15:47:55.31074
4c672222-46c7-49ec-b278-970a92dcf84a	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-09-19	WORKDAY	\N	2026-04-10 15:47:55.31074	2026-04-10 15:47:55.31074
96f456ac-eef9-4d33-b36b-df351c409534	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-09-26	WORKDAY	\N	2026-04-10 15:47:55.311232	2026-04-10 15:47:55.311232
bd1857f1-79b9-4d58-ad67-5178df229dc6	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-10-03	WORKDAY	\N	2026-04-10 15:47:55.311232	2026-04-10 15:47:55.311232
eec3d29f-934f-407b-95ee-7c7ce57fddb7	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-10-10	WORKDAY	\N	2026-04-10 15:47:55.311232	2026-04-10 15:47:55.311232
6039d48d-b7f3-4cf5-9e3b-1d398696aedb	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-10-17	WORKDAY	\N	2026-04-10 15:47:55.311736	2026-04-10 15:47:55.311736
38f6855e-660a-4d18-8307-8184c9af3699	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-10-24	WORKDAY	\N	2026-04-10 15:47:55.311736	2026-04-10 15:47:55.311736
be3ba0cc-7f54-4805-bf40-1994f778afa2	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-10-31	WORKDAY	\N	2026-04-10 15:47:55.311736	2026-04-10 15:47:55.311736
bece340f-d6ca-4168-a114-2f78b0886e56	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-11-07	WORKDAY	\N	2026-04-10 15:47:55.311736	2026-04-10 15:47:55.311736
ebd4426c-f53f-43f7-b74e-88fad162ca0f	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-11-14	WORKDAY	\N	2026-04-10 15:47:55.312748	2026-04-10 15:47:55.312748
898c2aab-b828-43bd-bc47-22608164fff5	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-11-21	WORKDAY	\N	2026-04-10 15:47:55.313381	2026-04-10 15:47:55.313381
32d3b14b-297b-49a3-b301-0230e0cb15eb	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-11-28	WORKDAY	\N	2026-04-10 15:47:55.313381	2026-04-10 15:47:55.313381
33cdad72-b180-4986-b420-c59b4c92a02a	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-12-05	WORKDAY	\N	2026-04-10 15:47:55.313381	2026-04-10 15:47:55.313381
3ab23905-3c32-4873-bbae-33192da67cf6	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-12-12	WORKDAY	\N	2026-04-10 15:47:55.313884	2026-04-10 15:47:55.313884
ca13f2bc-c43f-432e-942c-f6aea1c60bcf	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-12-19	WORKDAY	\N	2026-04-10 15:47:55.314514	2026-04-10 15:47:55.314514
d750660d-b49f-400d-90d6-2b17785c6d61	ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	2026-12-26	WORKDAY	\N	2026-04-10 15:47:55.314514	2026-04-10 15:47:55.314514
\.


--
-- TOC entry 3716 (class 0 OID 17390)
-- Dependencies: 230
-- Data for Name: calendar_shifts; Type: TABLE DATA; Schema: public; Owner: aps_user
--

COPY public.calendar_shifts (id, calendar_id, name, start_time, end_time, sort_order, create_time, update_time, next_day, break_minutes) FROM stdin;
9d46b2c4-8e4a-446c-923e-50de4cfa76fa	8648aa7c-5574-479f-9084-a9ca960b6855	晚班	20:00:00	08:00:00	2	2026-04-09 09:02:38.167369	2026-04-09 09:05:28.261411	t	0
b01dbfa2-f835-41f8-b08a-9c91fb182554	8648aa7c-5574-479f-9084-a9ca960b6855	白班	08:00:00	20:00:00	1	2026-04-09 09:02:10.309462	2026-04-09 20:10:33.980749	f	0
\.


--
-- TOC entry 3715 (class 0 OID 17378)
-- Dependencies: 229
-- Data for Name: factory_calendars; Type: TABLE DATA; Schema: public; Owner: aps_user
--

COPY public.factory_calendars (id, name, code, description, year, is_default, enabled, create_time, update_time) FROM stdin;
a73e562e-fc18-4ac3-b966-d67799e3ae80	通用	SA		2026	f	t	2026-04-09 09:05:55.493504	2026-04-09 15:30:21.293617
8648aa7c-5574-479f-9084-a9ca960b6855	夏季	SJ		2026	t	t	2026-04-09 07:31:11.315773	2026-04-09 15:30:21.293617
ef3ed8f9-4ffd-49cf-b45b-78e3893e9086	注塑机日历	ZS		2026	f	t	2026-04-10 15:47:55.287268	2026-04-10 15:47:55.287268
\.


--
-- TOC entry 3702 (class 0 OID 17181)
-- Dependencies: 216
-- Data for Name: flyway_schema_history; Type: TABLE DATA; Schema: public; Owner: aps_user
--

COPY public.flyway_schema_history (installed_rank, version, description, type, script, checksum, installed_by, installed_on, execution_time, success) FROM stdin;
1	1	Initial Schema	SQL	V1__Initial_Schema.sql	432549994	aps_user	2026-04-07 16:56:16.302015	309	t
2	3	Update Permissions Table	SQL	V3__Update_Permissions_Table.sql	1961768123	aps_user	2026-04-07 16:56:16.651856	48	t
3	4	Grant Admin All Permissions	SQL	V4__Grant_Admin_All_Permissions.sql	-319749245	aps_user	2026-04-07 17:01:16.237427	14	t
4	5	Seed System Management Permissions	SQL	V5__Seed_System_Management_Permissions.sql	2000485508	aps_user	2026-04-07 17:11:37.186148	27	t
5	6	Grant Admin System Management Permissions	SQL	V6__Grant_Admin_System_Management_Permissions.sql	-2056576495	aps_user	2026-04-07 17:11:37.310877	6	t
6	7	Fix Permission Parent Relationships	SQL	V7__Fix_Permission_Parent_Relationships.sql	1600140506	aps_user	2026-04-07 17:28:25.944865	31	t
7	8	Factory Calendar	SQL	V8__Factory_Calendar.sql	-119112920	aps_user	2026-04-09 07:22:37.40243	151	t
8	9	Workshop And Machine	SQL	V9__Add_Next_Day_To_Calendar_Shifts.sql	548310638	aps_user	2026-04-09 07:51:12.360161	13	t
9	10	Repair Workshop And Machine	SQL	V10__Repair_Workshop_And_Machine.sql	1079187963	aps_user	2026-04-09 14:55:30.912948	86	t
10	11	Dictionary Management	SQL	V11__Dictionary_Management.sql	-1377853514	aps_user	2026-04-09 18:16:03.696936	254	t
11	12	Seed Dictionary Permissions	SQL	V12__Seed_Dictionary_Permissions.sql	-783358031	aps_user	2026-04-09 18:16:04.096326	38	t
12	13	Grant Admin Dictionary Permissions	SQL	V13__Grant_Admin_Dictionary_Permissions.sql	1786010982	aps_user	2026-04-09 18:16:04.180011	9	t
13	14	Seed Enum Dict Types	SQL	V14__Seed_Enum_Dict_Types.sql	1926999247	aps_user	2026-04-09 19:07:56.771263	49	t
14	15	Seed Permission Type Dict	SQL	V15__Seed_Permission_Type_Dict.sql	-1940636338	aps_user	2026-04-09 19:07:56.863991	6	t
15	16	Resource Daily Capacity	SQL	V16__Resource_Daily_Capacity.sql	1554718515	aps_user	2026-04-10 11:56:20.603882	84	t
16	17	Seed BaseData Permissions	SQL	V17__Seed_BaseData_Permissions.sql	-1697554484	aps_user	2026-04-10 15:06:37.094777	401	t
17	18	Seed Missing System Permissions	SQL	V18__Seed_Missing_System_Permissions.sql	811151285	aps_user	2026-04-10 15:12:44.109847	50	t
18	19	Add Break Minutes To Calendar Shifts	SQL	V19__Add_Break_Minutes_To_Calendar_Shifts.sql	532071782	aps_user	2026-04-10 16:18:50.2479	14	t
19	20	Seed Factory Calendar BaseData Permissions	SQL	V20__Seed_Factory_Calendar_BaseData_Permissions.sql	-2014229294	aps_user	2026-04-10 16:48:46.340088	66	t
20	21	Schedule Time Parameter	SQL	V21__Schedule_Time_Parameter.sql	-537325260	aps_user	2026-04-10 21:14:50.648869	51	t
21	22	Seed Schedule Time Parameter Permissions	SQL	V22__Seed_Schedule_Time_Parameter_Permissions.sql	425867285	aps_user	2026-04-10 21:14:50.748023	22	t
\.


--
-- TOC entry 3711 (class 0 OID 17290)
-- Dependencies: 225
-- Data for Name: operations; Type: TABLE DATA; Schema: public; Owner: aps_user
--

COPY public.operations (id, order_id, operation_code, operation_name, sequence, standard_duration, required_resource_id, create_time, update_time) FROM stdin;
\.


--
-- TOC entry 3710 (class 0 OID 17280)
-- Dependencies: 224
-- Data for Name: orders; Type: TABLE DATA; Schema: public; Owner: aps_user
--

COPY public.orders (id, order_no, product_code, product_name, quantity, priority, status, due_date, create_time, update_time) FROM stdin;
993a93cd-6ac1-4591-8fe5-5168ecf2a363	TEST-ORDER-001	PROD-001	Test Product	100	HIGH	PENDING	2026-04-15 00:00:00	2026-04-08 08:19:06.576038	2026-04-08 08:19:06.576038
\.


--
-- TOC entry 3705 (class 0 OID 17211)
-- Dependencies: 219
-- Data for Name: permissions; Type: TABLE DATA; Schema: public; Owner: aps_user
--

COPY public.permissions (id, code, description, create_time, update_time, name, type, route_path, icon, sort, enabled, visible, parent_id) FROM stdin;
4199134d-91f4-4b66-883c-c9791ce2deac	USER_READ	查看用户	2026-04-07 16:56:16.326522	2026-04-07 16:56:16.326522		BUTTON	\N	\N	0	t	t	\N
33bf54e7-5fe4-499f-8888-a734332fd80d	USER_WRITE	管理用户	2026-04-07 16:56:16.326522	2026-04-07 16:56:16.326522		BUTTON	\N	\N	0	t	t	\N
d7316b07-bf1d-455c-b25d-f5c4374a94f5	ORDER_READ	查看工单	2026-04-07 16:56:16.326522	2026-04-07 16:56:16.326522		BUTTON	\N	\N	0	t	t	\N
637f2346-8a5e-4538-8b4b-7bea52058dc2	ORDER_WRITE	管理工单	2026-04-07 16:56:16.326522	2026-04-07 16:56:16.326522		BUTTON	\N	\N	0	t	t	\N
82874628-73b3-4ad3-807e-90631a58da38	SCHEDULE_READ	查看排产	2026-04-07 16:56:16.326522	2026-04-07 16:56:16.326522		BUTTON	\N	\N	0	t	t	\N
5c5955f3-6442-45dc-afe5-a613002e3b30	SCHEDULE_WRITE	管理排产	2026-04-07 16:56:16.326522	2026-04-07 16:56:16.326522		BUTTON	\N	\N	0	t	t	\N
1fdaad35-cf7a-4de7-b94f-26c4cb85dd99	system	系统管理模块	2026-04-07 16:56:16.66447	2026-04-07 16:56:16.66447	系统管理	CATALOG	\N	Setting	1	t	t	\N
6e95e535-a7c4-41d4-9d17-3913fc393d7b	schedule	排产管理模块	2026-04-07 16:56:16.66447	2026-04-07 16:56:16.66447	排产管理	CATALOG	\N	Calendar	2	t	t	\N
59a5a21d-87b0-4602-840f-decd92c160c9	system:user:delete	删除用户	2026-04-07 17:11:37.209435	2026-04-07 17:11:37.209435	删除用户	BUTTON	\N	Delete	4	t	t	b70d936e-9a70-492c-a8bc-59bd3be97604
334bc965-0ff4-4716-8bcb-d03e476dce0f	system:user:reset_password	重置用户密码	2026-04-07 17:11:37.209435	2026-04-07 17:11:37.209435	重置密码	BUTTON	\N	Key	5	t	t	b70d936e-9a70-492c-a8bc-59bd3be97604
b70d936e-9a70-492c-a8bc-59bd3be97604	system:user	用户管理页面	2026-04-07 16:56:16.66447	2026-04-07 16:56:16.66447	用户管理	MENU	\N	User	1	t	t	1fdaad35-cf7a-4de7-b94f-26c4cb85dd99
49d90997-32ac-4092-a29f-eb11e0b5f4d4	system:permission	权限管理页面	2026-04-07 16:56:16.66447	2026-04-07 16:56:16.66447	权限管理	MENU	\N	Lock	3	t	t	1fdaad35-cf7a-4de7-b94f-26c4cb85dd99
44c70c91-e25c-4aff-a9cb-7d8078cf1f5b	system:role	角色管理页面	2026-04-07 16:56:16.66447	2026-04-07 16:56:16.66447	角色管理	MENU	\N	UserFilled	2	t	t	1fdaad35-cf7a-4de7-b94f-26c4cb85dd99
cd4596a1-ff88-4494-9f1d-ed961f0041a8	system:user:list	查看用户列表	2026-04-07 16:56:16.66447	2026-04-07 16:56:16.66447	用户列表	BUTTON	\N	View	1	t	t	b70d936e-9a70-492c-a8bc-59bd3be97604
fd79a1a7-ab1a-4f9a-be87-f4ae0f1ceb51	system:user:add	新增用户	2026-04-07 16:56:16.66447	2026-04-07 16:56:16.66447	新增用户	BUTTON	\N	Plus	2	t	t	b70d936e-9a70-492c-a8bc-59bd3be97604
575bebb5-d3bc-47f3-a3a4-b8326c2da6a2	system:user:edit	编辑用户	2026-04-07 16:56:16.66447	2026-04-07 16:56:16.66447	编辑用户	BUTTON	\N	Edit	3	t	t	b70d936e-9a70-492c-a8bc-59bd3be97604
35f6f631-4d11-484f-a7c0-7fd20642c734	system:user:remove	删除用户	2026-04-07 16:56:16.66447	2026-04-07 16:56:16.66447	删除用户	BUTTON	\N	Delete	4	t	t	b70d936e-9a70-492c-a8bc-59bd3be97604
51dbfd52-ceda-4f52-bb62-49ccd38fd17a	system:permission:list	查看权限列表	2026-04-07 16:56:16.66447	2026-04-07 16:56:16.66447	权限列表	BUTTON	\N	View	1	t	t	49d90997-32ac-4092-a29f-eb11e0b5f4d4
9622b888-c8c1-49f4-9e67-0bcdf04e53dd	system:permission:query	查看权限详情	2026-04-07 16:56:16.66447	2026-04-07 16:56:16.66447	权限详情	BUTTON	\N	View	2	t	t	49d90997-32ac-4092-a29f-eb11e0b5f4d4
f9c29ea6-6cfb-4f65-9adf-479115c2e820	system:permission:add	新增权限	2026-04-07 16:56:16.66447	2026-04-07 16:56:16.66447	新增权限	BUTTON	\N	Plus	3	t	t	49d90997-32ac-4092-a29f-eb11e0b5f4d4
72a522fd-b04f-4735-82e5-bec9a1fab083	system:permission:edit	编辑权限	2026-04-07 16:56:16.66447	2026-04-07 16:56:16.66447	编辑权限	BUTTON	\N	Edit	4	t	t	49d90997-32ac-4092-a29f-eb11e0b5f4d4
2516d536-b9c4-4fa6-b29e-8363e7d7f8a3	system:permission:remove	删除权限	2026-04-07 16:56:16.66447	2026-04-07 16:56:16.66447	删除权限	BUTTON	\N	Delete	5	t	t	49d90997-32ac-4092-a29f-eb11e0b5f4d4
d465d5a7-dd01-4282-84c8-0a32b899dc1a	system:role:list	查看角色列表	2026-04-07 16:56:16.66447	2026-04-07 16:56:16.66447	角色列表	BUTTON	\N	View	1	t	t	44c70c91-e25c-4aff-a9cb-7d8078cf1f5b
98359d79-fb34-4a45-bdf6-84ec2ad1da36	system:role:add	新增角色	2026-04-07 16:56:16.66447	2026-04-07 16:56:16.66447	新增角色	BUTTON	\N	Plus	2	t	t	44c70c91-e25c-4aff-a9cb-7d8078cf1f5b
bfec0ede-ce2d-485c-b741-ace73ddc259f	system:role:edit	编辑角色	2026-04-07 16:56:16.66447	2026-04-07 16:56:16.66447	编辑角色	BUTTON	\N	Edit	3	t	t	44c70c91-e25c-4aff-a9cb-7d8078cf1f5b
ebb7287c-07af-4c78-b356-a01961d50cb0	system:role:remove	删除角色	2026-04-07 16:56:16.66447	2026-04-07 16:56:16.66447	删除角色	BUTTON	\N	Delete	4	t	t	44c70c91-e25c-4aff-a9cb-7d8078cf1f5b
aaa1982a-f108-46b3-8084-77a4192e2b97	schedule:plan	排产计划页面	2026-04-07 16:56:16.66447	2026-04-07 16:56:16.66447	排产计划	MENU	\N	Calendar	1	t	t	6e95e535-a7c4-41d4-9d17-3913fc393d7b
1286afec-42c4-4e54-bff9-ef46787520ed	schedule:plan:list	查看计划列表	2026-04-07 16:56:16.66447	2026-04-07 16:56:16.66447	计划列表	BUTTON	\N	View	1	t	t	aaa1982a-f108-46b3-8084-77a4192e2b97
14f5dad7-697e-49ca-8d36-2861d92153de	schedule:plan:create	创建排产计划	2026-04-07 16:56:16.66447	2026-04-07 16:56:16.66447	创建计划	BUTTON	\N	Plus	2	t	t	aaa1982a-f108-46b3-8084-77a4192e2b97
12ac1140-c27a-47ab-976e-666c0300e70c	schedule:plan:solve	执行排产求解	2026-04-07 16:56:16.66447	2026-04-07 16:56:16.66447	执行求解	BUTTON	\N	VideoPlay	3	t	t	aaa1982a-f108-46b3-8084-77a4192e2b97
3cb50220-3784-46cf-9836-2e6707148bd2	schedule:plan:stop	停止排产求解	2026-04-07 16:56:16.66447	2026-04-07 16:56:16.66447	停止求解	BUTTON	\N	VideoPause	4	t	t	aaa1982a-f108-46b3-8084-77a4192e2b97
868b1564-ff07-4695-984b-689fdd939ab6	system:dict	编码管理页面	2026-04-09 18:16:04.119466	2026-04-09 18:16:04.119466	编码管理	MENU	\N	Collection	5	t	t	1fdaad35-cf7a-4de7-b94f-26c4cb85dd99
5e3a6390-3919-4b45-854d-f90936d1bec2	system:dict:type:list	查看字典类型列表	2026-04-09 18:16:04.119466	2026-04-09 18:16:04.119466	字典类型列表	BUTTON	\N	View	1	t	t	868b1564-ff07-4695-984b-689fdd939ab6
61ad9f8b-24fe-4ed0-9865-1cb3a078fe94	system:dict:type:query	查看字典类型详情	2026-04-09 18:16:04.119466	2026-04-09 18:16:04.119466	字典类型详情	BUTTON	\N	View	2	t	t	868b1564-ff07-4695-984b-689fdd939ab6
f5bbab30-5491-472b-b7be-e81f01023be2	system:dict:type:add	新增字典类型	2026-04-09 18:16:04.119466	2026-04-09 18:16:04.119466	新增字典类型	BUTTON	\N	Plus	3	t	t	868b1564-ff07-4695-984b-689fdd939ab6
f07e6bd3-273f-4e30-a200-2fd80c359267	system:dict:type:edit	编辑字典类型	2026-04-09 18:16:04.119466	2026-04-09 18:16:04.119466	编辑字典类型	BUTTON	\N	Edit	4	t	t	868b1564-ff07-4695-984b-689fdd939ab6
ce05dfe2-2c3c-49e8-ad8e-565ee98d9f16	system:dict:type:remove	删除字典类型	2026-04-09 18:16:04.119466	2026-04-09 18:16:04.119466	删除字典类型	BUTTON	\N	Delete	5	t	t	868b1564-ff07-4695-984b-689fdd939ab6
5daa519f-a91f-4eb1-bad2-95be62ce5c03	system:dict:item:list	查看字典项列表	2026-04-09 18:16:04.119466	2026-04-09 18:16:04.119466	字典项列表	BUTTON	\N	View	6	t	t	868b1564-ff07-4695-984b-689fdd939ab6
e03a2539-1d22-4984-8fd7-7c35d8406e2b	system:dict:item:query	查看字典项详情	2026-04-09 18:16:04.119466	2026-04-09 18:16:04.119466	字典项详情	BUTTON	\N	View	7	t	t	868b1564-ff07-4695-984b-689fdd939ab6
35740f9e-0941-450d-89d1-0886ecbc47c2	system:dict:item:add	新增字典项	2026-04-09 18:16:04.119466	2026-04-09 18:16:04.119466	新增字典项	BUTTON	\N	Plus	8	t	t	868b1564-ff07-4695-984b-689fdd939ab6
9c5f2943-0f00-4bb1-852c-523c5373a3c3	system:dict:item:edit	编辑字典项	2026-04-09 18:16:04.119466	2026-04-09 18:16:04.119466	编辑字典项	BUTTON	\N	Edit	9	t	t	868b1564-ff07-4695-984b-689fdd939ab6
6cd3bfa1-d988-463a-a16c-ec5c43f5b0fc	system:dict:item:remove	删除字典项	2026-04-09 18:16:04.119466	2026-04-09 18:16:04.119466	删除字典项	BUTTON	\N	Delete	10	t	t	868b1564-ff07-4695-984b-689fdd939ab6
af9e968e-c924-4ce7-b347-89ffeb9e37db	system:dict:query	按类型编码查询启用字典项	2026-04-09 18:16:04.119466	2026-04-09 18:16:04.119466	字典通用查询	BUTTON	\N	View	11	t	t	868b1564-ff07-4695-984b-689fdd939ab6
50063b81-7980-458d-8e20-95cf6770f5ff	basedata	基础数据模块	2026-04-10 15:06:37.135083	2026-04-10 15:06:37.135083	基础数据	CATALOG	\N	Setting	3	t	t	\N
d763e93b-33a6-4c68-b6a2-5218dc019377	basedata:workshop	工厂建模页面	2026-04-10 15:06:37.135083	2026-04-10 15:06:37.135083	工厂建模	MENU	\N	Setting	1	t	t	50063b81-7980-458d-8e20-95cf6770f5ff
77398f30-e92f-4d0a-a8b3-9b32afa0e6a8	basedata:workshop:list	查看车间与设备列表	2026-04-10 15:06:37.135083	2026-04-10 15:06:37.135083	查看工厂建模	BUTTON	\N	View	1	t	t	d763e93b-33a6-4c68-b6a2-5218dc019377
3347dc13-0f21-493c-b3e3-9b201ac30c0e	basedata:workshop:add	新增车间或设备	2026-04-10 15:06:37.135083	2026-04-10 15:06:37.135083	新增车间/设备	BUTTON	\N	Plus	2	t	t	d763e93b-33a6-4c68-b6a2-5218dc019377
10056d3e-dc3d-4911-a3a6-ec4bad212f03	basedata:workshop:edit	编辑车间或设备	2026-04-10 15:06:37.135083	2026-04-10 15:06:37.135083	编辑车间/设备	BUTTON	\N	Edit	3	t	t	d763e93b-33a6-4c68-b6a2-5218dc019377
6f1fa02f-425f-4eb2-af75-e4e93cca09d0	basedata:workshop:delete	删除车间或设备	2026-04-10 15:06:37.135083	2026-04-10 15:06:37.135083	删除车间/设备	BUTTON	\N	Delete	4	t	t	d763e93b-33a6-4c68-b6a2-5218dc019377
5cd09214-cf53-4743-a1e6-59e6c25282da	basedata:resource-capacity	设备日产能管理页面	2026-04-10 15:06:37.135083	2026-04-10 15:06:37.135083	设备日产能	MENU	\N	Cpu	2	t	t	50063b81-7980-458d-8e20-95cf6770f5ff
08585c46-2bc1-411c-bfa3-53a595b9da7e	basedata:resource-capacity:list	查看设备产能列表	2026-04-10 15:06:37.135083	2026-04-10 15:06:37.135083	查看设备日产能	BUTTON	\N	View	1	t	t	5cd09214-cf53-4743-a1e6-59e6c25282da
31cc8a52-1398-4cad-a712-f4e354ec94e3	basedata:resource-capacity:edit	编辑设备产能数据	2026-04-10 15:06:37.135083	2026-04-10 15:06:37.135083	编辑设备日产能	BUTTON	\N	Edit	2	t	t	5cd09214-cf53-4743-a1e6-59e6c25282da
5e08a391-9ccd-4bfb-8d14-6c797824b02c	basedata:resource-capacity:batch-edit	批量编辑设备产能数据	2026-04-10 15:06:37.135083	2026-04-10 15:06:37.135083	批量编辑产能	BUTTON	\N	Edit	3	t	t	5cd09214-cf53-4743-a1e6-59e6c25282da
dad72a8a-f614-4d84-9192-ebdab8c979df	system:audit-log	审计日志页面	2026-04-10 15:12:44.199797	2026-04-10 15:12:44.199797	审计日志	MENU	\N	Document	4	t	t	1fdaad35-cf7a-4de7-b94f-26c4cb85dd99
98aee902-e94c-42d5-8f1d-4af986739506	system:audit-log:list	分页查看审计日志	2026-04-10 15:12:44.199797	2026-04-10 15:12:44.199797	查看审计日志	BUTTON	\N	View	1	t	t	dad72a8a-f614-4d84-9192-ebdab8c979df
3e2eb23e-0bd0-49e0-bbb7-a6cfb1b5a634	system:audit-log:query	查看审计日志详情	2026-04-10 15:12:44.199797	2026-04-10 15:12:44.199797	审计日志详情	BUTTON	\N	View	2	t	t	dad72a8a-f614-4d84-9192-ebdab8c979df
6051583e-672e-49be-8970-247f5693fcc2	system:audit-log:search	按条件搜索审计日志	2026-04-10 15:12:44.199797	2026-04-10 15:12:44.199797	搜索审计日志	BUTTON	\N	Search	3	t	t	dad72a8a-f614-4d84-9192-ebdab8c979df
eaeba481-0c3f-4836-a572-12432b83f6b6	system:audit-log:statistics	查看审计日志统计分析	2026-04-10 15:12:44.199797	2026-04-10 15:12:44.199797	审计日志统计	BUTTON	\N	DataAnalysis	4	t	t	dad72a8a-f614-4d84-9192-ebdab8c979df
41616e21-9d23-4b2e-919a-1ba288364eef	system:audit-log:export	导出审计日志CSV	2026-04-10 15:12:44.199797	2026-04-10 15:12:44.199797	导出审计日志	BUTTON	\N	Download	5	t	t	dad72a8a-f614-4d84-9192-ebdab8c979df
52872c37-f675-4609-994f-d70e0ce0f8b2	system:factory-calendar	工厂日历页面	2026-04-10 15:12:44.199797	2026-04-10 15:12:44.199797	工厂日历	MENU	\N	Calendar	6	t	t	1fdaad35-cf7a-4de7-b94f-26c4cb85dd99
27a9202d-7267-49eb-b153-8ae92d2d5cec	system:factory-calendar:list	查看工厂日历列表	2026-04-10 15:12:44.199797	2026-04-10 15:12:44.199797	查看工厂日历	BUTTON	\N	View	1	t	t	52872c37-f675-4609-994f-d70e0ce0f8b2
4b4b1fd3-ea20-4797-b01a-47cbf58db5b2	system:factory-calendar:add	新增工厂日历	2026-04-10 15:12:44.199797	2026-04-10 15:12:44.199797	新增工厂日历	BUTTON	\N	Plus	2	t	t	52872c37-f675-4609-994f-d70e0ce0f8b2
80554b7d-9f27-41e2-8874-d07e39ca4903	system:factory-calendar:edit	编辑工厂日历与班次日期	2026-04-10 15:12:44.199797	2026-04-10 15:12:44.199797	编辑工厂日历	BUTTON	\N	Edit	3	t	t	52872c37-f675-4609-994f-d70e0ce0f8b2
c613086d-fdaf-4459-a70f-cb8277dbd00b	system:factory-calendar:delete	删除工厂日历	2026-04-10 15:12:44.199797	2026-04-10 15:12:44.199797	删除工厂日历	BUTTON	\N	Delete	4	t	t	52872c37-f675-4609-994f-d70e0ce0f8b2
dbf7af74-75ab-4323-9aff-55529dd94677	system:factory-calendar:set-default	设置默认工厂日历	2026-04-10 15:12:44.199797	2026-04-10 15:12:44.199797	设置默认日历	BUTTON	\N	Select	5	t	t	52872c37-f675-4609-994f-d70e0ce0f8b2
a7f37ef8-0bb3-4eab-82e5-4e57d5b07f8e	basedata:factory-calendar	工厂日历页面	2026-04-10 16:48:46.439659	2026-04-10 16:48:46.439659	工厂日历	MENU	\N	Calendar	3	t	t	50063b81-7980-458d-8e20-95cf6770f5ff
35becc0c-2a6e-4da8-8b96-fd761c20bc0b	basedata:factory-calendar:list	查看工厂日历列表	2026-04-10 16:48:46.439659	2026-04-10 16:48:46.439659	查看工厂日历	BUTTON	\N	View	1	t	t	a7f37ef8-0bb3-4eab-82e5-4e57d5b07f8e
decb5d39-59ed-4bfe-9594-e01ba4b4c62a	basedata:factory-calendar:query	查看工厂日历详情与班次日期	2026-04-10 16:48:46.439659	2026-04-10 16:48:46.439659	工厂日历详情	BUTTON	\N	View	2	t	t	a7f37ef8-0bb3-4eab-82e5-4e57d5b07f8e
862a9163-f98e-4ca0-82d7-8866b13d2e29	basedata:factory-calendar:add	新增工厂日历	2026-04-10 16:48:46.439659	2026-04-10 16:48:46.439659	新增工厂日历	BUTTON	\N	Plus	3	t	t	a7f37ef8-0bb3-4eab-82e5-4e57d5b07f8e
fc42ba4f-508e-45ce-80ad-3c25dc9c22e7	basedata:factory-calendar:edit	编辑工厂日历与班次日期	2026-04-10 16:48:46.439659	2026-04-10 16:48:46.439659	编辑工厂日历	BUTTON	\N	Edit	4	t	t	a7f37ef8-0bb3-4eab-82e5-4e57d5b07f8e
6f35cb80-2ac2-428b-9894-dee34dcec38a	basedata:factory-calendar:remove	删除工厂日历与班次	2026-04-10 16:48:46.439659	2026-04-10 16:48:46.439659	删除工厂日历	BUTTON	\N	Delete	5	t	t	a7f37ef8-0bb3-4eab-82e5-4e57d5b07f8e
d6903f84-ea37-46cf-9f43-ab6e46094db4	schedule:time-param	排程时间参数配置页面	2026-04-10 21:14:50.763173	2026-04-10 21:14:50.763173	排程时间参数	MENU	\N	Timer	1	t	t	6e95e535-a7c4-41d4-9d17-3913fc393d7b
299c4717-0f8e-4777-abc1-4c36c13ea195	schedule:time-param:list	查看排程时间参数列表	2026-04-10 21:14:50.763173	2026-04-10 21:14:50.763173	查看排程时间参数	BUTTON	\N	View	1	t	t	d6903f84-ea37-46cf-9f43-ab6e46094db4
d857b6ae-fe81-4eec-89e1-0638735baf86	schedule:time-param:add	新增排程时间参数	2026-04-10 21:14:50.763173	2026-04-10 21:14:50.763173	新增排程时间参数	BUTTON	\N	Plus	2	t	t	d6903f84-ea37-46cf-9f43-ab6e46094db4
04743014-17dd-45b8-8f93-16e5caf68483	schedule:time-param:edit	编辑排程时间参数	2026-04-10 21:14:50.763173	2026-04-10 21:14:50.763173	编辑排程时间参数	BUTTON	\N	Edit	3	t	t	d6903f84-ea37-46cf-9f43-ab6e46094db4
2e36ecaa-3dc5-4265-95b8-ec9120b5122f	schedule:time-param:remove	删除排程时间参数	2026-04-10 21:14:50.763173	2026-04-10 21:14:50.763173	删除排程时间参数	BUTTON	\N	Delete	4	t	t	d6903f84-ea37-46cf-9f43-ab6e46094db4
5924ec62-2ba6-4665-a21e-ac6501c071d6	schedule:time-param:preview	预览排程时间参数计算结果	2026-04-10 21:14:50.763173	2026-04-10 21:14:50.763173	预览排程时间参数	BUTTON	\N	View	5	t	t	d6903f84-ea37-46cf-9f43-ab6e46094db4
\.


--
-- TOC entry 3721 (class 0 OID 17495)
-- Dependencies: 235
-- Data for Name: resource_capacity_days; Type: TABLE DATA; Schema: public; Owner: aps_user
--

COPY public.resource_capacity_days (id, resource_id, capacity_date, shift_minutes_override, utilization_rate, remark, create_time, update_time) FROM stdin;
\.


--
-- TOC entry 3709 (class 0 OID 17269)
-- Dependencies: 223
-- Data for Name: resources; Type: TABLE DATA; Schema: public; Owner: aps_user
--

COPY public.resources (id, resource_code, resource_name, resource_type, available, create_time, update_time, workshop_id, tonnage, machine_brand, machine_model, max_shot_weight, status, calendar_id) FROM stdin;
4987fb16-e76c-45f7-802c-4f16423bcd69	ZS001	注塑1号机	INJECTION_MACHINE	t	2026-04-09 15:01:27.805464	2026-04-10 08:33:11.893034	0ec60c75-a830-4651-ab47-71f7d2b42ad7	180	力经		180.00	IDLE	8648aa7c-5574-479f-9084-a9ca960b6855
3ddbcd15-2f92-44fa-a17a-00996129ec01	ZS11	qqq	INJECTION_MACHINE	t	2026-04-10 09:12:21.112677	2026-04-10 15:42:07.255439	1c56f6a5-575e-4534-9e57-28444fb62f28	\N			\N	IDLE	a73e562e-fc18-4ac3-b966-d67799e3ae80
\.


--
-- TOC entry 3707 (class 0 OID 17238)
-- Dependencies: 221
-- Data for Name: role_permissions; Type: TABLE DATA; Schema: public; Owner: aps_user
--

COPY public.role_permissions (role_id, permission_id) FROM stdin;
5f685927-09b9-4f5b-a20e-8561748e7f99	4199134d-91f4-4b66-883c-c9791ce2deac
5f685927-09b9-4f5b-a20e-8561748e7f99	33bf54e7-5fe4-499f-8888-a734332fd80d
5f685927-09b9-4f5b-a20e-8561748e7f99	d7316b07-bf1d-455c-b25d-f5c4374a94f5
5f685927-09b9-4f5b-a20e-8561748e7f99	637f2346-8a5e-4538-8b4b-7bea52058dc2
5f685927-09b9-4f5b-a20e-8561748e7f99	82874628-73b3-4ad3-807e-90631a58da38
5f685927-09b9-4f5b-a20e-8561748e7f99	5c5955f3-6442-45dc-afe5-a613002e3b30
fd9ec3a8-bf7c-41fb-a424-36a6ac7b23f3	d7316b07-bf1d-455c-b25d-f5c4374a94f5
fd9ec3a8-bf7c-41fb-a424-36a6ac7b23f3	637f2346-8a5e-4538-8b4b-7bea52058dc2
fd9ec3a8-bf7c-41fb-a424-36a6ac7b23f3	82874628-73b3-4ad3-807e-90631a58da38
fd9ec3a8-bf7c-41fb-a424-36a6ac7b23f3	5c5955f3-6442-45dc-afe5-a613002e3b30
d831f5b3-b0a9-436b-b9cd-6b5c32b54d42	4199134d-91f4-4b66-883c-c9791ce2deac
d831f5b3-b0a9-436b-b9cd-6b5c32b54d42	d7316b07-bf1d-455c-b25d-f5c4374a94f5
d831f5b3-b0a9-436b-b9cd-6b5c32b54d42	82874628-73b3-4ad3-807e-90631a58da38
d831f5b3-b0a9-436b-b9cd-6b5c32b54d42	5c5955f3-6442-45dc-afe5-a613002e3b30
5f685927-09b9-4f5b-a20e-8561748e7f99	f9c29ea6-6cfb-4f65-9adf-479115c2e820
5f685927-09b9-4f5b-a20e-8561748e7f99	fd79a1a7-ab1a-4f9a-be87-f4ae0f1ceb51
5f685927-09b9-4f5b-a20e-8561748e7f99	9622b888-c8c1-49f4-9e67-0bcdf04e53dd
5f685927-09b9-4f5b-a20e-8561748e7f99	98359d79-fb34-4a45-bdf6-84ec2ad1da36
5f685927-09b9-4f5b-a20e-8561748e7f99	49d90997-32ac-4092-a29f-eb11e0b5f4d4
5f685927-09b9-4f5b-a20e-8561748e7f99	aaa1982a-f108-46b3-8084-77a4192e2b97
5f685927-09b9-4f5b-a20e-8561748e7f99	14f5dad7-697e-49ca-8d36-2861d92153de
5f685927-09b9-4f5b-a20e-8561748e7f99	cd4596a1-ff88-4494-9f1d-ed961f0041a8
5f685927-09b9-4f5b-a20e-8561748e7f99	bfec0ede-ce2d-485c-b741-ace73ddc259f
5f685927-09b9-4f5b-a20e-8561748e7f99	575bebb5-d3bc-47f3-a3a4-b8326c2da6a2
5f685927-09b9-4f5b-a20e-8561748e7f99	12ac1140-c27a-47ab-976e-666c0300e70c
5f685927-09b9-4f5b-a20e-8561748e7f99	3cb50220-3784-46cf-9836-2e6707148bd2
5f685927-09b9-4f5b-a20e-8561748e7f99	ebb7287c-07af-4c78-b356-a01961d50cb0
5f685927-09b9-4f5b-a20e-8561748e7f99	72a522fd-b04f-4735-82e5-bec9a1fab083
5f685927-09b9-4f5b-a20e-8561748e7f99	d465d5a7-dd01-4282-84c8-0a32b899dc1a
5f685927-09b9-4f5b-a20e-8561748e7f99	b70d936e-9a70-492c-a8bc-59bd3be97604
5f685927-09b9-4f5b-a20e-8561748e7f99	44c70c91-e25c-4aff-a9cb-7d8078cf1f5b
5f685927-09b9-4f5b-a20e-8561748e7f99	6e95e535-a7c4-41d4-9d17-3913fc393d7b
5f685927-09b9-4f5b-a20e-8561748e7f99	35f6f631-4d11-484f-a7c0-7fd20642c734
5f685927-09b9-4f5b-a20e-8561748e7f99	2516d536-b9c4-4fa6-b29e-8363e7d7f8a3
5f685927-09b9-4f5b-a20e-8561748e7f99	51dbfd52-ceda-4f52-bb62-49ccd38fd17a
5f685927-09b9-4f5b-a20e-8561748e7f99	1286afec-42c4-4e54-bff9-ef46787520ed
5f685927-09b9-4f5b-a20e-8561748e7f99	1fdaad35-cf7a-4de7-b94f-26c4cb85dd99
5f685927-09b9-4f5b-a20e-8561748e7f99	59a5a21d-87b0-4602-840f-decd92c160c9
5f685927-09b9-4f5b-a20e-8561748e7f99	334bc965-0ff4-4716-8bcb-d03e476dce0f
5f685927-09b9-4f5b-a20e-8561748e7f99	af9e968e-c924-4ce7-b347-89ffeb9e37db
5f685927-09b9-4f5b-a20e-8561748e7f99	5e3a6390-3919-4b45-854d-f90936d1bec2
5f685927-09b9-4f5b-a20e-8561748e7f99	5daa519f-a91f-4eb1-bad2-95be62ce5c03
5f685927-09b9-4f5b-a20e-8561748e7f99	e03a2539-1d22-4984-8fd7-7c35d8406e2b
5f685927-09b9-4f5b-a20e-8561748e7f99	9c5f2943-0f00-4bb1-852c-523c5373a3c3
5f685927-09b9-4f5b-a20e-8561748e7f99	6cd3bfa1-d988-463a-a16c-ec5c43f5b0fc
5f685927-09b9-4f5b-a20e-8561748e7f99	868b1564-ff07-4695-984b-689fdd939ab6
5f685927-09b9-4f5b-a20e-8561748e7f99	f07e6bd3-273f-4e30-a200-2fd80c359267
5f685927-09b9-4f5b-a20e-8561748e7f99	ce05dfe2-2c3c-49e8-ad8e-565ee98d9f16
5f685927-09b9-4f5b-a20e-8561748e7f99	35740f9e-0941-450d-89d1-0886ecbc47c2
5f685927-09b9-4f5b-a20e-8561748e7f99	f5bbab30-5491-472b-b7be-e81f01023be2
5f685927-09b9-4f5b-a20e-8561748e7f99	61ad9f8b-24fe-4ed0-9865-1cb3a078fe94
5f685927-09b9-4f5b-a20e-8561748e7f99	d763e93b-33a6-4c68-b6a2-5218dc019377
5f685927-09b9-4f5b-a20e-8561748e7f99	3347dc13-0f21-493c-b3e3-9b201ac30c0e
5f685927-09b9-4f5b-a20e-8561748e7f99	6f1fa02f-425f-4eb2-af75-e4e93cca09d0
5f685927-09b9-4f5b-a20e-8561748e7f99	5e08a391-9ccd-4bfb-8d14-6c797824b02c
5f685927-09b9-4f5b-a20e-8561748e7f99	50063b81-7980-458d-8e20-95cf6770f5ff
5f685927-09b9-4f5b-a20e-8561748e7f99	31cc8a52-1398-4cad-a712-f4e354ec94e3
5f685927-09b9-4f5b-a20e-8561748e7f99	77398f30-e92f-4d0a-a8b3-9b32afa0e6a8
5f685927-09b9-4f5b-a20e-8561748e7f99	5cd09214-cf53-4743-a1e6-59e6c25282da
5f685927-09b9-4f5b-a20e-8561748e7f99	08585c46-2bc1-411c-bfa3-53a595b9da7e
5f685927-09b9-4f5b-a20e-8561748e7f99	10056d3e-dc3d-4911-a3a6-ec4bad212f03
fd9ec3a8-bf7c-41fb-a424-36a6ac7b23f3	5cd09214-cf53-4743-a1e6-59e6c25282da
fd9ec3a8-bf7c-41fb-a424-36a6ac7b23f3	08585c46-2bc1-411c-bfa3-53a595b9da7e
fd9ec3a8-bf7c-41fb-a424-36a6ac7b23f3	5e08a391-9ccd-4bfb-8d14-6c797824b02c
fd9ec3a8-bf7c-41fb-a424-36a6ac7b23f3	50063b81-7980-458d-8e20-95cf6770f5ff
fd9ec3a8-bf7c-41fb-a424-36a6ac7b23f3	31cc8a52-1398-4cad-a712-f4e354ec94e3
d831f5b3-b0a9-436b-b9cd-6b5c32b54d42	5cd09214-cf53-4743-a1e6-59e6c25282da
d831f5b3-b0a9-436b-b9cd-6b5c32b54d42	08585c46-2bc1-411c-bfa3-53a595b9da7e
d831f5b3-b0a9-436b-b9cd-6b5c32b54d42	50063b81-7980-458d-8e20-95cf6770f5ff
5f685927-09b9-4f5b-a20e-8561748e7f99	80554b7d-9f27-41e2-8874-d07e39ca4903
5f685927-09b9-4f5b-a20e-8561748e7f99	dbf7af74-75ab-4323-9aff-55529dd94677
5f685927-09b9-4f5b-a20e-8561748e7f99	52872c37-f675-4609-994f-d70e0ce0f8b2
5f685927-09b9-4f5b-a20e-8561748e7f99	eaeba481-0c3f-4836-a572-12432b83f6b6
5f685927-09b9-4f5b-a20e-8561748e7f99	3e2eb23e-0bd0-49e0-bbb7-a6cfb1b5a634
5f685927-09b9-4f5b-a20e-8561748e7f99	c613086d-fdaf-4459-a70f-cb8277dbd00b
5f685927-09b9-4f5b-a20e-8561748e7f99	27a9202d-7267-49eb-b153-8ae92d2d5cec
5f685927-09b9-4f5b-a20e-8561748e7f99	41616e21-9d23-4b2e-919a-1ba288364eef
5f685927-09b9-4f5b-a20e-8561748e7f99	98aee902-e94c-42d5-8f1d-4af986739506
5f685927-09b9-4f5b-a20e-8561748e7f99	6051583e-672e-49be-8970-247f5693fcc2
5f685927-09b9-4f5b-a20e-8561748e7f99	dad72a8a-f614-4d84-9192-ebdab8c979df
5f685927-09b9-4f5b-a20e-8561748e7f99	4b4b1fd3-ea20-4797-b01a-47cbf58db5b2
5f685927-09b9-4f5b-a20e-8561748e7f99	35becc0c-2a6e-4da8-8b96-fd761c20bc0b
5f685927-09b9-4f5b-a20e-8561748e7f99	a7f37ef8-0bb3-4eab-82e5-4e57d5b07f8e
5f685927-09b9-4f5b-a20e-8561748e7f99	6f35cb80-2ac2-428b-9894-dee34dcec38a
5f685927-09b9-4f5b-a20e-8561748e7f99	862a9163-f98e-4ca0-82d7-8866b13d2e29
5f685927-09b9-4f5b-a20e-8561748e7f99	decb5d39-59ed-4bfe-9594-e01ba4b4c62a
5f685927-09b9-4f5b-a20e-8561748e7f99	fc42ba4f-508e-45ce-80ad-3c25dc9c22e7
fd9ec3a8-bf7c-41fb-a424-36a6ac7b23f3	6e95e535-a7c4-41d4-9d17-3913fc393d7b
fd9ec3a8-bf7c-41fb-a424-36a6ac7b23f3	d857b6ae-fe81-4eec-89e1-0638735baf86
fd9ec3a8-bf7c-41fb-a424-36a6ac7b23f3	299c4717-0f8e-4777-abc1-4c36c13ea195
5f685927-09b9-4f5b-a20e-8561748e7f99	2e36ecaa-3dc5-4265-95b8-ec9120b5122f
5f685927-09b9-4f5b-a20e-8561748e7f99	5924ec62-2ba6-4665-a21e-ac6501c071d6
fd9ec3a8-bf7c-41fb-a424-36a6ac7b23f3	04743014-17dd-45b8-8f93-16e5caf68483
5f685927-09b9-4f5b-a20e-8561748e7f99	d6903f84-ea37-46cf-9f43-ab6e46094db4
5f685927-09b9-4f5b-a20e-8561748e7f99	d857b6ae-fe81-4eec-89e1-0638735baf86
fd9ec3a8-bf7c-41fb-a424-36a6ac7b23f3	d6903f84-ea37-46cf-9f43-ab6e46094db4
5f685927-09b9-4f5b-a20e-8561748e7f99	04743014-17dd-45b8-8f93-16e5caf68483
5f685927-09b9-4f5b-a20e-8561748e7f99	299c4717-0f8e-4777-abc1-4c36c13ea195
fd9ec3a8-bf7c-41fb-a424-36a6ac7b23f3	2e36ecaa-3dc5-4265-95b8-ec9120b5122f
fd9ec3a8-bf7c-41fb-a424-36a6ac7b23f3	5924ec62-2ba6-4665-a21e-ac6501c071d6
\.


--
-- TOC entry 3704 (class 0 OID 17201)
-- Dependencies: 218
-- Data for Name: roles; Type: TABLE DATA; Schema: public; Owner: aps_user
--

COPY public.roles (id, name, description, create_time, update_time) FROM stdin;
5f685927-09b9-4f5b-a20e-8561748e7f99	ADMIN	系统管理员	2026-04-07 16:56:16.326522	2026-04-07 16:56:16.326522
fd9ec3a8-bf7c-41fb-a424-36a6ac7b23f3	PLANNER	计划员	2026-04-07 16:56:16.326522	2026-04-07 16:56:16.326522
d831f5b3-b0a9-436b-b9cd-6b5c32b54d42	SUPERVISOR	主管	2026-04-07 16:56:16.326522	2026-04-07 16:56:16.326522
efa55144-8757-4fb0-9e29-2d37bb9baf86	opertion	操作员	2026-04-08 17:20:44.524076	2026-04-08 17:20:44.524076
\.


--
-- TOC entry 3713 (class 0 OID 17316)
-- Dependencies: 227
-- Data for Name: schedule_resources; Type: TABLE DATA; Schema: public; Owner: aps_user
--

COPY public.schedule_resources (schedule_id, resource_id) FROM stdin;
\.


--
-- TOC entry 3722 (class 0 OID 17515)
-- Dependencies: 236
-- Data for Name: schedule_time_parameters; Type: TABLE DATA; Schema: public; Owner: aps_user
--

COPY public.schedule_time_parameters (id, resource_id, order_filter_start_days, order_filter_start_time, order_filter_end_days, order_filter_end_time, planning_start_days, planning_start_time, display_start_days, display_end_days, completion_days, time_scale, factor, exceed_period, is_default, enabled, remark, create_time, update_time) FROM stdin;
\.


--
-- TOC entry 3712 (class 0 OID 17308)
-- Dependencies: 226
-- Data for Name: schedules; Type: TABLE DATA; Schema: public; Owner: aps_user
--

COPY public.schedules (id, name, status, schedule_start_time, schedule_end_time, final_score, create_time, update_time) FROM stdin;
\.


--
-- TOC entry 3720 (class 0 OID 17473)
-- Dependencies: 234
-- Data for Name: sys_dict_item; Type: TABLE DATA; Schema: public; Owner: aps_user
--

COPY public.sys_dict_item (id, dict_type_id, item_code, item_name, item_value, description, enabled, sort_order, is_system, create_time, update_time) FROM stdin;
ccdd3010-8ec9-45d3-83d4-570cc8febbc6	b616d085-c385-4526-a613-5e8fa34399ef	SCHEDULED	已排产	SCHEDULED	订单已完成排产	t	2	t	2026-04-09 18:16:03.810221	2026-04-09 18:16:03.810221
1f87750b-bee1-4d73-b112-8febd7f1fa16	b616d085-c385-4526-a613-5e8fa34399ef	IN_PROGRESS	生产中	IN_PROGRESS	订单正在生产执行	t	3	t	2026-04-09 18:16:03.810221	2026-04-09 18:16:03.810221
14d6b622-b5de-448d-9786-a26ceb488611	b616d085-c385-4526-a613-5e8fa34399ef	COMPLETED	已完成	COMPLETED	订单生产已完成	t	4	t	2026-04-09 18:16:03.810221	2026-04-09 18:16:03.810221
2de40da3-ee1d-470e-adf4-84a7faf7ef27	b616d085-c385-4526-a613-5e8fa34399ef	CANCELLED	已取消	CANCELLED	订单已取消	t	5	t	2026-04-09 18:16:03.810221	2026-04-09 18:16:03.810221
ce113f95-9f96-4b07-9950-122855a08b93	bb584fac-10a8-4b1d-b02e-d8f28a0f8d13	URGENT	紧急	URGENT	最高优先级	t	1	t	2026-04-09 18:16:03.810221	2026-04-09 18:16:03.810221
2ce7ee03-6572-4067-833b-7044a5faf616	bb584fac-10a8-4b1d-b02e-d8f28a0f8d13	HIGH	高	HIGH	高优先级	t	2	t	2026-04-09 18:16:03.810221	2026-04-09 18:16:03.810221
c850acb1-a6e5-482e-9937-26c94bb113ba	bb584fac-10a8-4b1d-b02e-d8f28a0f8d13	NORMAL	中	NORMAL	常规优先级	t	3	t	2026-04-09 18:16:03.810221	2026-04-09 18:16:03.810221
2e579900-847b-444b-8308-7fd235ddb632	bb584fac-10a8-4b1d-b02e-d8f28a0f8d13	LOW	低	LOW	低优先级	t	4	t	2026-04-09 18:16:03.810221	2026-04-09 18:16:03.810221
c89c82c7-82d2-46db-8fd0-f9ed585f9725	b83ee719-41a0-4162-aef1-cd8e0c387313	RUNNING	运行中	RUNNING	设备正常运行	t	1	t	2026-04-09 18:16:03.810221	2026-04-09 18:16:03.810221
90d27963-5321-4e1c-94b4-e0a1d9d15267	b83ee719-41a0-4162-aef1-cd8e0c387313	IDLE	空闲	IDLE	设备空闲可排产	t	2	t	2026-04-09 18:16:03.810221	2026-04-09 18:16:03.810221
4eb4dba7-432f-4f79-a3c6-a16c2b62632a	b83ee719-41a0-4162-aef1-cd8e0c387313	MAINTENANCE	维护中	MAINTENANCE	设备维护停机	t	3	t	2026-04-09 18:16:03.810221	2026-04-09 18:16:03.810221
ccd7ff3d-765e-47b2-a3ad-5d042e6c9004	b83ee719-41a0-4162-aef1-cd8e0c387313	DISABLED	停用	DISABLED	设备不可用	t	4	t	2026-04-09 18:16:03.810221	2026-04-09 18:16:03.810221
3a37d061-c662-4a11-b260-772c11bd74b2	00a84cfe-ccfa-477d-8259-a9da3602497b	ADMIN	管理员	ADMIN	系统管理员，拥有全部访问权限	t	1	t	2026-04-09 19:07:56.802004	2026-04-09 19:07:56.802004
1706aec0-ae6d-43a7-9fd9-a67f6d7941ed	00a84cfe-ccfa-477d-8259-a9da3602497b	PLANNER	计划员	PLANNER	负责创建和修改排产计划	t	2	t	2026-04-09 19:07:56.802004	2026-04-09 19:07:56.802004
8d7e7723-a624-4773-aed4-799b07ad99aa	00a84cfe-ccfa-477d-8259-a9da3602497b	SUPERVISOR	监督员	SUPERVISOR	负责查看和监控排产执行情况	t	3	t	2026-04-09 19:07:56.802004	2026-04-09 19:07:56.802004
3d89bc32-928c-4a50-b82c-3b90cdf3c386	d6d776da-a504-4bdb-a343-32c340c00aa0	WORKDAY	工作日	WORKDAY	正常生产排班工作日	t	1	t	2026-04-09 19:07:56.802004	2026-04-09 19:07:56.802004
215b6dca-bb20-4d31-b9c3-4bf546b14fcc	d6d776da-a504-4bdb-a343-32c340c00aa0	RESTDAY	休息日	RESTDAY	周末或调休休息日	t	2	t	2026-04-09 19:07:56.802004	2026-04-09 19:07:56.802004
c6ef8622-26db-4cb6-8339-95b19a1c3329	d6d776da-a504-4bdb-a343-32c340c00aa0	HOLIDAY	节假日	HOLIDAY	法定节假日，不安排生产	t	3	t	2026-04-09 19:07:56.802004	2026-04-09 19:07:56.802004
9cb13b52-0175-4a93-9fda-67c98b3da6cd	375fc911-03a9-41d4-b656-352cebc380f5	LOGIN	登录	LOGIN	用户登录系统	t	1	t	2026-04-09 19:07:56.802004	2026-04-09 19:07:56.802004
f06aae51-32f1-4de2-b0ba-cd5477303235	375fc911-03a9-41d4-b656-352cebc380f5	LOGOUT	登出	LOGOUT	用户退出系统	t	2	t	2026-04-09 19:07:56.802004	2026-04-09 19:07:56.802004
b9857b9f-44b4-4ebe-b356-abf4df5c9173	375fc911-03a9-41d4-b656-352cebc380f5	ACCESS_DENIED	访问拒绝	ACCESS_DENIED	用户访问被拒绝	t	3	t	2026-04-09 19:07:56.802004	2026-04-09 19:07:56.802004
aaf618eb-d55d-43b6-8f32-4cbf8d88129f	375fc911-03a9-41d4-b656-352cebc380f5	SCHEDULE_CREATE	创建排产	SCHEDULE_CREATE	创建排产方案	t	10	t	2026-04-09 19:07:56.802004	2026-04-09 19:07:56.802004
4f47933b-6f2c-4dc6-a761-6243e1b065e4	375fc911-03a9-41d4-b656-352cebc380f5	SCHEDULE_UPDATE	修改排产	SCHEDULE_UPDATE	修改排产方案	t	11	t	2026-04-09 19:07:56.802004	2026-04-09 19:07:56.802004
36b9b2e6-b60b-42dc-b15b-8d80cd91f9a3	375fc911-03a9-41d4-b656-352cebc380f5	SCHEDULE_DELETE	删除排产	SCHEDULE_DELETE	删除排产方案	t	12	t	2026-04-09 19:07:56.802004	2026-04-09 19:07:56.802004
8a746331-b420-41c0-9d65-5b4760e8514e	375fc911-03a9-41d4-b656-352cebc380f5	SCHEDULE_SOLVE	启动求解	SCHEDULE_SOLVE	启动排产求解引擎	t	13	t	2026-04-09 19:07:56.802004	2026-04-09 19:07:56.802004
40b3a5a8-c6d5-45ad-ad82-3c10eccb4090	375fc911-03a9-41d4-b656-352cebc380f5	SCHEDULE_STOP	停止求解	SCHEDULE_STOP	停止排产求解引擎	t	14	t	2026-04-09 19:07:56.802004	2026-04-09 19:07:56.802004
e77efe5a-9d8b-4faa-9851-e570f011ce53	375fc911-03a9-41d4-b656-352cebc380f5	SCHEDULE_PUBLISH	发布排产	SCHEDULE_PUBLISH	发布排产方案	t	15	t	2026-04-09 19:07:56.802004	2026-04-09 19:07:56.802004
058ec789-409e-495a-8705-f505c663f021	375fc911-03a9-41d4-b656-352cebc380f5	ORDER_CREATE	创建工单	ORDER_CREATE	创建生产工单	t	20	t	2026-04-09 19:07:56.802004	2026-04-09 19:07:56.802004
fa71e504-1ccf-4bcb-a830-26192e892bdc	375fc911-03a9-41d4-b656-352cebc380f5	ORDER_UPDATE	修改工单	ORDER_UPDATE	修改生产工单	t	21	t	2026-04-09 19:07:56.802004	2026-04-09 19:07:56.802004
c4f3de0d-aa65-4742-bffb-59110beef12f	375fc911-03a9-41d4-b656-352cebc380f5	ORDER_DELETE	删除工单	ORDER_DELETE	删除生产工单	t	22	t	2026-04-09 19:07:56.802004	2026-04-09 19:07:56.802004
3c14ff8d-843c-43c5-bd23-fce89bbee308	375fc911-03a9-41d4-b656-352cebc380f5	ORDER_IMPORT	导入工单	ORDER_IMPORT	批量导入生产工单	t	23	t	2026-04-09 19:07:56.802004	2026-04-09 19:07:56.802004
8365b75e-533c-4733-baa8-a72fb9984a70	375fc911-03a9-41d4-b656-352cebc380f5	RESOURCE_CREATE	创建资源	RESOURCE_CREATE	创建生产资源	t	30	t	2026-04-09 19:07:56.802004	2026-04-09 19:07:56.802004
3911c339-15ab-4703-815c-fb8a9b4dee5f	375fc911-03a9-41d4-b656-352cebc380f5	RESOURCE_UPDATE	修改资源	RESOURCE_UPDATE	修改生产资源	t	31	t	2026-04-09 19:07:56.802004	2026-04-09 19:07:56.802004
f37b8876-9da2-4893-a4e1-c337e8f17a69	375fc911-03a9-41d4-b656-352cebc380f5	RESOURCE_DELETE	删除资源	RESOURCE_DELETE	删除生产资源	t	32	t	2026-04-09 19:07:56.802004	2026-04-09 19:07:56.802004
94e6dafc-d301-4db5-9502-710673fddae8	375fc911-03a9-41d4-b656-352cebc380f5	USER_CREATE	创建用户	USER_CREATE	创建系统用户	t	40	t	2026-04-09 19:07:56.802004	2026-04-09 19:07:56.802004
5501279e-e963-426f-b765-5d2d2ab3e470	375fc911-03a9-41d4-b656-352cebc380f5	USER_UPDATE	修改用户	USER_UPDATE	修改系统用户信息	t	41	t	2026-04-09 19:07:56.802004	2026-04-09 19:07:56.802004
34bef5fe-8ed5-4f42-80b0-2b1949655b0f	375fc911-03a9-41d4-b656-352cebc380f5	USER_DELETE	删除用户	USER_DELETE	删除系统用户	t	42	t	2026-04-09 19:07:56.802004	2026-04-09 19:07:56.802004
9470dd6f-1ad9-4a5c-8e47-e8026ebd740c	375fc911-03a9-41d4-b656-352cebc380f5	USER_DISABLE	禁用用户	USER_DISABLE	禁用系统用户账号	t	43	t	2026-04-09 19:07:56.802004	2026-04-09 19:07:56.802004
2e804c62-e402-4916-904b-c98ef4901017	375fc911-03a9-41d4-b656-352cebc380f5	USER_ENABLE	启用用户	USER_ENABLE	启用系统用户账号	t	44	t	2026-04-09 19:07:56.802004	2026-04-09 19:07:56.802004
d77105bf-0bd7-4823-9797-a64d62987a63	375fc911-03a9-41d4-b656-352cebc380f5	ROLE_ASSIGN	分配角色	ROLE_ASSIGN	为用户分配角色	t	45	t	2026-04-09 19:07:56.802004	2026-04-09 19:07:56.802004
298cb2e1-c14a-41a3-9b70-8ea57488bcb8	375fc911-03a9-41d4-b656-352cebc380f5	ROLE_REMOVE	移除角色	ROLE_REMOVE	移除用户的角色	t	46	t	2026-04-09 19:07:56.802004	2026-04-09 19:07:56.802004
758333e3-67de-454b-acdc-1905511485d9	375fc911-03a9-41d4-b656-352cebc380f5	PERMISSION_GRANT	授予权限	PERMISSION_GRANT	为角色授予权限	t	47	t	2026-04-09 19:07:56.802004	2026-04-09 19:07:56.802004
b2f6416d-122e-4ca6-9c4b-7b9f49538f39	375fc911-03a9-41d4-b656-352cebc380f5	PERMISSION_REVOKE	撤销权限	PERMISSION_REVOKE	撤销角色的权限	t	48	t	2026-04-09 19:07:56.802004	2026-04-09 19:07:56.802004
2abf99bb-a000-4ec5-ab59-dcb4820cea46	375fc911-03a9-41d4-b656-352cebc380f5	CONFIG_UPDATE	修改配置	CONFIG_UPDATE	修改系统配置	t	50	t	2026-04-09 19:07:56.802004	2026-04-09 19:07:56.802004
f28ee1c9-1964-4435-897a-407f5a74ee91	375fc911-03a9-41d4-b656-352cebc380f5	CREATE	创建	CREATE	通用创建操作	t	90	t	2026-04-09 19:07:56.802004	2026-04-09 19:07:56.802004
7a596a90-7924-4ba4-837f-89919f562eab	375fc911-03a9-41d4-b656-352cebc380f5	UPDATE	更新	UPDATE	通用更新操作	t	91	t	2026-04-09 19:07:56.802004	2026-04-09 19:07:56.802004
4c897fd9-fd5c-42ea-a6c7-2546ad809900	375fc911-03a9-41d4-b656-352cebc380f5	DELETE	删除	DELETE	通用删除操作	t	92	t	2026-04-09 19:07:56.802004	2026-04-09 19:07:56.802004
449c4a0c-7563-4f9c-9c94-a62a2cac66ca	6c3414cc-8916-4c33-8a71-f63da94326e7	CATALOG	目录	CATALOG	导航目录节点，用于分组菜单	t	1	t	2026-04-09 19:07:56.870816	2026-04-09 19:07:56.870816
ad06976d-b43e-44d5-8d61-da31f6bef180	6c3414cc-8916-4c33-8a71-f63da94326e7	MENU	菜单	MENU	页面菜单节点，对应前端路由	t	2	t	2026-04-09 19:07:56.870816	2026-04-09 19:07:56.870816
1489af3c-8574-4ffc-8978-ae1549586852	6c3414cc-8916-4c33-8a71-f63da94326e7	BUTTON	按钮	BUTTON	操作按钮节点，控制功能级权限	t	3	t	2026-04-09 19:07:56.870816	2026-04-09 19:07:56.870816
ea4b0469-b8cb-43a0-95e2-cf09beffa6f8	b616d085-c385-4526-a613-5e8fa34399ef	PENDING	待排产	PENDING	订单已创建，尚未排产	t	1	t	2026-04-09 18:16:03.810221	2026-04-09 20:15:55.457503
1a158320-5e1e-4842-92b6-71239210139f	dac94487-9571-4d80-8c0c-f85aa6853962	01	开始	01	\N	t	0	f	2026-04-09 20:17:30.62946	2026-04-09 20:18:02.88661
\.


--
-- TOC entry 3719 (class 0 OID 17461)
-- Dependencies: 233
-- Data for Name: sys_dict_type; Type: TABLE DATA; Schema: public; Owner: aps_user
--

COPY public.sys_dict_type (id, code, name, description, enabled, sort_order, create_time, update_time) FROM stdin;
b616d085-c385-4526-a613-5e8fa34399ef	ORDER_STATUS	订单状态	订单处理状态编码	t	1	2026-04-09 18:16:03.810221	2026-04-09 18:16:03.810221
bb584fac-10a8-4b1d-b02e-d8f28a0f8d13	PRIORITY	优先级	生产优先级编码	t	2	2026-04-09 18:16:03.810221	2026-04-09 18:16:03.810221
b83ee719-41a0-4162-aef1-cd8e0c387313	MACHINE_STATUS	设备状态	设备运行状态编码	t	3	2026-04-09 18:16:03.810221	2026-04-09 18:16:03.810221
00a84cfe-ccfa-477d-8259-a9da3602497b	ROLE_TYPE	角色类型	系统角色分类编码	t	4	2026-04-09 19:07:56.802004	2026-04-09 19:07:56.802004
d6d776da-a504-4bdb-a343-32c340c00aa0	DATE_TYPE	日期类型	工厂日历日期分类编码	t	5	2026-04-09 19:07:56.802004	2026-04-09 19:07:56.802004
375fc911-03a9-41d4-b656-352cebc380f5	AUDIT_ACTION	审计操作类型	审计日志操作行为编码	t	6	2026-04-09 19:07:56.802004	2026-04-09 19:07:56.802004
6c3414cc-8916-4c33-8a71-f63da94326e7	PERMISSION_TYPE	权限类型	权限节点类型编码（目录/菜单/按钮）	t	7	2026-04-09 19:07:56.870816	2026-04-09 19:07:56.870816
dac94487-9571-4d80-8c0c-f85aa6853962	CUSTYPE	自定义	\N	t	7	2026-04-09 20:16:42.238204	2026-04-09 20:17:08.578328
\.


--
-- TOC entry 3706 (class 0 OID 17223)
-- Dependencies: 220
-- Data for Name: user_roles; Type: TABLE DATA; Schema: public; Owner: aps_user
--

COPY public.user_roles (user_id, role_id) FROM stdin;
cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	5f685927-09b9-4f5b-a20e-8561748e7f99
\.


--
-- TOC entry 3703 (class 0 OID 17190)
-- Dependencies: 217
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: aps_user
--

COPY public.users (id, username, password_hash, email, enabled, create_time, update_time, last_login_at) FROM stdin;
cad00da0-ef20-4b60-9b26-7ac4ecb9aa09	admin	$2a$10$JNdH7npwqbwn9y0VFbyv.O1BpjSgA.nmIQUrT/TUFQOFzVf90/1U6	admin@aps.com	t	2026-04-07 16:56:20.775506	2026-04-11 07:44:58.690591	2026-04-11 07:44:58.682064
\.


--
-- TOC entry 3718 (class 0 OID 17425)
-- Dependencies: 232
-- Data for Name: workshops; Type: TABLE DATA; Schema: public; Owner: aps_user
--

COPY public.workshops (id, code, name, calendar_id, manager_name, enabled, sort_order, description, create_time, update_time) FROM stdin;
0ec60c75-a830-4651-ab47-71f7d2b42ad7	ZSCJ	注塑车间	8648aa7c-5574-479f-9084-a9ca960b6855	张三	t	1		2026-04-09 14:58:50.20028	2026-04-10 08:34:23.495143
1c56f6a5-575e-4534-9e57-28444fb62f28	ZSCJ2	注塑车间2	a73e562e-fc18-4ac3-b966-d67799e3ae80		t	0		2026-04-09 15:22:25.238938	2026-04-10 08:34:35.578225
\.


--
-- TOC entry 3497 (class 2606 OID 17339)
-- Name: assignments assignments_pkey; Type: CONSTRAINT; Schema: public; Owner: aps_user
--

ALTER TABLE ONLY public.assignments
    ADD CONSTRAINT assignments_pkey PRIMARY KEY (id);


--
-- TOC entry 3472 (class 2606 OID 17263)
-- Name: audit_logs audit_logs_pkey; Type: CONSTRAINT; Schema: public; Owner: aps_user
--

ALTER TABLE ONLY public.audit_logs
    ADD CONSTRAINT audit_logs_pkey PRIMARY KEY (id);


--
-- TOC entry 3509 (class 2606 OID 17412)
-- Name: calendar_dates calendar_dates_pkey; Type: CONSTRAINT; Schema: public; Owner: aps_user
--

ALTER TABLE ONLY public.calendar_dates
    ADD CONSTRAINT calendar_dates_pkey PRIMARY KEY (id);


--
-- TOC entry 3506 (class 2606 OID 17398)
-- Name: calendar_shifts calendar_shifts_pkey; Type: CONSTRAINT; Schema: public; Owner: aps_user
--

ALTER TABLE ONLY public.calendar_shifts
    ADD CONSTRAINT calendar_shifts_pkey PRIMARY KEY (id);


--
-- TOC entry 3501 (class 2606 OID 17389)
-- Name: factory_calendars factory_calendars_code_key; Type: CONSTRAINT; Schema: public; Owner: aps_user
--

ALTER TABLE ONLY public.factory_calendars
    ADD CONSTRAINT factory_calendars_code_key UNIQUE (code);


--
-- TOC entry 3503 (class 2606 OID 17387)
-- Name: factory_calendars factory_calendars_pkey; Type: CONSTRAINT; Schema: public; Owner: aps_user
--

ALTER TABLE ONLY public.factory_calendars
    ADD CONSTRAINT factory_calendars_pkey PRIMARY KEY (id);


--
-- TOC entry 3448 (class 2606 OID 17188)
-- Name: flyway_schema_history flyway_schema_history_pk; Type: CONSTRAINT; Schema: public; Owner: aps_user
--

ALTER TABLE ONLY public.flyway_schema_history
    ADD CONSTRAINT flyway_schema_history_pk PRIMARY KEY (installed_rank);


--
-- TOC entry 3491 (class 2606 OID 17297)
-- Name: operations operations_pkey; Type: CONSTRAINT; Schema: public; Owner: aps_user
--

ALTER TABLE ONLY public.operations
    ADD CONSTRAINT operations_pkey PRIMARY KEY (id);


--
-- TOC entry 3486 (class 2606 OID 17289)
-- Name: orders orders_order_no_key; Type: CONSTRAINT; Schema: public; Owner: aps_user
--

ALTER TABLE ONLY public.orders
    ADD CONSTRAINT orders_order_no_key UNIQUE (order_no);


--
-- TOC entry 3488 (class 2606 OID 17287)
-- Name: orders orders_pkey; Type: CONSTRAINT; Schema: public; Owner: aps_user
--

ALTER TABLE ONLY public.orders
    ADD CONSTRAINT orders_pkey PRIMARY KEY (id);


--
-- TOC entry 3464 (class 2606 OID 17222)
-- Name: permissions permissions_code_key; Type: CONSTRAINT; Schema: public; Owner: aps_user
--

ALTER TABLE ONLY public.permissions
    ADD CONSTRAINT permissions_code_key UNIQUE (code);


--
-- TOC entry 3466 (class 2606 OID 17220)
-- Name: permissions permissions_pkey; Type: CONSTRAINT; Schema: public; Owner: aps_user
--

ALTER TABLE ONLY public.permissions
    ADD CONSTRAINT permissions_pkey PRIMARY KEY (id);


--
-- TOC entry 3531 (class 2606 OID 17505)
-- Name: resource_capacity_days resource_capacity_days_pkey; Type: CONSTRAINT; Schema: public; Owner: aps_user
--

ALTER TABLE ONLY public.resource_capacity_days
    ADD CONSTRAINT resource_capacity_days_pkey PRIMARY KEY (id);


--
-- TOC entry 3479 (class 2606 OID 17277)
-- Name: resources resources_pkey; Type: CONSTRAINT; Schema: public; Owner: aps_user
--

ALTER TABLE ONLY public.resources
    ADD CONSTRAINT resources_pkey PRIMARY KEY (id);


--
-- TOC entry 3481 (class 2606 OID 17279)
-- Name: resources resources_resource_code_key; Type: CONSTRAINT; Schema: public; Owner: aps_user
--

ALTER TABLE ONLY public.resources
    ADD CONSTRAINT resources_resource_code_key UNIQUE (resource_code);


--
-- TOC entry 3470 (class 2606 OID 17242)
-- Name: role_permissions role_permissions_pkey; Type: CONSTRAINT; Schema: public; Owner: aps_user
--

ALTER TABLE ONLY public.role_permissions
    ADD CONSTRAINT role_permissions_pkey PRIMARY KEY (role_id, permission_id);


--
-- TOC entry 3457 (class 2606 OID 17210)
-- Name: roles roles_name_key; Type: CONSTRAINT; Schema: public; Owner: aps_user
--

ALTER TABLE ONLY public.roles
    ADD CONSTRAINT roles_name_key UNIQUE (name);


--
-- TOC entry 3459 (class 2606 OID 17208)
-- Name: roles roles_pkey; Type: CONSTRAINT; Schema: public; Owner: aps_user
--

ALTER TABLE ONLY public.roles
    ADD CONSTRAINT roles_pkey PRIMARY KEY (id);


--
-- TOC entry 3495 (class 2606 OID 17320)
-- Name: schedule_resources schedule_resources_pkey; Type: CONSTRAINT; Schema: public; Owner: aps_user
--

ALTER TABLE ONLY public.schedule_resources
    ADD CONSTRAINT schedule_resources_pkey PRIMARY KEY (schedule_id, resource_id);


--
-- TOC entry 3535 (class 2606 OID 17537)
-- Name: schedule_time_parameters schedule_time_parameters_pkey; Type: CONSTRAINT; Schema: public; Owner: aps_user
--

ALTER TABLE ONLY public.schedule_time_parameters
    ADD CONSTRAINT schedule_time_parameters_pkey PRIMARY KEY (id);


--
-- TOC entry 3537 (class 2606 OID 17539)
-- Name: schedule_time_parameters schedule_time_parameters_resource_id_key; Type: CONSTRAINT; Schema: public; Owner: aps_user
--

ALTER TABLE ONLY public.schedule_time_parameters
    ADD CONSTRAINT schedule_time_parameters_resource_id_key UNIQUE (resource_id);


--
-- TOC entry 3493 (class 2606 OID 17315)
-- Name: schedules schedules_pkey; Type: CONSTRAINT; Schema: public; Owner: aps_user
--

ALTER TABLE ONLY public.schedules
    ADD CONSTRAINT schedules_pkey PRIMARY KEY (id);


--
-- TOC entry 3527 (class 2606 OID 17485)
-- Name: sys_dict_item sys_dict_item_pkey; Type: CONSTRAINT; Schema: public; Owner: aps_user
--

ALTER TABLE ONLY public.sys_dict_item
    ADD CONSTRAINT sys_dict_item_pkey PRIMARY KEY (id);


--
-- TOC entry 3523 (class 2606 OID 17472)
-- Name: sys_dict_type sys_dict_type_pkey; Type: CONSTRAINT; Schema: public; Owner: aps_user
--

ALTER TABLE ONLY public.sys_dict_type
    ADD CONSTRAINT sys_dict_type_pkey PRIMARY KEY (id);


--
-- TOC entry 3533 (class 2606 OID 17507)
-- Name: resource_capacity_days uk_resource_capacity_days_resource_date; Type: CONSTRAINT; Schema: public; Owner: aps_user
--

ALTER TABLE ONLY public.resource_capacity_days
    ADD CONSTRAINT uk_resource_capacity_days_resource_date UNIQUE (resource_id, capacity_date);


--
-- TOC entry 3513 (class 2606 OID 17414)
-- Name: calendar_dates uq_calendar_date; Type: CONSTRAINT; Schema: public; Owner: aps_user
--

ALTER TABLE ONLY public.calendar_dates
    ADD CONSTRAINT uq_calendar_date UNIQUE (calendar_id, date);


--
-- TOC entry 3468 (class 2606 OID 17227)
-- Name: user_roles user_roles_pkey; Type: CONSTRAINT; Schema: public; Owner: aps_user
--

ALTER TABLE ONLY public.user_roles
    ADD CONSTRAINT user_roles_pkey PRIMARY KEY (user_id, role_id);


--
-- TOC entry 3453 (class 2606 OID 17198)
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: aps_user
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- TOC entry 3455 (class 2606 OID 17200)
-- Name: users users_username_key; Type: CONSTRAINT; Schema: public; Owner: aps_user
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_username_key UNIQUE (username);


--
-- TOC entry 3518 (class 2606 OID 17438)
-- Name: workshops workshops_code_key; Type: CONSTRAINT; Schema: public; Owner: aps_user
--

ALTER TABLE ONLY public.workshops
    ADD CONSTRAINT workshops_code_key UNIQUE (code);


--
-- TOC entry 3520 (class 2606 OID 17436)
-- Name: workshops workshops_pkey; Type: CONSTRAINT; Schema: public; Owner: aps_user
--

ALTER TABLE ONLY public.workshops
    ADD CONSTRAINT workshops_pkey PRIMARY KEY (id);


--
-- TOC entry 3449 (class 1259 OID 17189)
-- Name: flyway_schema_history_s_idx; Type: INDEX; Schema: public; Owner: aps_user
--

CREATE INDEX flyway_schema_history_s_idx ON public.flyway_schema_history USING btree (success);


--
-- TOC entry 3498 (class 1259 OID 17364)
-- Name: idx_assignments_operation_id; Type: INDEX; Schema: public; Owner: aps_user
--

CREATE INDEX idx_assignments_operation_id ON public.assignments USING btree (operation_id);


--
-- TOC entry 3499 (class 1259 OID 17363)
-- Name: idx_assignments_schedule_id; Type: INDEX; Schema: public; Owner: aps_user
--

CREATE INDEX idx_assignments_schedule_id ON public.assignments USING btree (schedule_id);


--
-- TOC entry 3473 (class 1259 OID 17358)
-- Name: idx_audit_logs_timestamp; Type: INDEX; Schema: public; Owner: aps_user
--

CREATE INDEX idx_audit_logs_timestamp ON public.audit_logs USING btree ("timestamp");


--
-- TOC entry 3474 (class 1259 OID 17357)
-- Name: idx_audit_logs_user_id; Type: INDEX; Schema: public; Owner: aps_user
--

CREATE INDEX idx_audit_logs_user_id ON public.audit_logs USING btree (user_id);


--
-- TOC entry 3510 (class 1259 OID 17421)
-- Name: idx_calendar_dates_calendar_id; Type: INDEX; Schema: public; Owner: aps_user
--

CREATE INDEX idx_calendar_dates_calendar_id ON public.calendar_dates USING btree (calendar_id);


--
-- TOC entry 3511 (class 1259 OID 17422)
-- Name: idx_calendar_dates_date; Type: INDEX; Schema: public; Owner: aps_user
--

CREATE INDEX idx_calendar_dates_date ON public.calendar_dates USING btree (date);


--
-- TOC entry 3507 (class 1259 OID 17420)
-- Name: idx_calendar_shifts_calendar_id; Type: INDEX; Schema: public; Owner: aps_user
--

CREATE INDEX idx_calendar_shifts_calendar_id ON public.calendar_shifts USING btree (calendar_id);


--
-- TOC entry 3504 (class 1259 OID 17423)
-- Name: idx_factory_calendars_year; Type: INDEX; Schema: public; Owner: aps_user
--

CREATE INDEX idx_factory_calendars_year ON public.factory_calendars USING btree (year);


--
-- TOC entry 3489 (class 1259 OID 17362)
-- Name: idx_operations_order_id; Type: INDEX; Schema: public; Owner: aps_user
--

CREATE INDEX idx_operations_order_id ON public.operations USING btree (order_id);


--
-- TOC entry 3482 (class 1259 OID 17361)
-- Name: idx_orders_due_date; Type: INDEX; Schema: public; Owner: aps_user
--

CREATE INDEX idx_orders_due_date ON public.orders USING btree (due_date);


--
-- TOC entry 3483 (class 1259 OID 17359)
-- Name: idx_orders_order_no; Type: INDEX; Schema: public; Owner: aps_user
--

CREATE INDEX idx_orders_order_no ON public.orders USING btree (order_no);


--
-- TOC entry 3484 (class 1259 OID 17360)
-- Name: idx_orders_status; Type: INDEX; Schema: public; Owner: aps_user
--

CREATE INDEX idx_orders_status ON public.orders USING btree (status);


--
-- TOC entry 3460 (class 1259 OID 17377)
-- Name: idx_permission_enabled; Type: INDEX; Schema: public; Owner: aps_user
--

CREATE INDEX idx_permission_enabled ON public.permissions USING btree (enabled);


--
-- TOC entry 3461 (class 1259 OID 17375)
-- Name: idx_permission_parent_id; Type: INDEX; Schema: public; Owner: aps_user
--

CREATE INDEX idx_permission_parent_id ON public.permissions USING btree (parent_id);


--
-- TOC entry 3462 (class 1259 OID 17376)
-- Name: idx_permission_sort; Type: INDEX; Schema: public; Owner: aps_user
--

CREATE INDEX idx_permission_sort ON public.permissions USING btree (sort);


--
-- TOC entry 3529 (class 1259 OID 17513)
-- Name: idx_resource_capacity_days_resource_date; Type: INDEX; Schema: public; Owner: aps_user
--

CREATE INDEX idx_resource_capacity_days_resource_date ON public.resource_capacity_days USING btree (resource_id, capacity_date);


--
-- TOC entry 3475 (class 1259 OID 17459)
-- Name: idx_resources_calendar_id; Type: INDEX; Schema: public; Owner: aps_user
--

CREATE INDEX idx_resources_calendar_id ON public.resources USING btree (calendar_id);


--
-- TOC entry 3476 (class 1259 OID 17458)
-- Name: idx_resources_status; Type: INDEX; Schema: public; Owner: aps_user
--

CREATE INDEX idx_resources_status ON public.resources USING btree (status);


--
-- TOC entry 3477 (class 1259 OID 17457)
-- Name: idx_resources_workshop_id; Type: INDEX; Schema: public; Owner: aps_user
--

CREATE INDEX idx_resources_workshop_id ON public.resources USING btree (workshop_id);


--
-- TOC entry 3525 (class 1259 OID 17494)
-- Name: idx_sys_dict_item_type_enabled_sort; Type: INDEX; Schema: public; Owner: aps_user
--

CREATE INDEX idx_sys_dict_item_type_enabled_sort ON public.sys_dict_item USING btree (dict_type_id, enabled, sort_order);


--
-- TOC entry 3521 (class 1259 OID 17493)
-- Name: idx_sys_dict_type_enabled_sort; Type: INDEX; Schema: public; Owner: aps_user
--

CREATE INDEX idx_sys_dict_type_enabled_sort ON public.sys_dict_type USING btree (enabled, sort_order);


--
-- TOC entry 3450 (class 1259 OID 17356)
-- Name: idx_users_email; Type: INDEX; Schema: public; Owner: aps_user
--

CREATE INDEX idx_users_email ON public.users USING btree (email);


--
-- TOC entry 3451 (class 1259 OID 17355)
-- Name: idx_users_username; Type: INDEX; Schema: public; Owner: aps_user
--

CREATE INDEX idx_users_username ON public.users USING btree (username);


--
-- TOC entry 3514 (class 1259 OID 17455)
-- Name: idx_workshops_calendar_id; Type: INDEX; Schema: public; Owner: aps_user
--

CREATE INDEX idx_workshops_calendar_id ON public.workshops USING btree (calendar_id);


--
-- TOC entry 3515 (class 1259 OID 17456)
-- Name: idx_workshops_enabled; Type: INDEX; Schema: public; Owner: aps_user
--

CREATE INDEX idx_workshops_enabled ON public.workshops USING btree (enabled);


--
-- TOC entry 3528 (class 1259 OID 17492)
-- Name: uk_sys_dict_item_type_code; Type: INDEX; Schema: public; Owner: aps_user
--

CREATE UNIQUE INDEX uk_sys_dict_item_type_code ON public.sys_dict_item USING btree (dict_type_id, item_code);


--
-- TOC entry 3524 (class 1259 OID 17491)
-- Name: uk_sys_dict_type_code; Type: INDEX; Schema: public; Owner: aps_user
--

CREATE UNIQUE INDEX uk_sys_dict_type_code ON public.sys_dict_type USING btree (code);


--
-- TOC entry 3516 (class 1259 OID 17454)
-- Name: uk_workshops_code; Type: INDEX; Schema: public; Owner: aps_user
--

CREATE UNIQUE INDEX uk_workshops_code ON public.workshops USING btree (code);


--
-- TOC entry 3550 (class 2606 OID 17350)
-- Name: assignments assignments_assigned_resource_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: aps_user
--

ALTER TABLE ONLY public.assignments
    ADD CONSTRAINT assignments_assigned_resource_id_fkey FOREIGN KEY (assigned_resource_id) REFERENCES public.resources(id);


--
-- TOC entry 3551 (class 2606 OID 17345)
-- Name: assignments assignments_operation_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: aps_user
--

ALTER TABLE ONLY public.assignments
    ADD CONSTRAINT assignments_operation_id_fkey FOREIGN KEY (operation_id) REFERENCES public.operations(id);


--
-- TOC entry 3552 (class 2606 OID 17340)
-- Name: assignments assignments_schedule_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: aps_user
--

ALTER TABLE ONLY public.assignments
    ADD CONSTRAINT assignments_schedule_id_fkey FOREIGN KEY (schedule_id) REFERENCES public.schedules(id) ON DELETE CASCADE;


--
-- TOC entry 3543 (class 2606 OID 17264)
-- Name: audit_logs audit_logs_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: aps_user
--

ALTER TABLE ONLY public.audit_logs
    ADD CONSTRAINT audit_logs_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE SET NULL;


--
-- TOC entry 3554 (class 2606 OID 17415)
-- Name: calendar_dates calendar_dates_calendar_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: aps_user
--

ALTER TABLE ONLY public.calendar_dates
    ADD CONSTRAINT calendar_dates_calendar_id_fkey FOREIGN KEY (calendar_id) REFERENCES public.factory_calendars(id) ON DELETE CASCADE;


--
-- TOC entry 3553 (class 2606 OID 17399)
-- Name: calendar_shifts calendar_shifts_calendar_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: aps_user
--

ALTER TABLE ONLY public.calendar_shifts
    ADD CONSTRAINT calendar_shifts_calendar_id_fkey FOREIGN KEY (calendar_id) REFERENCES public.factory_calendars(id) ON DELETE CASCADE;


--
-- TOC entry 3538 (class 2606 OID 17370)
-- Name: permissions fk_permission_parent; Type: FK CONSTRAINT; Schema: public; Owner: aps_user
--

ALTER TABLE ONLY public.permissions
    ADD CONSTRAINT fk_permission_parent FOREIGN KEY (parent_id) REFERENCES public.permissions(id) ON DELETE CASCADE;


--
-- TOC entry 3546 (class 2606 OID 17298)
-- Name: operations operations_order_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: aps_user
--

ALTER TABLE ONLY public.operations
    ADD CONSTRAINT operations_order_id_fkey FOREIGN KEY (order_id) REFERENCES public.orders(id) ON DELETE CASCADE;


--
-- TOC entry 3547 (class 2606 OID 17303)
-- Name: operations operations_required_resource_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: aps_user
--

ALTER TABLE ONLY public.operations
    ADD CONSTRAINT operations_required_resource_id_fkey FOREIGN KEY (required_resource_id) REFERENCES public.resources(id);


--
-- TOC entry 3557 (class 2606 OID 17508)
-- Name: resource_capacity_days resource_capacity_days_resource_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: aps_user
--

ALTER TABLE ONLY public.resource_capacity_days
    ADD CONSTRAINT resource_capacity_days_resource_id_fkey FOREIGN KEY (resource_id) REFERENCES public.resources(id) ON DELETE CASCADE;


--
-- TOC entry 3544 (class 2606 OID 17449)
-- Name: resources resources_calendar_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: aps_user
--

ALTER TABLE ONLY public.resources
    ADD CONSTRAINT resources_calendar_id_fkey FOREIGN KEY (calendar_id) REFERENCES public.factory_calendars(id) ON DELETE SET NULL;


--
-- TOC entry 3545 (class 2606 OID 17444)
-- Name: resources resources_workshop_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: aps_user
--

ALTER TABLE ONLY public.resources
    ADD CONSTRAINT resources_workshop_id_fkey FOREIGN KEY (workshop_id) REFERENCES public.workshops(id) ON DELETE SET NULL;


--
-- TOC entry 3541 (class 2606 OID 17248)
-- Name: role_permissions role_permissions_permission_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: aps_user
--

ALTER TABLE ONLY public.role_permissions
    ADD CONSTRAINT role_permissions_permission_id_fkey FOREIGN KEY (permission_id) REFERENCES public.permissions(id) ON DELETE CASCADE;


--
-- TOC entry 3542 (class 2606 OID 17243)
-- Name: role_permissions role_permissions_role_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: aps_user
--

ALTER TABLE ONLY public.role_permissions
    ADD CONSTRAINT role_permissions_role_id_fkey FOREIGN KEY (role_id) REFERENCES public.roles(id) ON DELETE CASCADE;


--
-- TOC entry 3548 (class 2606 OID 17326)
-- Name: schedule_resources schedule_resources_resource_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: aps_user
--

ALTER TABLE ONLY public.schedule_resources
    ADD CONSTRAINT schedule_resources_resource_id_fkey FOREIGN KEY (resource_id) REFERENCES public.resources(id) ON DELETE CASCADE;


--
-- TOC entry 3549 (class 2606 OID 17321)
-- Name: schedule_resources schedule_resources_schedule_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: aps_user
--

ALTER TABLE ONLY public.schedule_resources
    ADD CONSTRAINT schedule_resources_schedule_id_fkey FOREIGN KEY (schedule_id) REFERENCES public.schedules(id) ON DELETE CASCADE;


--
-- TOC entry 3558 (class 2606 OID 17540)
-- Name: schedule_time_parameters schedule_time_parameters_resource_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: aps_user
--

ALTER TABLE ONLY public.schedule_time_parameters
    ADD CONSTRAINT schedule_time_parameters_resource_id_fkey FOREIGN KEY (resource_id) REFERENCES public.resources(id) ON DELETE CASCADE;


--
-- TOC entry 3556 (class 2606 OID 17486)
-- Name: sys_dict_item sys_dict_item_dict_type_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: aps_user
--

ALTER TABLE ONLY public.sys_dict_item
    ADD CONSTRAINT sys_dict_item_dict_type_id_fkey FOREIGN KEY (dict_type_id) REFERENCES public.sys_dict_type(id) ON DELETE RESTRICT;


--
-- TOC entry 3539 (class 2606 OID 17233)
-- Name: user_roles user_roles_role_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: aps_user
--

ALTER TABLE ONLY public.user_roles
    ADD CONSTRAINT user_roles_role_id_fkey FOREIGN KEY (role_id) REFERENCES public.roles(id) ON DELETE CASCADE;


--
-- TOC entry 3540 (class 2606 OID 17228)
-- Name: user_roles user_roles_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: aps_user
--

ALTER TABLE ONLY public.user_roles
    ADD CONSTRAINT user_roles_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.users(id) ON DELETE CASCADE;


--
-- TOC entry 3555 (class 2606 OID 17439)
-- Name: workshops workshops_calendar_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: aps_user
--

ALTER TABLE ONLY public.workshops
    ADD CONSTRAINT workshops_calendar_id_fkey FOREIGN KEY (calendar_id) REFERENCES public.factory_calendars(id) ON DELETE SET NULL;


-- Completed on 2026-04-11 08:18:49

--
-- PostgreSQL database dump complete
--

\unrestrict 5FD6Tn0s4KM02Om9biT9qt3ztVtN7heOzaI2ESbxbwC5nEgHKRoOuB7PSyu0TJb

