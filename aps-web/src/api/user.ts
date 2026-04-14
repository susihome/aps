import axiosInstance from './axios'
import type { AjaxResult } from './types'
import type { User } from '../types/auth'

export interface CreateUserRequest {
  username: string
  password: string
  email: string
  roleIds: string[]
}

export interface UpdateUserRequest {
  email?: string
  enabled?: boolean
  roleIds?: string[]
}

export interface ResetUserPasswordRequest {
  newPassword: string
}

interface BackendPageResult<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
  size: number
}

export interface UserPageResult<T> {
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

function toPageResult<T>(data: BackendPageResult<T>): UserPageResult<T> {
  return {
    items: data.content,
    total: data.totalElements,
    pageNo: data.number + 1,
    pageSize: data.size,
    totalPages: data.totalPages,
  }
}

export const userApi = {
  async list(params: { pageNo: number; pageSize: number; keyword?: string; enabled?: boolean }): Promise<UserPageResult<User>> {
    const { pageNo, pageSize, keyword, enabled } = params
    const response = await axiosInstance.get<AjaxResult<BackendPageResult<User>>>('/users', {
      params: {
        page: pageNo - 1,
        size: pageSize,
        keyword: keyword || undefined,
        enabled,
      },
    })
    return toPageResult(unwrap(response.data))
  },

  async getById(id: string): Promise<User> {
    const response = await axiosInstance.get<AjaxResult<User>>(`/users/${id}`)
    return unwrap(response.data)
  },

  async create(data: CreateUserRequest): Promise<User> {
    const response = await axiosInstance.post<AjaxResult<User>>('/users', data)
    return unwrap(response.data)
  },

  async update(id: string, data: UpdateUserRequest): Promise<User> {
    const response = await axiosInstance.put<AjaxResult<User>>(`/users/${id}`, data)
    return unwrap(response.data)
  },

  async delete(id: string): Promise<void> {
    const response = await axiosInstance.delete<AjaxResult<void>>(`/users/${id}`)
    if (response.data.code !== 200) {
      throw new Error(response.data.message || '删除用户失败')
    }
  },

  async resetPassword(id: string, data: ResetUserPasswordRequest): Promise<void> {
    const response = await axiosInstance.post<AjaxResult<void>>(`/users/${id}/reset-password`, data)
    if (response.data.code !== 200) {
      throw new Error(response.data.message || '重置密码失败')
    }
  },
}
