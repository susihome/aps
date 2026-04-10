import axios from 'axios';
import { msgError } from '@/utils/message';
const axiosInstance = axios.create({
    baseURL: '/api',
    timeout: 30000,
    withCredentials: true,
    headers: {
        'Content-Type': 'application/json'
    }
});
// 请求拦截器
axiosInstance.interceptors.request.use((config) => config, (error) => Promise.reject(error));
// 响应拦截器：统一错误处理和令牌刷新
let isRefreshing = false;
let failedQueue = [];
const processQueue = (error = null) => {
    failedQueue.forEach((promise) => {
        if (error) {
            promise.reject(error);
        }
        else {
            promise.resolve();
        }
    });
    failedQueue = [];
};
const isSilentAuthRequest = (url) => {
    if (!url) {
        return false;
    }
    return url.includes('/auth/me') || url.includes('/auth/refresh');
};
axiosInstance.interceptors.response.use((response) => response, async (error) => {
    const originalRequest = error.config;
    // 401 错误处理
    if (error.response?.status === 401 && !originalRequest._retry) {
        if (originalRequest.url?.includes('/auth/refresh')) {
            // 刷新令牌失败，跳转登录
            isRefreshing = false;
            processQueue(error);
            window.location.href = '/login';
            return Promise.reject(error);
        }
        if (isRefreshing) {
            // 正在刷新，将请求加入队列
            return new Promise((resolve, reject) => {
                failedQueue.push({ resolve, reject });
            })
                .then(() => axiosInstance(originalRequest))
                .catch((err) => Promise.reject(err));
        }
        originalRequest._retry = true;
        isRefreshing = true;
        try {
            await axios.post('/api/auth/refresh', {}, { withCredentials: true });
            isRefreshing = false;
            processQueue();
            return axiosInstance(originalRequest);
        }
        catch (refreshError) {
            isRefreshing = false;
            processQueue(refreshError);
            window.location.href = '/login';
            return Promise.reject(refreshError);
        }
    }
    // 403 错误
    if (error.response?.status === 403) {
        if (!isSilentAuthRequest(originalRequest?.url)) {
            msgError('无权限访问');
        }
    }
    // 500 错误
    if (error.response?.status === 500) {
        msgError('服务器错误，请稍后重试');
    }
    // 网络错误
    if (!error.response) {
        msgError('网络连接失败，请检查网络');
    }
    return Promise.reject(error);
});
export default axiosInstance;
