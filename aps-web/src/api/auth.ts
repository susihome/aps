import axiosInstance from './axios'
import type { LoginRequest, LoginResponse, User } from '../types/auth'

interface AjaxResult<T> {
  code: number
  message: string
  data: T | null
}

export const authApi = {
  /**
   * 用户登录
   */
  async login(request: LoginRequest): Promise<LoginResponse> {
    const response = await axiosInstance.post<AjaxResult<LoginResponse>>('/auth/login', request)
    if (response.data.code !== 200 || !response.data.data) {
      throw new Error(response.data.message || '登录失败')
    }
    return response.data.data
  },

  /**
   * 用户登出
   */
  async logout(): Promise<void> {
    const response = await axiosInstance.post<AjaxResult<void>>('/auth/logout')
    if (response.data.code !== 200) {
      throw new Error(response.data.message || '登出失败')
    }
  },

  /**
   * 刷新令牌
   */
  async refreshToken(): Promise<LoginResponse> {
    const response = await axiosInstance.post<AjaxResult<LoginResponse>>('/auth/refresh')
    if (response.data.code !== 200 || !response.data.data) {
      throw new Error(response.data.message || '刷新令牌失败')
    }
    return response.data.data
  },

  /**
   * 获取当前用户信息
   */
  async getCurrentUser(): Promise<User> {
    const response = await axiosInstance.get<AjaxResult<User>>('/auth/me')
    if (response.data.code !== 200 || !response.data.data) {
      throw new Error(response.data.message || '获取用户信息失败')
    }
    return response.data.data
  }
}
