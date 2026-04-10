import axiosInstance from './axios';
function unwrap(result) {
    if (result.code !== 200 || result.data == null) {
        throw new Error(result.message || '请求失败');
    }
    return result.data;
}
function toPageResult(data) {
    return {
        items: data.content,
        total: data.totalElements,
        pageNo: data.number + 1,
        pageSize: data.size,
        totalPages: data.totalPages,
    };
}
export const dictionaryApi = {
    getTypes: async (params) => {
        const { data } = await axiosInstance.get('/dictionaries/types', { params });
        return toPageResult(unwrap(data));
    },
    createType: async (form) => {
        const { data } = await axiosInstance.post('/dictionaries/types', form);
        return unwrap(data);
    },
    updateType: async (id, form) => {
        const { data } = await axiosInstance.put(`/dictionaries/types/${id}`, form);
        return unwrap(data);
    },
    toggleTypeEnabled: async (id, enabled) => {
        const { data } = await axiosInstance.patch(`/dictionaries/types/${id}/enabled`, { enabled });
        return unwrap(data);
    },
    deleteType: async (id) => {
        const { data } = await axiosInstance.delete(`/dictionaries/types/${id}`);
        unwrap(data);
    },
    getItemsByType: async (typeId, params) => {
        const { data } = await axiosInstance.get(`/dictionaries/types/${typeId}/items`, { params });
        return toPageResult(unwrap(data));
    },
    createItem: async (typeId, form) => {
        const { data } = await axiosInstance.post(`/dictionaries/types/${typeId}/items`, form);
        return unwrap(data);
    },
    updateItem: async (id, form) => {
        const { data } = await axiosInstance.put(`/dictionaries/items/${id}`, form);
        return unwrap(data);
    },
    toggleItemEnabled: async (id, enabled) => {
        const { data } = await axiosInstance.patch(`/dictionaries/items/${id}/enabled`, { enabled });
        return unwrap(data);
    },
    deleteItem: async (id) => {
        const { data } = await axiosInstance.delete(`/dictionaries/items/${id}`);
        unwrap(data);
    },
    getEnabledItemsByTypeCode: async (typeCode) => {
        const { data } = await axiosInstance.get(`/dictionaries/${typeCode}/enabled-items`);
        return unwrap(data);
    },
};
