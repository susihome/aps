import axiosInstance from './axios';
const API_BASE = '/permissions';
function normalizeType(type) {
    const normalized = type.toLowerCase();
    if (normalized === 'catalog' || normalized === 'menu' || normalized === 'button') {
        return normalized;
    }
    return 'button';
}
function normalizePermission(permission) {
    return {
        id: permission.id,
        code: permission.code,
        name: permission.name,
        description: permission.description,
        type: normalizeType(permission.type),
        routePath: permission.routePath,
        icon: permission.icon,
        sort: permission.sort,
        enabled: permission.enabled,
        visible: permission.visible,
        parentId: permission.parent?.id ?? permission.parentId,
        children: Array.isArray(permission.children)
            ? permission.children.map(normalizePermission)
            : []
    };
}
function unwrap(result) {
    if (result.code !== 200 || result.data == null) {
        throw new Error(result.message || '请求失败');
    }
    return result.data;
}
export const usePermissionApi = () => {
    return {
        // 获取权限树
        getPermissionTree: async () => {
            const { data } = await axiosInstance.get(`${API_BASE}/tree`);
            return unwrap(data).map(normalizePermission);
        },
        // 获取单个权限
        getPermission: async (id) => {
            const { data } = await axiosInstance.get(`${API_BASE}/${id}`);
            return normalizePermission(unwrap(data));
        },
        // 创建权限
        createPermission: async (form) => {
            const payload = {
                ...form,
                type: form.type.toUpperCase()
            };
            const { data } = await axiosInstance.post(API_BASE, payload);
            return normalizePermission(unwrap(data));
        },
        // 更新权限
        updatePermission: async (id, form) => {
            const payload = {
                ...form,
                type: form.type ? form.type.toUpperCase() : undefined
            };
            const { data } = await axiosInstance.put(`${API_BASE}/${id}`, payload);
            return normalizePermission(unwrap(data));
        },
        // 删除权限
        deletePermission: async (id) => {
            const { data } = await axiosInstance.delete(`${API_BASE}/${id}`);
            unwrap(data);
        },
        // 批量更新排序
        updateSort: async (updates) => {
            const { data } = await axiosInstance.post(`${API_BASE}/batch-sort`, { updates });
            unwrap(data);
        },
        // 启用/禁用权限
        togglePermission: async (id, enabled) => {
            const { data } = await axiosInstance.patch(`${API_BASE}/${id}/toggle`, { enabled });
            return normalizePermission(unwrap(data));
        }
    };
};
