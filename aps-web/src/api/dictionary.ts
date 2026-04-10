import axiosInstance from './axios'
import type { AjaxResult } from './types'

export interface DictType {
  id: string
  code: string
  name: string
  description?: string | null
  enabled: boolean
  sortOrder: number
  createTime: string
  updateTime: string
}

export interface DictItem {
  id: string
  dictTypeId: string
  dictTypeCode: string
  itemCode: string
  itemName: string
  itemValue: string
  description?: string | null
  enabled: boolean
  sortOrder: number
  isSystem: boolean
  createTime: string
  updateTime: string
}

export interface DictTypeForm {
  code: string
  name: string
  description?: string
  enabled?: boolean
  sortOrder?: number
}

export interface DictItemForm {
  itemCode: string
  itemName: string
  itemValue: string
  description?: string
  enabled?: boolean
  sortOrder?: number
  isSystem?: boolean
}

interface BackendPageResult<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
  size: number
}

interface DictionaryPageResult<T> {
  items: T[]
  total: number
  pageNo: number
  pageSize: number
  totalPages: number
}

function unwrap<T>(result: AjaxResult<T>): T {
  if (result.code !== 200 || result.data == null) {
    throw new Error(result.message || '请求失败')
  }
  return result.data
}

function toPageResult<T>(data: BackendPageResult<T>): DictionaryPageResult<T> {
  return {
    items: data.content,
    total: data.totalElements,
    pageNo: data.number + 1,
    pageSize: data.size,
    totalPages: data.totalPages,
  }
}

export const dictionaryApi = {
  getTypes: async (params: { pageNo: number; pageSize: number; keyword?: string; enabled?: boolean }) => {
    const { data } = await axiosInstance.get<AjaxResult<BackendPageResult<DictType>>>('/dictionaries/types', { params })
    return toPageResult(unwrap(data))
  },

  createType: async (form: DictTypeForm) => {
    const { data } = await axiosInstance.post<AjaxResult<DictType>>('/dictionaries/types', form)
    return unwrap(data)
  },

  updateType: async (id: string, form: DictTypeForm) => {
    const { data } = await axiosInstance.put<AjaxResult<DictType>>(`/dictionaries/types/${id}`, form)
    return unwrap(data)
  },

  toggleTypeEnabled: async (id: string, enabled: boolean) => {
    const { data } = await axiosInstance.patch<AjaxResult<DictType>>(`/dictionaries/types/${id}/enabled`, { enabled })
    return unwrap(data)
  },

  deleteType: async (id: string) => {
    const { data } = await axiosInstance.delete<AjaxResult<null>>(`/dictionaries/types/${id}`)
    unwrap(data)
  },

  getItemsByType: async (typeId: string, params: { pageNo: number; pageSize: number; keyword?: string; enabled?: boolean }) => {
    const { data } = await axiosInstance.get<AjaxResult<BackendPageResult<DictItem>>>(`/dictionaries/types/${typeId}/items`, { params })
    return toPageResult(unwrap(data))
  },

  createItem: async (typeId: string, form: DictItemForm) => {
    const { data } = await axiosInstance.post<AjaxResult<DictItem>>(`/dictionaries/types/${typeId}/items`, form)
    return unwrap(data)
  },

  updateItem: async (id: string, form: DictItemForm) => {
    const { data } = await axiosInstance.put<AjaxResult<DictItem>>(`/dictionaries/items/${id}`, form)
    return unwrap(data)
  },

  toggleItemEnabled: async (id: string, enabled: boolean) => {
    const { data } = await axiosInstance.patch<AjaxResult<DictItem>>(`/dictionaries/items/${id}/enabled`, { enabled })
    return unwrap(data)
  },

  deleteItem: async (id: string) => {
    const { data } = await axiosInstance.delete<AjaxResult<null>>(`/dictionaries/items/${id}`)
    unwrap(data)
  },

  getEnabledItemsByTypeCode: async (typeCode: string) => {
    const { data } = await axiosInstance.get<AjaxResult<DictItem[]>>(`/dictionaries/${typeCode}/enabled-items`)
    return unwrap(data)
  },
}
