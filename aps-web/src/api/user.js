import axiosInstance from './axios';
export const userApi = {
    /**
     * 获取用户列表
     */
    async list() {
        const response = await axiosInstance.get('/users');
        if (response.data.code !== 200 || !response.data.data) {
            throw new Error(response.data.message || '获取用户列表失败');
        }
        return response.data.data;
    },
    /**
     * 获取用户详情
     */
    async getById(id) {
        const response = await axiosInstance.get(`/users/${id}`);
        if (response.data.code !== 200 || !response.data.data) {
            throw new Error(response.data.message || '获取用户详情失败');
        }
        return response.data.data;
    },
    /**
     * 创建用户
     */
    async create(data) {
        const response = await axiosInstance.post('/users', data);
        if (response.data.code !== 200 || !response.data.data) {
            throw new Error(response.data.message || '创建用户失败');
        }
        return response.data.data;
    },
    /**
     * 更新用户
     */
    async update(id, data) {
        const response = await axiosInstance.put(`/users/${id}`, data);
        if (response.data.code !== 200 || !response.data.data) {
            throw new Error(response.data.message || '更新用户失败');
        }
        return response.data.data;
    },
    /**
     * 删除用户
     */
    async delete(id) {
        const response = await axiosInstance.delete(`/users/${id}`);
        if (response.data.code !== 200) {
            throw new Error(response.data.message || '删除用户失败');
        }
    }
};
