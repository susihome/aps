-- 修复权限的父子关系

-- system:user -> parent: system
UPDATE permissions SET parent_id = (SELECT id FROM permissions WHERE code = 'system')
WHERE code = 'system:user' AND parent_id IS NULL;

-- system:permission -> parent: system
UPDATE permissions SET parent_id = (SELECT id FROM permissions WHERE code = 'system')
WHERE code = 'system:permission' AND parent_id IS NULL;

-- system:role -> parent: system
UPDATE permissions SET parent_id = (SELECT id FROM permissions WHERE code = 'system')
WHERE code = 'system:role' AND parent_id IS NULL;

-- system:user 的子权限
UPDATE permissions SET parent_id = (SELECT id FROM permissions WHERE code = 'system:user')
WHERE code = 'system:user:list' AND parent_id IS NULL;

UPDATE permissions SET parent_id = (SELECT id FROM permissions WHERE code = 'system:user')
WHERE code = 'system:user:add' AND parent_id IS NULL;

UPDATE permissions SET parent_id = (SELECT id FROM permissions WHERE code = 'system:user')
WHERE code = 'system:user:edit' AND parent_id IS NULL;

UPDATE permissions SET parent_id = (SELECT id FROM permissions WHERE code = 'system:user')
WHERE code = 'system:user:delete' AND parent_id IS NULL;

UPDATE permissions SET parent_id = (SELECT id FROM permissions WHERE code = 'system:user')
WHERE code = 'system:user:remove' AND parent_id IS NULL;

UPDATE permissions SET parent_id = (SELECT id FROM permissions WHERE code = 'system:user')
WHERE code = 'system:user:reset_password' AND parent_id IS NULL;

-- system:permission 的子权限
UPDATE permissions SET parent_id = (SELECT id FROM permissions WHERE code = 'system:permission')
WHERE code = 'system:permission:list' AND parent_id IS NULL;

UPDATE permissions SET parent_id = (SELECT id FROM permissions WHERE code = 'system:permission')
WHERE code = 'system:permission:query' AND parent_id IS NULL;

UPDATE permissions SET parent_id = (SELECT id FROM permissions WHERE code = 'system:permission')
WHERE code = 'system:permission:add' AND parent_id IS NULL;

UPDATE permissions SET parent_id = (SELECT id FROM permissions WHERE code = 'system:permission')
WHERE code = 'system:permission:edit' AND parent_id IS NULL;

UPDATE permissions SET parent_id = (SELECT id FROM permissions WHERE code = 'system:permission')
WHERE code = 'system:permission:remove' AND parent_id IS NULL;

-- system:role 的子权限
UPDATE permissions SET parent_id = (SELECT id FROM permissions WHERE code = 'system:role')
WHERE code = 'system:role:list' AND parent_id IS NULL;

UPDATE permissions SET parent_id = (SELECT id FROM permissions WHERE code = 'system:role')
WHERE code = 'system:role:add' AND parent_id IS NULL;

UPDATE permissions SET parent_id = (SELECT id FROM permissions WHERE code = 'system:role')
WHERE code = 'system:role:edit' AND parent_id IS NULL;

UPDATE permissions SET parent_id = (SELECT id FROM permissions WHERE code = 'system:role')
WHERE code = 'system:role:remove' AND parent_id IS NULL;

-- schedule:plan -> parent: schedule
UPDATE permissions SET parent_id = (SELECT id FROM permissions WHERE code = 'schedule')
WHERE code = 'schedule:plan' AND parent_id IS NULL;

-- schedule:plan 的子权限
UPDATE permissions SET parent_id = (SELECT id FROM permissions WHERE code = 'schedule:plan')
WHERE code = 'schedule:plan:list' AND parent_id IS NULL;

UPDATE permissions SET parent_id = (SELECT id FROM permissions WHERE code = 'schedule:plan')
WHERE code = 'schedule:plan:create' AND parent_id IS NULL;

UPDATE permissions SET parent_id = (SELECT id FROM permissions WHERE code = 'schedule:plan')
WHERE code = 'schedule:plan:solve' AND parent_id IS NULL;

UPDATE permissions SET parent_id = (SELECT id FROM permissions WHERE code = 'schedule:plan')
WHERE code = 'schedule:plan:stop' AND parent_id IS NULL;
