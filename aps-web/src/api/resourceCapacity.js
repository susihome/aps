import axiosInstance from './axios';
function unwrap(result) {
    if (result.code !== 200 || result.data == null) {
        throw new Error(result.message || '请求失败');
    }
    return result.data;
}
function unwrapNullable(result) {
    if (result.code !== 200) {
        throw new Error(result.message || '请求失败');
    }
}
export const resourceCapacityApi = {
    getResources: async () => {
        const { data } = await axiosInstance.get('/resource-capacities/resources');
        return unwrap(data);
    },
    getMonthCapacity: async (params) => {
        const { data } = await axiosInstance.get('/resource-capacities', { params });
        return unwrap(data);
    },
    updateDay: async (resourceId, date, form) => {
        const { data } = await axiosInstance.put(`/resource-capacities/resources/${resourceId}/days/${date}`, form);
        return unwrap(data);
    },
    batchUpdateDays: async (resourceId, form) => {
        const { data } = await axiosInstance.put(`/resource-capacities/resources/${resourceId}/days/batch`, form);
        unwrapNullable(data);
    }
};
