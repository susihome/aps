import axiosInstance from './axios'
import type { AjaxResult } from './types'

export interface Mold {
  id: string
  moldCode: string
  moldName: string
  cavityCount: number | null
  status: string | null
  enabled: boolean
  remark: string | null
  createTime: string
  updateTime: string
}

export interface CreateMoldRequest {
  moldCode: string
  moldName: string
  cavityCount?: number | null
  status?: string | null
  enabled?: boolean
  remark?: string | null
}

export interface UpdateMoldRequest {
  moldName?: string
  cavityCount?: number | null
  status?: string | null
  enabled?: boolean
  remark?: string | null
}

function unwrap<T>(result: AjaxResult<T>): T {
  if (result.code !== 200 || result.data == null) {
    throw new Error(result.message || '请求失败')
  }
  return result.data
}

export const moldApi = {
  getAll: async (keyword?: string, limit?: number) => {
    const params: Record<string, string | number> = {}
    if (keyword && keyword.trim()) params.keyword = keyword.trim()
    if (limit != null) params.limit = limit
    const { data } = await axiosInstance.get<AjaxResult<Mold[]>>('/molds', { params })
    return unwrap(data)
  },

  getById: async (id: string) => {
    const { data } = await axiosInstance.get<AjaxResult<Mold>>(`/molds/${id}`)
    return unwrap(data)
  },

  create: async (form: CreateMoldRequest) => {
    const { data } = await axiosInstance.post<AjaxResult<Mold>>('/molds', form)
    return unwrap(data)
  },

  update: async (id: string, form: UpdateMoldRequest) => {
    const { data } = await axiosInstance.put<AjaxResult<Mold>>(`/molds/${id}`, form)
    return unwrap(data)
  },

  delete: async (id: string) => {
    const { data } = await axiosInstance.delete<AjaxResult<null>>(`/molds/${id}`)
    if (data.code !== 200) {
      throw new Error(data.message || '请求失败')
    }
  },
}
