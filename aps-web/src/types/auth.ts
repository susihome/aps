export interface Role {
  id: string
  name: string
  description?: string
}

export interface User {
  id: string
  username: string
  email: string
  roles: Array<Role | string>
  permissions: string[]
  enabled: boolean
  createTime: string
  updateTime: string
  lastLoginAt?: string
}

export interface LoginRequest {
  username: string
  password: string
}

export interface LoginResponse {
  user: User
  expiresIn: number
}

export type UserRole = 'ADMIN' | 'PLANNER' | 'SUPERVISOR'
