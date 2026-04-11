import axiosInstance from './axios'
import type { AjaxResult } from './types'

export interface Material {
  id: string
  materialCode: string
  materialName: string
  specification: string | null
  unit: string | null
  enabled: boolean
  remark: string | null
  createTime: string
  updateTime: string
  // 车间排产属性
  colorCode: string | null
  rawMaterialType: string | null
  defaultLotSize: number | null
  minLotSize: number | null
  maxLotSize: number | null
  allowDelay: boolean | null
  abcClassification: string | null
  productGroup: string | null
}

export interface CreateMaterialRequest {
  materialCode: string
  materialName: string
  specification?: string | null
  unit?: string | null
  enabled?: boolean
  remark?: string | null
  colorCode?: string | null
  rawMaterialType?: string | null
  defaultLotSize?: number | null
  minLotSize?: number | null
  maxLotSize?: number | null
  allowDelay?: boolean | null
  abcClassification?: string | null
  productGroup?: string | null
}

export interface UpdateMaterialRequest {
  materialName?: string
  specification?: string | null
  unit?: string | null
  enabled?: boolean
  remark?: string | null
  colorCode?: string | null
  rawMaterialType?: string | null
  defaultLotSize?: number | null
  minLotSize?: number | null
  maxLotSize?: number | null
  allowDelay?: boolean | null
  abcClassification?: string | null
  productGroup?: string | null
}

function unwrap<T>(result: AjaxResult<T>): T {
  if (result.code !== 200 || result.data == null) {
    throw new Error(result.message || '请求失败')
  }
  return result.data
}

export const materialApi = {
  getAll: async () => {
    const { data } = await axiosInstance.get<AjaxResult<Material[]>>('/materials')
    return unwrap(data)
  },

  getById: async (id: string) => {
    const { data } = await axiosInstance.get<AjaxResult<Material>>(`/materials/${id}`)
    return unwrap(data)
  },

  create: async (form: CreateMaterialRequest) => {
    const { data } = await axiosInstance.post<AjaxResult<Material>>('/materials', form)
    return unwrap(data)
  },

  update: async (id: string, form: UpdateMaterialRequest) => {
    const { data } = await axiosInstance.put<AjaxResult<Material>>(`/materials/${id}`, form)
    return unwrap(data)
  },

  delete: async (id: string) => {
    const { data } = await axiosInstance.delete<AjaxResult<null>>(`/materials/${id}`)
    if (data.code !== 200) {
      throw new Error(data.message || '请求失败')
    }
  },
}
