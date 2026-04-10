// API 统一响应格式
export interface AjaxResult<T = unknown> {
  code: number
  message: string
  data: T | null
}

export interface Resource {
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
  status: 'RUNNING' | 'IDLE' | 'MAINTENANCE' | 'DISABLED' | null
  calendarId: string | null
  calendarName: string | null
  createTime: string
  updateTime: string
}
