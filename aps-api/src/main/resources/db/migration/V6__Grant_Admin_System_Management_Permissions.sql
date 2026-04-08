INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.code IN (
    'system',
    'system:user',
    'system:user:list',
    'system:user:add',
    'system:user:edit',
    'system:user:delete',
    'system:user:reset_password',
    'system:permission',
    'system:permission:list',
    'system:permission:query',
    'system:permission:add',
    'system:permission:edit',
    'system:permission:remove'
)
WHERE r.name = 'ADMIN'
  AND NOT EXISTS (
    SELECT 1
    FROM role_permissions rp
    WHERE rp.role_id = r.id
      AND rp.permission_id = p.id
  );
