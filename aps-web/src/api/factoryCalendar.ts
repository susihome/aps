import axiosInstance from './axios'

export interface FactoryCalendar {
  id: string
  name: string
  code: string
  description: string
  year: number
  isDefault: boolean
  enabled: boolean
  shifts: CalendarShift[]
  workdayCount: number
  createTime: string
  updateTime: string
}

export interface CalendarShift {
  id: string
  name: string
  startTime: string
  endTime: string
  sortOrder: number
}

export interface CalendarDate {
  id: string
  date: string
  dateType: 'WORKDAY' | 'RESTDAY' | 'HOLIDAY'
  label: string
}

interface AjaxResult<T> {
  code: number
  message: string
  data: T | null
}

function unwrap<T>(result: AjaxResult<T>): T {
  if (result.code !== 200 || result.data == null) {
    throw new Error(result.message || '请求失败')
  }
  return result.data
}

export const factoryCalendarApi = {
  getCalendars: async (year?: number) => {
    const params = year ? { year } : {}
    const { data } = await axiosInstance.get<AjaxResult<FactoryCalendar[]>>('/factory-calendars', { params })
    return unwrap(data)
  },

  getCalendar: async (id: string) => {
    const { data } = await axiosInstance.get<AjaxResult<FactoryCalendar>>(`/factory-calendars/${id}`)
    return unwrap(data)
  },

  createCalendar: async (form: { name: string; code: string; year: number; description?: string }) => {
    const { data } = await axiosInstance.post<AjaxResult<FactoryCalendar>>('/factory-calendars', form)
    return unwrap(data)
  },

  updateCalendar: async (id: string, form: { name?: string; description?: string; enabled?: boolean }) => {
    const { data } = await axiosInstance.put<AjaxResult<FactoryCalendar>>(`/factory-calendars/${id}`, form)
    return unwrap(data)
  },

  deleteCalendar: async (id: string) => {
    const { data } = await axiosInstance.delete<AjaxResult<null>>(`/factory-calendars/${id}`)
    unwrap(data)
  },

  setDefault: async (id: string) => {
    const { data } = await axiosInstance.put<AjaxResult<null>>(`/factory-calendars/${id}/default`)
    unwrap(data)
  },

  // 班次
  getShifts: async (calendarId: string) => {
    const { data } = await axiosInstance.get<AjaxResult<CalendarShift[]>>(`/factory-calendars/${calendarId}/shifts`)
    return unwrap(data)
  },

  addShift: async (calendarId: string, form: { name: string; startTime: string; endTime: string; sortOrder?: number }) => {
    const { data } = await axiosInstance.post<AjaxResult<CalendarShift>>(`/factory-calendars/${calendarId}/shifts`, form)
    return unwrap(data)
  },

  updateShift: async (calendarId: string, shiftId: string, form: { name?: string; startTime?: string; endTime?: string; sortOrder?: number }) => {
    const { data } = await axiosInstance.put<AjaxResult<CalendarShift>>(`/factory-calendars/${calendarId}/shifts/${shiftId}`, form)
    return unwrap(data)
  },

  deleteShift: async (calendarId: string, shiftId: string) => {
    const { data } = await axiosInstance.delete<AjaxResult<null>>(`/factory-calendars/${calendarId}/shifts/${shiftId}`)
    unwrap(data)
  },

  // 日期
  getDatesByMonth: async (calendarId: string, year: number, month: number) => {
    const { data } = await axiosInstance.get<AjaxResult<CalendarDate[]>>(`/factory-calendars/${calendarId}/dates`, {
      params: { year, month }
    })
    return unwrap(data)
  },

  updateDateType: async (calendarId: string, form: { date: string; dateType: string; label?: string }) => {
    const { data } = await axiosInstance.put<AjaxResult<null>>(`/factory-calendars/${calendarId}/dates`, form)
    unwrap(data)
  },

  batchSetHolidays: async (calendarId: string, form: { dates: string[]; label?: string }) => {
    const { data } = await axiosInstance.post<AjaxResult<null>>(`/factory-calendars/${calendarId}/dates/holidays`, form)
    unwrap(data)
  },

  batchUpdateDates: async (calendarId: string, form: { dates: string[]; dateType: string; label?: string }) => {
    const { data } = await axiosInstance.put<AjaxResult<null>>(`/factory-calendars/${calendarId}/dates/batch`, form)
    unwrap(data)
  }
}
