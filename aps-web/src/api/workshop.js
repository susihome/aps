import axiosInstance from './axios';
function unwrap(result) {
    if (result.code !== 200) {
        throw new Error(result.message || '请求失败');
    }
    return result.data;
}
// ===== 车间 API =====
export const workshopApi = {
    getAll: async () => {
        const { data } = await axiosInstance.get('/workshops');
        return unwrap(data);
    },
    getById: async (id) => {
        const { data } = await axiosInstance.get(`/workshops/${id}`);
        return unwrap(data);
    },
    create: async (form) => {
        const { data } = await axiosInstance.post('/workshops', form);
        return unwrap(data);
    },
    update: async (id, form) => {
        const { data } = await axiosInstance.put(`/workshops/${id}`, form);
        return unwrap(data);
    },
    delete: async (id) => {
        const { data } = await axiosInstance.delete(`/workshops/${id}`);
        unwrap(data);
    },
    getEffectiveCalendar: async (id) => {
        const { data } = await axiosInstance.get(`/workshops/${id}/effective-calendar`);
        return unwrap(data);
    }
};
// ===== 资源（注塑机）API =====
export const resourceApi = {
    getAll: async (params) => {
        const { data } = await axiosInstance.get('/resources', { params });
        return unwrap(data);
    },
    getById: async (id) => {
        const { data } = await axiosInstance.get(`/resources/${id}`);
        return unwrap(data);
    },
    create: async (form) => {
        const { data } = await axiosInstance.post('/resources', form);
        return unwrap(data);
    },
    update: async (id, form) => {
        const { data } = await axiosInstance.put(`/resources/${id}`, form);
        return unwrap(data);
    },
    delete: async (id) => {
        const { data } = await axiosInstance.delete(`/resources/${id}`);
        unwrap(data);
    }
};
