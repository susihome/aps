import axios from './axios';
/**
 * 分页查询审计日志
 */
export function getAuditLogs(params) {
    return axios.get('/audit-logs', { params });
}
/**
 * 多条件搜索审计日志
 */
export function searchAuditLogs(params) {
    return axios.get('/audit-logs/search', { params });
}
/**
 * 查询单条审计日志详情
 */
export function getAuditLogById(id) {
    return axios.get(`/audit-logs/${id}`);
}
/**
 * 获取统计数据
 */
export function getStatistics(startTime, endTime) {
    return axios.get('/audit-logs/statistics', {
        params: { startTime, endTime }
    });
}
/**
 * 导出审计日志为CSV
 */
export function exportAuditLogs(startTime, endTime) {
    return axios.get('/audit-logs/export', {
        params: { startTime, endTime },
        responseType: 'blob'
    });
}
