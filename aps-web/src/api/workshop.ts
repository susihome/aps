import axiosInstance from './axios'
import type { AjaxResult } from './types'

// ===== 类型定义 =====

export interface Workshop {
  id: string
  code: string
  name: string
  calendarId: string | null
  calendarName: string | null
  managerName: string | null
  enabled: boolean
  sortOrder: number
  description: string | null
  createTime: string
  updateTime: string
}

export interface MachineResource {
  id: string
  resourceCode: string
  resourceName: string
  resourceType: string | null
  available: boolean
  workshopId: string | null
  workshopName: string | null
  tonnage: number | null
  machineBrand: string | null
  machineModel: string | null
  maxShotWeight: number | null
  status: MachineStatus | null
  calendarId: string | null
  calendarName: string | null
  createTime: string
  updateTime: string
}

export type MachineStatus = 'RUNNING' | 'IDLE' | 'MAINTENANCE' | 'DISABLED'

function unwrap<T>(result: AjaxResult<T>): T {
  if (result.code !== 200) {
    throw new Error(result.message || '请求失败')
  }
  return result.data as T
}

// ===== 车间 API =====

export const workshopApi = {
  getAll: async () => {
    const { data } = await axiosInstance.get<AjaxResult<Workshop[]>>('/workshops')
    return unwrap(data)
  },

  getById: async (id: string) => {
    const { data } = await axiosInstance.get<AjaxResult<Workshop>>(`/workshops/${id}`)
    return unwrap(data)
  },

  create: async (form: {
    code: string
    name: string
    calendarId?: string | null
    managerName?: string
    sortOrder?: number
    description?: string
  }) => {
    const { data } = await axiosInstance.post<AjaxResult<Workshop>>('/workshops', form)
    return unwrap(data)
  },

  update: async (id: string, form: {
    name?: string
    calendarId?: string | null
    managerName?: string
    sortOrder?: number
    description?: string
    enabled?: boolean
  }) => {
    const { data } = await axiosInstance.put<AjaxResult<Workshop>>(`/workshops/${id}`, form)
    return unwrap(data)
  },

  delete: async (id: string) => {
    const { data } = await axiosInstance.delete<AjaxResult<null>>(`/workshops/${id}`)
    unwrap(data)
  },

  getEffectiveCalendar: async (id: string) => {
    const { data } = await axiosInstance.get<AjaxResult<{
      id: string
      name: string
      code: string
      year: number
      enabled: boolean
    } | null>>(`/workshops/${id}/effective-calendar`)
    return unwrap(data)
  }
}

// ===== 资源（注塑机）API =====

export const resourceApi = {
  getAll: async (params?: { workshopId?: string; status?: MachineStatus }) => {
    const { data } = await axiosInstance.get<AjaxResult<MachineResource[]>>('/resources', { params })
    return unwrap(data)
  },

  getById: async (id: string) => {
    const { data } = await axiosInstance.get<AjaxResult<MachineResource>>(`/resources/${id}`)
    return unwrap(data)
  },

  create: async (form: {
    resourceCode: string
    resourceName: string
    resourceType?: string
    workshopId?: string | null
    tonnage?: number
    machineBrand?: string
    machineModel?: string
    maxShotWeight?: number
    status?: MachineStatus
    calendarId?: string | null
  }) => {
    const { data } = await axiosInstance.post<AjaxResult<MachineResource>>('/resources', form)
    return unwrap(data)
  },

  update: async (id: string, form: {
    resourceName?: string
    resourceType?: string
    workshopId?: string | null
    tonnage?: number
    machineBrand?: string
    machineModel?: string
    maxShotWeight?: number
    status?: MachineStatus
    calendarId?: string | null
    available?: boolean
  }) => {
    const { data } = await axiosInstance.put<AjaxResult<MachineResource>>(`/resources/${id}`, form)
    return unwrap(data)
  },

  delete: async (id: string) => {
    const { data } = await axiosInstance.delete<AjaxResult<null>>(`/resources/${id}`)
    unwrap(data)
  }
}
