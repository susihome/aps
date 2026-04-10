import axiosInstance from './axios';
export const authApi = {
    /**
     * 用户登录
     */
    async login(request) {
        const response = await axiosInstance.post('/auth/login', request);
        if (response.data.code !== 200 || !response.data.data) {
            throw new Error(response.data.message || '登录失败');
        }
        return response.data.data;
    },
    /**
     * 用户登出
     */
    async logout() {
        const response = await axiosInstance.post('/auth/logout');
        if (response.data.code !== 200) {
            throw new Error(response.data.message || '登出失败');
        }
    },
    /**
     * 刷新令牌
     */
    async refreshToken() {
        const response = await axiosInstance.post('/auth/refresh');
        if (response.data.code !== 200 || !response.data.data) {
            throw new Error(response.data.message || '刷新令牌失败');
        }
        return response.data.data;
    },
    /**
     * 获取当前用户信息
     */
    async getCurrentUser() {
        const response = await axiosInstance.get('/auth/me');
        if (response.data.code !== 200 || !response.data.data) {
            throw new Error(response.data.message || '获取用户信息失败');
        }
        return response.data.data;
    }
};
