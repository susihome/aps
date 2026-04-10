import axiosInstance from './axios';
function unwrap(result) {
    if (result.code !== 200) {
        throw new Error(result.message || '请求失败');
    }
    return result.data;
}
export const factoryCalendarApi = {
    getCalendars: async (year) => {
        const params = year ? { year } : {};
        const { data } = await axiosInstance.get('/factory-calendars', { params });
        return unwrap(data);
    },
    getCalendar: async (id) => {
        const { data } = await axiosInstance.get(`/factory-calendars/${id}`);
        return unwrap(data);
    },
    createCalendar: async (form) => {
        const { data } = await axiosInstance.post('/factory-calendars', form);
        return unwrap(data);
    },
    updateCalendar: async (id, form) => {
        const { data } = await axiosInstance.put(`/factory-calendars/${id}`, form);
        return unwrap(data);
    },
    deleteCalendar: async (id) => {
        const { data } = await axiosInstance.delete(`/factory-calendars/${id}`);
        unwrap(data);
    },
    setDefault: async (id) => {
        const { data } = await axiosInstance.put(`/factory-calendars/${id}/default`);
        unwrap(data);
    },
    // 班次
    getShifts: async (calendarId) => {
        const { data } = await axiosInstance.get(`/factory-calendars/${calendarId}/shifts`);
        return unwrap(data);
    },
    addShift: async (calendarId, form) => {
        const { data } = await axiosInstance.post(`/factory-calendars/${calendarId}/shifts`, form);
        return unwrap(data);
    },
    updateShift: async (calendarId, shiftId, form) => {
        const { data } = await axiosInstance.put(`/factory-calendars/${calendarId}/shifts/${shiftId}`, form);
        return unwrap(data);
    },
    deleteShift: async (calendarId, shiftId) => {
        const { data } = await axiosInstance.delete(`/factory-calendars/${calendarId}/shifts/${shiftId}`);
        unwrap(data);
    },
    // 日期
    getDatesByMonth: async (calendarId, year, month) => {
        const { data } = await axiosInstance.get(`/factory-calendars/${calendarId}/dates`, {
            params: { year, month }
        });
        return unwrap(data);
    },
    updateDateType: async (calendarId, form) => {
        const { data } = await axiosInstance.put(`/factory-calendars/${calendarId}/dates`, form);
        unwrap(data);
    },
    batchSetHolidays: async (calendarId, form) => {
        const { data } = await axiosInstance.post(`/factory-calendars/${calendarId}/dates/holidays`, form);
        unwrap(data);
    },
    applyWeekendPattern: async (calendarId, form) => {
        const { data } = await axiosInstance.put(`/factory-calendars/${calendarId}/dates/weekend-pattern`, form);
        unwrap(data);
    },
    batchUpdateDates: async (calendarId, form) => {
        const { data } = await axiosInstance.put(`/factory-calendars/${calendarId}/dates/batch`, form);
        unwrap(data);
    }
};
