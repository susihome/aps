import axios from './axios'
import type { AjaxResult } from './types'

// 审计日志类型
export interface AuditLog {
  id: string
  userId: string
  username: string
  action: string
  resource: string
  details: string
  ipAddress: string
  timestamp: string
}

// 分页响应类型
export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}

// 搜索参数
export interface AuditLogSearchParams {
  userId?: string
  username?: string
  action?: string
  resource?: string
  startTime?: string
  endTime?: string
  page?: number
  size?: number
}

// 统计数据
export interface AuditStatistics {
  actionStatistics: Record<string, number>
  userStatistics: Record<string, number>
  startTime: string
  endTime: string
}

/**
 * 多条件搜索审计日志
 */
export function searchAuditLogs(params: AuditLogSearchParams) {
  return axios.get<AjaxResult<PageResponse<AuditLog>>>('/audit-logs/search', { params })
}

/**
 * 获取统计数据
 */
export function getStatistics(startTime?: string, endTime?: string) {
  return axios.get<AjaxResult<AuditStatistics>>('/audit-logs/statistics', {
    params: { startTime, endTime }
  })
}

/**
 * 导出审计日志为CSV
 */
export function exportAuditLogs(startTime?: string, endTime?: string) {
  return axios.get('/audit-logs/export', {
    params: { startTime, endTime },
    responseType: 'blob'
  })
}
