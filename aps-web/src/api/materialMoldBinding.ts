import axiosInstance from './axios'
import type { AjaxResult } from './types'

export interface MaterialMoldBinding {
  id: string
  materialId: string
  materialCode: string
  materialName: string
  moldId: string
  moldCode: string
  moldName: string
  priority: number
  isDefault: boolean
  isPreferred: boolean
  cycleTimeMinutes: number | null
  setupTimeMinutes: number | null
  changeoverTimeMinutes: number | null
  enabled: boolean
  validFrom: string | null
  validTo: string | null
  remark: string | null
  createTime: string
  updateTime: string
}

export interface CreateMaterialMoldBindingRequest {
  materialId: string
  moldId: string
  priority?: number
  isDefault?: boolean
  isPreferred?: boolean
  cycleTimeMinutes?: number | null
  setupTimeMinutes?: number | null
  changeoverTimeMinutes?: number | null
  enabled?: boolean
  validFrom?: string | null
  validTo?: string | null
  remark?: string | null
}

export interface UpdateMaterialMoldBindingRequest {
  priority?: number
  isDefault?: boolean
  isPreferred?: boolean
  cycleTimeMinutes?: number | null
  setupTimeMinutes?: number | null
  changeoverTimeMinutes?: number | null
  enabled?: boolean
  validFrom?: string | null
  validTo?: string | null
  remark?: string | null
}

function unwrap<T>(result: AjaxResult<T>): T {
  if (result.code !== 200 || result.data == null) {
    throw new Error(result.message || '请求失败')
  }
  return result.data
}

export const materialMoldBindingApi = {
  getAll: async (materialId?: string, moldId?: string) => {
    const params: Record<string, string> = {}
    if (materialId) params.materialId = materialId
    if (moldId) params.moldId = moldId
    const { data } = await axiosInstance.get<AjaxResult<MaterialMoldBinding[]>>(
      '/material-mold-bindings',
      { params },
    )
    return unwrap(data)
  },

  getById: async (id: string) => {
    const { data } = await axiosInstance.get<AjaxResult<MaterialMoldBinding>>(
      `/material-mold-bindings/${id}`,
    )
    return unwrap(data)
  },

  create: async (form: CreateMaterialMoldBindingRequest) => {
    const { data } = await axiosInstance.post<AjaxResult<MaterialMoldBinding>>(
      '/material-mold-bindings',
      form,
    )
    return unwrap(data)
  },

  update: async (id: string, form: UpdateMaterialMoldBindingRequest) => {
    const { data } = await axiosInstance.put<AjaxResult<MaterialMoldBinding>>(
      `/material-mold-bindings/${id}`,
      form,
    )
    return unwrap(data)
  },

  delete: async (id: string) => {
    const { data } = await axiosInstance.delete<AjaxResult<null>>(
      `/material-mold-bindings/${id}`,
    )
    if (data.code !== 200) {
      throw new Error(data.message || '请求失败')
    }
  },
}
