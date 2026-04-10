import axiosInstance from './axios'
import type { AjaxResult, Resource } from './types'

export type ResourceCapacityDateType = 'WORKDAY' | 'RESTDAY' | 'HOLIDAY'

export interface ResourceCapacityDay {
  date: string
  dateType: ResourceCapacityDateType
  dateLabel: string | null
  defaultShiftMinutes: number
  shiftMinutesOverride: number | null
  effectiveShiftMinutes: number
  utilizationRate: number
  availableCapacityMinutes: number
  remark: string | null
  overridden: boolean
}

export interface ResourceCapacityMonthResult {
  resourceId: string
  resourceCode: string
  resourceName: string
  resourceType: string | null
  workshopId: string | null
  workshopName: string | null
  calendarId: string | null
  calendarName: string | null
  year: number
  month: number
  workdayCount: number
  totalDefaultShiftMinutes: number
  totalEffectiveShiftMinutes: number
  totalAvailableCapacityMinutes: number
  averageUtilizationRate: number
  days: ResourceCapacityDay[]
}

export interface ResourceCapacityDayUpdatePayload {
  shiftMinutesOverride?: number | null
  utilizationRate: number
  remark?: string | null
}

export interface ResourceCapacityBatchUpdatePayload {
  dates: string[]
  shiftMinutesOverride?: number | null
  utilizationRate?: number
  remark?: string | null
}

function unwrap<T>(result: AjaxResult<T>): NonNullable<T> {
  if (result.code !== 200 || result.data == null) {
    throw new Error(result.message || '请求失败')
  }
  return result.data
}

function unwrapNullable(result: AjaxResult<null>): void {
  if (result.code !== 200) {
    throw new Error(result.message || '请求失败')
  }
}

export const resourceCapacityApi = {
  getResources: async () => {
    const { data } = await axiosInstance.get<AjaxResult<Resource[]>>('/resource-capacities/resources')
    return unwrap(data)
  },

  getMonthCapacity: async (params: { resourceId: string; year: number; month: number }) => {
    const { data } = await axiosInstance.get<AjaxResult<ResourceCapacityMonthResult>>('/resource-capacities', { params })
    return unwrap(data)
  },

  updateDay: async (resourceId: string, date: string, form: ResourceCapacityDayUpdatePayload) => {
    const { data } = await axiosInstance.put<AjaxResult<ResourceCapacityDay>>(
      `/resource-capacities/resources/${resourceId}/days/${date}`,
      form
    )
    return unwrap(data)
  },

  batchUpdateDays: async (resourceId: string, form: ResourceCapacityBatchUpdatePayload) => {
    const { data } = await axiosInstance.put<AjaxResult<null>>(
      `/resource-capacities/resources/${resourceId}/days/batch`,
      form
    )
    unwrapNullable(data)
  }
}
