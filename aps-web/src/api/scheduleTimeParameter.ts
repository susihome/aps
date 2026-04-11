import axiosInstance from './axios'
import type { AjaxResult } from './types'

export interface ScheduleTimeParameter {
  id?: string
  resourceId?: string
  resourceCode?: string
  resourceName?: string
  orderFilterStartDays: number
  orderFilterStartTime: string
  orderFilterEndDays: number
  orderFilterEndTime: string
  planningStartDays: number
  planningStartTime: string
  displayStartDays: number
  displayEndDays: number
  completionDays: number
  timeScale: number
  factor: number
  exceedPeriod?: number | null
  isDefault: boolean
  enabled: boolean
  remark?: string
  createTime?: string
  updateTime?: string
  calculatedOrderFilterStart?: string
  calculatedOrderFilterEnd?: string
  calculatedPlanningStart?: string
  calculatedDisplayStart?: string
  calculatedDisplayEnd?: string
}

export type CreateScheduleTimeParameterRequest = Omit<ScheduleTimeParameter,
  'id' | 'resourceCode' | 'resourceName' | 'createTime' | 'updateTime' |
  'calculatedOrderFilterStart' | 'calculatedOrderFilterEnd' |
  'calculatedPlanningStart' | 'calculatedDisplayStart' | 'calculatedDisplayEnd'>

export type UpdateScheduleTimeParameterRequest = Partial<CreateScheduleTimeParameterRequest>

export const scheduleTimeParameterApi = {
  async list(): Promise<ScheduleTimeParameter[]> {
    const res = await axiosInstance.get<AjaxResult<ScheduleTimeParameter[]>>('/schedule-time-parameters')
    if (res.data.code !== 200 || !res.data.data) {
      throw new Error(res.data.message || '获取排程时间参数列表失败')
    }
    return res.data.data
  },

  async getById(id: string): Promise<ScheduleTimeParameter> {
    const res = await axiosInstance.get<AjaxResult<ScheduleTimeParameter>>(`/schedule-time-parameters/${id}`)
    if (res.data.code !== 200 || !res.data.data) {
      throw new Error(res.data.message || '获取排程时间参数详情失败')
    }
    return res.data.data
  },

  async create(data: CreateScheduleTimeParameterRequest): Promise<ScheduleTimeParameter> {
    const res = await axiosInstance.post<AjaxResult<ScheduleTimeParameter>>('/schedule-time-parameters', data)
    if (res.data.code !== 200 || !res.data.data) {
      throw new Error(res.data.message || '新增排程时间参数失败')
    }
    return res.data.data
  },

  async update(id: string, data: UpdateScheduleTimeParameterRequest): Promise<ScheduleTimeParameter> {
    const res = await axiosInstance.put<AjaxResult<ScheduleTimeParameter>>(`/schedule-time-parameters/${id}`, data)
    if (res.data.code !== 200 || !res.data.data) {
      throw new Error(res.data.message || '修改排程时间参数失败')
    }
    return res.data.data
  },

  async remove(id: string): Promise<void> {
    const res = await axiosInstance.delete<AjaxResult<void>>(`/schedule-time-parameters/${id}`)
    if (res.data.code !== 200) {
      throw new Error(res.data.message || '删除排程时间参数失败')
    }
  },

  async preview(resourceId?: string): Promise<ScheduleTimeParameter> {
    const params = resourceId ? { resourceId } : {}
    const res = await axiosInstance.get<AjaxResult<ScheduleTimeParameter>>('/schedule-time-parameters/preview', { params })
    if (res.data.code !== 200 || !res.data.data) {
      throw new Error(res.data.message || '预览排程时间参数失败')
    }
    return res.data.data
  }
}
