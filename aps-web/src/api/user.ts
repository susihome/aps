import axiosInstance from './axios'
import type { User } from '../types/auth'

interface AjaxResult<T> {
  code: number
  message: string
  data: T | null
}

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

export const userApi = {
  /**
   * 获取用户列表
   */
  async list(): Promise<User[]> {
    const response = await axiosInstance.get<AjaxResult<User[]>>('/users')
    if (response.data.code !== 200 || !response.data.data) {
      throw new Error(response.data.message || '获取用户列表失败')
    }
    return response.data.data
  },

  /**
   * 获取用户详情
   */
  async getById(id: string): Promise<User> {
    const response = await axiosInstance.get<AjaxResult<User>>(`/users/${id}`)
    if (response.data.code !== 200 || !response.data.data) {
      throw new Error(response.data.message || '获取用户详情失败')
    }
    return response.data.data
  },

  /**
   * 创建用户
   */
  async create(data: CreateUserRequest): Promise<User> {
    const response = await axiosInstance.post<AjaxResult<User>>('/users', data)
    if (response.data.code !== 200 || !response.data.data) {
      throw new Error(response.data.message || '创建用户失败')
    }
    return response.data.data
  },

  /**
   * 更新用户
   */
  async update(id: string, data: UpdateUserRequest): Promise<User> {
    const response = await axiosInstance.put<AjaxResult<User>>(`/users/${id}`, data)
    if (response.data.code !== 200 || !response.data.data) {
      throw new Error(response.data.message || '更新用户失败')
    }
    return response.data.data
  },

  /**
   * 删除用户
   */
  async delete(id: string): Promise<void> {
    const response = await axiosInstance.delete<AjaxResult<void>>(`/users/${id}`)
    if (response.data.code !== 200) {
      throw new Error(response.data.message || '删除用户失败')
    }
  }
}
