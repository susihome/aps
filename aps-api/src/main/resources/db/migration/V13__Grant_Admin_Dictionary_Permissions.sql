INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.code IN (
    'system:dict',
    'system:dict:type:list',
    'system:dict:type:query',
    'system:dict:type:add',
    'system:dict:type:edit',
    'system:dict:type:remove',
    'system:dict:item:list',
    'system:dict:item:query',
    'system:dict:item:add',
    'system:dict:item:edit',
    'system:dict:item:remove',
    'system:dict:query'
)
WHERE r.name = 'ADMIN'
  AND NOT EXISTS (
    SELECT 1
    FROM role_permissions rp
    WHERE rp.role_id = r.id
      AND rp.permission_id = p.id
  );
