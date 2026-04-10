import axiosInstance from './axios'
import type { AjaxResult } from './types'

export interface Schedule {
  id: string
  name: string
  status: string
  scheduleStartTime: string
  scheduleEndTime: string
  score?: string
  createTime?: string
  updateTime?: string
}

export const scheduleApi = {
  /**
   * 获取排产方案列表
   */
  async list(): Promise<Schedule[]> {
    const response = await axiosInstance.get<AjaxResult<Schedule[]>>('/schedules')
    if (response.data.code !== 200 || !response.data.data) {
      throw new Error(response.data.message || '获取排产方案列表失败')
    }
    return response.data.data
  },

  /**
   * 获取排产方案详情
   */
  async getById(id: string): Promise<Schedule> {
    const response = await axiosInstance.get<AjaxResult<Schedule>>(`/schedules/${id}`)
    if (response.data.code !== 200 || !response.data.data) {
      throw new Error(response.data.message || '获取排产方案详情失败')
    }
    return response.data.data
  },

  /**
   * 开始求解
   */
  async solve(id: string): Promise<void> {
    const response = await axiosInstance.post<AjaxResult<void>>(`/schedules/${id}/solve`)
    if (response.data.code !== 200) {
      throw new Error(response.data.message || '开始求解失败')
    }
  },

  /**
   * 停止求解
   */
  async stop(id: string): Promise<void> {
    const response = await axiosInstance.post<AjaxResult<void>>(`/schedules/${id}/stop`)
    if (response.data.code !== 200) {
      throw new Error(response.data.message || '停止求解失败')
    }
  }
}
