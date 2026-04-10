import axiosInstance from './axios';
export const orderApi = {
    /**
     * 获取工单列表
     */
    async list() {
        const response = await axiosInstance.get('/orders');
        if (response.data.code !== 200 || !response.data.data) {
            throw new Error(response.data.message || '获取工单列表失败');
        }
        return response.data.data;
    },
    /**
     * 获取工单详情
     */
    async getById(id) {
        const response = await axiosInstance.get(`/orders/${id}`);
        if (response.data.code !== 200 || !response.data.data) {
            throw new Error(response.data.message || '获取工单详情失败');
        }
        return response.data.data;
    },
    /**
     * 创建工单
     */
    async create(data) {
        const response = await axiosInstance.post('/orders', data);
        if (response.data.code !== 200 || !response.data.data) {
            throw new Error(response.data.message || '创建工单失败');
        }
        return response.data.data;
    },
    /**
     * 删除工单
     */
    async delete(id) {
        const response = await axiosInstance.delete(`/orders/${id}`);
        if (response.data.code !== 200) {
            throw new Error(response.data.message || '删除工单失败');
        }
    }
};
