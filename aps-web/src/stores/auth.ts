import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '../api/auth'
import type { User, LoginRequest } from '../types/auth'

export const useAuthStore = defineStore('auth', () => {
  // 状态
  const user = ref<User | null>(null)
  const loading = ref(false)

  // 计算属性
  const isAuthenticated = computed(() => user.value !== null)

  const hasRole = (role: string) => {
    return user.value?.roles.some(item => {
      if (typeof item === 'string') {
        return item === role
      }
      return item.name === role
    }) || false
  }

  const hasPermission = (permission: string) => {
    return user.value?.permissions.includes(permission) || false
  }

  // 操作
  async function login(request: LoginRequest) {
    loading.value = true
    try {
      const response = await authApi.login(request)
      user.value = response.user
      return response
    } finally {
      loading.value = false
    }
  }

  async function logout() {
    loading.value = true
    try {
      await authApi.logout()
    } finally {
      user.value = null
      loading.value = false
    }
  }

  async function fetchCurrentUser() {
    loading.value = true
    try {
      user.value = await authApi.getCurrentUser()
    } catch (error) {
      user.value = null
      throw error
    } finally {
      loading.value = false
    }
  }

  async function initialize() {
    try {
      await fetchCurrentUser()
    } catch {
      // 初始化失败，用户未登录
      user.value = null
    }
  }

  return {
    user,
    loading,
    isAuthenticated,
    hasRole,
    hasPermission,
    login,
    logout,
    fetchCurrentUser,
    initialize
  }
})
