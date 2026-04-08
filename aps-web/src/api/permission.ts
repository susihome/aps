import axiosInstance from './axios'

export interface Permission {
  id: string
  code: string
  name: string
  description?: string
  type: 'catalog' | 'menu' | 'button'
  routePath?: string
  icon: string
  sort: number
  enabled: boolean
  visible: boolean
  parentId?: string
  children?: Permission[]
}

export interface PermissionForm {
  code: string
  name: string
  description?: string
  type: 'catalog' | 'menu' | 'button'
  routePath?: string
  icon: string
  sort: number
  enabled: boolean
  visible: boolean
  parentId?: string
}

const API_BASE = '/permissions'

interface AjaxResult<T> {
  code: number
  message: string
  data: T | null
}

interface RawPermission {
  id: string
  code: string
  name: string
  description?: string
  type: string
  routePath?: string
  icon: string
  sort: number
  enabled: boolean
  visible: boolean
  parent?: {
    id: string
  }
  parentId?: string
  children?: RawPermission[]
}

function normalizeType(type: string): 'catalog' | 'menu' | 'button' {
  const normalized = type.toLowerCase()
  if (normalized === 'catalog' || normalized === 'menu' || normalized === 'button') {
    return normalized
  }
  return 'button'
}

function normalizePermission(permission: RawPermission): Permission {
  return {
    id: permission.id,
    code: permission.code,
    name: permission.name,
    description: permission.description,
    type: normalizeType(permission.type),
    routePath: permission.routePath,
    icon: permission.icon,
    sort: permission.sort,
    enabled: permission.enabled,
    visible: permission.visible,
    parentId: permission.parent?.id ?? permission.parentId,
    children: Array.isArray(permission.children)
      ? permission.children.map(normalizePermission)
      : []
  }
}

function unwrap<T>(result: AjaxResult<T>): T {
  if (result.code !== 200 || result.data == null) {
    throw new Error(result.message || '请求失败')
  }
  return result.data
}

export const usePermissionApi = () => {
  return {
    // 获取权限树
    getPermissionTree: async () => {
      const { data } = await axiosInstance.get<AjaxResult<RawPermission[]>>(`${API_BASE}/tree`)
      return unwrap(data).map(normalizePermission)
    },

    // 获取单个权限
    getPermission: async (id: string) => {
      const { data } = await axiosInstance.get<AjaxResult<RawPermission>>(`${API_BASE}/${id}`)
      return normalizePermission(unwrap(data))
    },

    // 创建权限
    createPermission: async (form: PermissionForm) => {
      const payload = {
        ...form,
        type: form.type.toUpperCase()
      }
      const { data } = await axiosInstance.post<AjaxResult<RawPermission>>(API_BASE, payload)
      return normalizePermission(unwrap(data))
    },

    // 更新权限
    updatePermission: async (id: string, form: Partial<PermissionForm>) => {
      const payload = {
        ...form,
        type: form.type ? form.type.toUpperCase() : undefined
      }
      const { data } = await axiosInstance.put<AjaxResult<RawPermission>>(`${API_BASE}/${id}`, payload)
      return normalizePermission(unwrap(data))
    },

    // 删除权限
    deletePermission: async (id: string) => {
      const { data } = await axiosInstance.delete<AjaxResult<null>>(`${API_BASE}/${id}`)
      unwrap(data)
    },

    // 批量更新排序
    updateSort: async (updates: Array<{ id: string; sort: number }>) => {
      const { data } = await axiosInstance.post<AjaxResult<null>>(`${API_BASE}/batch-sort`, { updates })
      unwrap(data)
    },

    // 启用/禁用权限
    togglePermission: async (id: string, enabled: boolean) => {
      const { data } = await axiosInstance.patch<AjaxResult<RawPermission>>(`${API_BASE}/${id}/toggle`, { enabled })
      return normalizePermission(unwrap(data))
    }
  }
}
