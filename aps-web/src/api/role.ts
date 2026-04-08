import axiosInstance from './axios'

export interface Role {
  id: string
  name: string
  description?: string
  permissions?: Permission[]
  userCount?: number
  createTime?: string
  updateTime?: string
}

export interface Permission {
  id: string
  code: string
  name: string
  type: string
}

export interface RoleForm {
  name: string
  description?: string
  permissionIds?: string[]
}

const API_BASE = '/roles'

interface AjaxResult<T> {
  code: number
  message: string
  data: T | null
}

interface PageResult<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}

function unwrap<T>(result: AjaxResult<T>): T {
  if (result.code !== 200 || result.data == null) {
    throw new Error(result.message || '请求失败')
  }
  return result.data
}

export const useRoleApi = () => {
  return {
    // 获取角色列表（分页）
    getRoles: async (page = 0, size = 20) => {
      const { data } = await axiosInstance.get<AjaxResult<PageResult<Role>>>(
        `${API_BASE}?page=${page}&size=${size}`
      )
      return unwrap(data)
    },

    // 获取所有角色（不分页）
    getAllRoles: async () => {
      const { data } = await axiosInstance.get<AjaxResult<PageResult<Role>>>(
        `${API_BASE}?page=0&size=1000`
      )
      return unwrap(data).content
    },

    // 获取单个角色
    getRole: async (id: string) => {
      const { data } = await axiosInstance.get<AjaxResult<Role>>(`${API_BASE}/${id}`)
      return unwrap(data)
    },

    // 创建角色
    createRole: async (form: RoleForm) => {
      const { data } = await axiosInstance.post<AjaxResult<Role>>(API_BASE, form)
      return unwrap(data)
    },

    // 更新角色
    updateRole: async (id: string, form: Partial<RoleForm>) => {
      const { data } = await axiosInstance.put<AjaxResult<Role>>(`${API_BASE}/${id}`, form)
      return unwrap(data)
    },

    // 删除角色
    deleteRole: async (id: string) => {
      const { data } = await axiosInstance.delete<AjaxResult<null>>(`${API_BASE}/${id}`)
      unwrap(data)
    },

    // 批量删除角色
    deleteRoles: async (ids: string[]) => {
      const { data } = await axiosInstance.delete<AjaxResult<null>>(`${API_BASE}/batch`, {
        data: ids
      })
      unwrap(data)
    },

    // 分配权限
    assignPermissions: async (id: string, permissionIds: string[]) => {
      const { data } = await axiosInstance.post<AjaxResult<Role>>(
        `${API_BASE}/${id}/permissions`,
        permissionIds
      )
      return unwrap(data)
    },

    // 获取角色权限
    getRolePermissions: async (id: string) => {
      const { data } = await axiosInstance.get<AjaxResult<string[]>>(
        `${API_BASE}/${id}/permissions`
      )
      return unwrap(data)
    },

    // 批量分配权限
    assignPermissionsToRoles: async (roleIds: string[], permissionIds: string[]) => {
      const { data } = await axiosInstance.post<AjaxResult<null>>(
        `${API_BASE}/batch/permissions`,
        { roleIds, permissionIds }
      )
      unwrap(data)
    }
  }
}
