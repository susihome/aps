-- 更新 permissions 表结构以支持完整的权限管理功能

-- 添加新字段
ALTER TABLE permissions ADD COLUMN IF NOT EXISTS name VARCHAR(100) NOT NULL DEFAULT '';
ALTER TABLE permissions ADD COLUMN IF NOT EXISTS type VARCHAR(20) NOT NULL DEFAULT 'BUTTON';
ALTER TABLE permissions ADD COLUMN IF NOT EXISTS route_path VARCHAR(255);
ALTER TABLE permissions ADD COLUMN IF NOT EXISTS icon VARCHAR(50);
ALTER TABLE permissions ADD COLUMN IF NOT EXISTS sort INTEGER NOT NULL DEFAULT 0;
ALTER TABLE permissions ADD COLUMN IF NOT EXISTS enabled BOOLEAN NOT NULL DEFAULT true;
ALTER TABLE permissions ADD COLUMN IF NOT EXISTS visible BOOLEAN NOT NULL DEFAULT true;
ALTER TABLE permissions ADD COLUMN IF NOT EXISTS parent_id UUID;

-- 添加外键约束
ALTER TABLE permissions ADD CONSTRAINT fk_permission_parent
    FOREIGN KEY (parent_id) REFERENCES permissions(id) ON DELETE CASCADE;

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_permission_parent_id ON permissions(parent_id);
CREATE INDEX IF NOT EXISTS idx_permission_sort ON permissions(sort);
CREATE INDEX IF NOT EXISTS idx_permission_enabled ON permissions(enabled);

-- 删除旧字段（如果存在）
ALTER TABLE permissions DROP COLUMN IF EXISTS resource;
ALTER TABLE permissions DROP COLUMN IF EXISTS action;

-- 插入示例权限数据
INSERT INTO permissions (id, code, name, description, type, icon, sort, enabled, visible, parent_id, create_time, update_time)
VALUES
    -- 系统管理（目录）
    (uuid_generate_v4(), 'system', '系统管理', '系统管理模块', 'CATALOG', 'Setting', 1, true, true, NULL, NOW(), NOW()),

    -- 用户管理（菜单）
    (uuid_generate_v4(), 'system:user', '用户管理', '用户管理页面', 'MENU', 'User', 1, true, true,
        (SELECT id FROM permissions WHERE code = 'system'), NOW(), NOW()),

    -- 用户管理按钮
    (uuid_generate_v4(), 'system:user:list', '用户列表', '查看用户列表', 'BUTTON', 'View', 1, true, true,
        (SELECT id FROM permissions WHERE code = 'system:user'), NOW(), NOW()),
    (uuid_generate_v4(), 'system:user:add', '新增用户', '新增用户', 'BUTTON', 'Plus', 2, true, true,
        (SELECT id FROM permissions WHERE code = 'system:user'), NOW(), NOW()),
    (uuid_generate_v4(), 'system:user:edit', '编辑用户', '编辑用户', 'BUTTON', 'Edit', 3, true, true,
        (SELECT id FROM permissions WHERE code = 'system:user'), NOW(), NOW()),
    (uuid_generate_v4(), 'system:user:remove', '删除用户', '删除用户', 'BUTTON', 'Delete', 4, true, true,
        (SELECT id FROM permissions WHERE code = 'system:user'), NOW(), NOW()),

    -- 角色管理（菜单）
    (uuid_generate_v4(), 'system:role', '角色管理', '角色管理页面', 'MENU', 'UserFilled', 2, true, true,
        (SELECT id FROM permissions WHERE code = 'system'), NOW(), NOW()),

    -- 角色管理按钮
    (uuid_generate_v4(), 'system:role:list', '角色列表', '查看角色列表', 'BUTTON', 'View', 1, true, true,
        (SELECT id FROM permissions WHERE code = 'system:role'), NOW(), NOW()),
    (uuid_generate_v4(), 'system:role:add', '新增角色', '新增角色', 'BUTTON', 'Plus', 2, true, true,
        (SELECT id FROM permissions WHERE code = 'system:role'), NOW(), NOW()),
    (uuid_generate_v4(), 'system:role:edit', '编辑角色', '编辑角色', 'BUTTON', 'Edit', 3, true, true,
        (SELECT id FROM permissions WHERE code = 'system:role'), NOW(), NOW()),
    (uuid_generate_v4(), 'system:role:remove', '删除角色', '删除角色', 'BUTTON', 'Delete', 4, true, true,
        (SELECT id FROM permissions WHERE code = 'system:role'), NOW(), NOW()),

    -- 权限管理（菜单）
    (uuid_generate_v4(), 'system:permission', '权限管理', '权限管理页面', 'MENU', 'Lock', 3, true, true,
        (SELECT id FROM permissions WHERE code = 'system'), NOW(), NOW()),

    -- 权限管理按钮
    (uuid_generate_v4(), 'system:permission:list', '权限列表', '查看权限列表', 'BUTTON', 'View', 1, true, true,
        (SELECT id FROM permissions WHERE code = 'system:permission'), NOW(), NOW()),
    (uuid_generate_v4(), 'system:permission:query', '权限详情', '查看权限详情', 'BUTTON', 'View', 2, true, true,
        (SELECT id FROM permissions WHERE code = 'system:permission'), NOW(), NOW()),
    (uuid_generate_v4(), 'system:permission:add', '新增权限', '新增权限', 'BUTTON', 'Plus', 3, true, true,
        (SELECT id FROM permissions WHERE code = 'system:permission'), NOW(), NOW()),
    (uuid_generate_v4(), 'system:permission:edit', '编辑权限', '编辑权限', 'BUTTON', 'Edit', 4, true, true,
        (SELECT id FROM permissions WHERE code = 'system:permission'), NOW(), NOW()),
    (uuid_generate_v4(), 'system:permission:remove', '删除权限', '删除权限', 'BUTTON', 'Delete', 5, true, true,
        (SELECT id FROM permissions WHERE code = 'system:permission'), NOW(), NOW()),

    -- 排产管理（目录）
    (uuid_generate_v4(), 'schedule', '排产管理', '排产管理模块', 'CATALOG', 'Calendar', 2, true, true, NULL, NOW(), NOW()),

    -- 排产计划（菜单）
    (uuid_generate_v4(), 'schedule:plan', '排产计划', '排产计划页面', 'MENU', 'Calendar', 1, true, true,
        (SELECT id FROM permissions WHERE code = 'schedule'), NOW(), NOW()),

    -- 排产计划按钮
    (uuid_generate_v4(), 'schedule:plan:list', '计划列表', '查看计划列表', 'BUTTON', 'View', 1, true, true,
        (SELECT id FROM permissions WHERE code = 'schedule:plan'), NOW(), NOW()),
    (uuid_generate_v4(), 'schedule:plan:create', '创建计划', '创建排产计划', 'BUTTON', 'Plus', 2, true, true,
        (SELECT id FROM permissions WHERE code = 'schedule:plan'), NOW(), NOW()),
    (uuid_generate_v4(), 'schedule:plan:solve', '执行求解', '执行排产求解', 'BUTTON', 'VideoPlay', 3, true, true,
        (SELECT id FROM permissions WHERE code = 'schedule:plan'), NOW(), NOW()),
    (uuid_generate_v4(), 'schedule:plan:stop', '停止求解', '停止排产求解', 'BUTTON', 'VideoPause', 4, true, true,
        (SELECT id FROM permissions WHERE code = 'schedule:plan'), NOW(), NOW())
ON CONFLICT (code) DO NOTHING;

COMMENT ON TABLE permissions IS '权限表';
COMMENT ON COLUMN permissions.code IS '权限编码（唯一）';
COMMENT ON COLUMN permissions.name IS '权限名称';
COMMENT ON COLUMN permissions.description IS '权限描述';
COMMENT ON COLUMN permissions.type IS '权限类型：CATALOG-目录, MENU-菜单, BUTTON-按钮';
COMMENT ON COLUMN permissions.route_path IS '路由路径';
COMMENT ON COLUMN permissions.icon IS '图标名称';
COMMENT ON COLUMN permissions.sort IS '排序号';
COMMENT ON COLUMN permissions.enabled IS '是否启用';
COMMENT ON COLUMN permissions.visible IS '是否可见';
COMMENT ON COLUMN permissions.parent_id IS '父权限ID';
