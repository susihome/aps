import axiosInstance from './axios';
export const scheduleApi = {
    /**
     * 获取排产方案列表
     */
    async list() {
        const response = await axiosInstance.get('/schedules');
        if (response.data.code !== 200 || !response.data.data) {
            throw new Error(response.data.message || '获取排产方案列表失败');
        }
        return response.data.data;
    },
    /**
     * 获取排产方案详情
     */
    async getById(id) {
        const response = await axiosInstance.get(`/schedules/${id}`);
        if (response.data.code !== 200 || !response.data.data) {
            throw new Error(response.data.message || '获取排产方案详情失败');
        }
        return response.data.data;
    },
    /**
     * 开始求解
     */
    async solve(id) {
        const response = await axiosInstance.post(`/schedules/${id}/solve`);
        if (response.data.code !== 200) {
            throw new Error(response.data.message || '开始求解失败');
        }
    },
    /**
     * 停止求解
     */
    async stop(id) {
        const response = await axiosInstance.post(`/schedules/${id}/stop`);
        if (response.data.code !== 200) {
            throw new Error(response.data.message || '停止求解失败');
        }
    }
};
