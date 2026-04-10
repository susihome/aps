import axiosInstance from './axios';
const API_BASE = '/roles';
function unwrap(result) {
    if (result.code !== 200 || result.data == null) {
        throw new Error(result.message || '请求失败');
    }
    return result.data;
}
export const useRoleApi = () => {
    return {
        // 获取角色列表（分页）
        getRoles: async (page = 0, size = 20) => {
            const { data } = await axiosInstance.get(`${API_BASE}?page=${page}&size=${size}`);
            return unwrap(data);
        },
        // 获取所有角色（不分页）
        getAllRoles: async () => {
            const { data } = await axiosInstance.get(`${API_BASE}?page=0&size=1000`);
            return unwrap(data).content;
        },
        // 获取单个角色
        getRole: async (id) => {
            const { data } = await axiosInstance.get(`${API_BASE}/${id}`);
            return unwrap(data);
        },
        // 创建角色
        createRole: async (form) => {
            const { data } = await axiosInstance.post(API_BASE, form);
            return unwrap(data);
        },
        // 更新角色
        updateRole: async (id, form) => {
            const { data } = await axiosInstance.put(`${API_BASE}/${id}`, form);
            return unwrap(data);
        },
        // 删除角色
        deleteRole: async (id) => {
            const { data } = await axiosInstance.delete(`${API_BASE}/${id}`);
            unwrap(data);
        },
        // 批量删除角色
        deleteRoles: async (ids) => {
            const { data } = await axiosInstance.delete(`${API_BASE}/batch`, {
                data: ids
            });
            unwrap(data);
        },
        // 分配权限
        assignPermissions: async (id, permissionIds) => {
            const { data } = await axiosInstance.post(`${API_BASE}/${id}/permissions`, permissionIds);
            return unwrap(data);
        },
        // 获取角色权限
        getRolePermissions: async (id) => {
            const { data } = await axiosInstance.get(`${API_BASE}/${id}/permissions`);
            return unwrap(data);
        },
        // 批量分配权限
        assignPermissionsToRoles: async (roleIds, permissionIds) => {
            const { data } = await axiosInstance.post(`${API_BASE}/batch/permissions`, { roleIds, permissionIds });
            unwrap(data);
        }
    };
};
