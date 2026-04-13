import axiosInstance from './axios'
import type { AxiosResponse } from 'axios'
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

export interface MaterialImportResult {
  totalCount: number
  createdCount: number
  updatedCount: number
  failedCount: number
  failures: MaterialImportFailure[]
  errorFileName: string | null
  errorFileToken: string | null
}

export interface MaterialImportFailure {
  rowNumber: number
  columnName: string
  message: string
}

function unwrap<T>(result: AjaxResult<T>): T {
  if (result.code !== 200 || result.data == null) {
    throw new Error(result.message || '请求失败')
  }
  return result.data
}

export const materialApi = {
  getAll: async (keyword?: string, limit?: number) => {
    const params: Record<string, string | number> = {}
    if (keyword && keyword.trim()) params.keyword = keyword.trim()
    if (limit != null) params.limit = limit
    const { data } = await axiosInstance.get<AjaxResult<Material[]>>('/materials', { params })
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

  exportFile: async (format: 'csv' | 'xlsx' = 'xlsx'): Promise<AxiosResponse<Blob>> => {
    return axiosInstance.get('/materials/export', {
      params: { format },
      responseType: 'blob',
    })
  },

  importFile: async (file: File) => {
    const formData = new FormData()
    formData.append('file', file)
    const { data } = await axiosInstance.post<AjaxResult<MaterialImportResult>>('/materials/import', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    })
    return unwrap(data)
  },

  downloadImportErrorFile: async (token: string): Promise<AxiosResponse<Blob>> => {
    return axiosInstance.get(`/materials/import-errors/${token}`, {
      responseType: 'blob',
    })
  },
}
