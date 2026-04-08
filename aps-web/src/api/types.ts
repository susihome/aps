// API 统一响应格式
export interface AjaxResult<T = any> {
  code: number
  message: string
  data: T | null
}

// 分页响应
export interface PageResult<T> {
  items: T[]
  total: number
  page: number
  pageSize: number
}

// 用户相关类型
export interface User {
  id: string
  username: string
  email: string
  enabled: boolean
  roles: Role[]
  createTime: string
  updateTime: string
}

export interface Role {
  id: string
  name: string
  description: string
}

export interface LoginRequest {
  username: string
  password: string
}

export interface LoginResponse {
  user: User
  expiresIn: number
}

// 工单相关类型
export interface Order {
  id: string
  orderNo: string
  productCode: string
  productName: string
  quantity: number
  priority: OrderPriority
  status: OrderStatus
  dueDate: string
  createTime: string
  updateTime: string
}

export enum OrderPriority {
  URGENT = 'URGENT',
  HIGH = 'HIGH',
  NORMAL = 'NORMAL',
  LOW = 'LOW'
}

export enum OrderStatus {
  PENDING = 'PENDING',
  SCHEDULED = 'SCHEDULED',
  IN_PROGRESS = 'IN_PROGRESS',
  COMPLETED = 'COMPLETED',
  CANCELLED = 'CANCELLED'
}

// 排产相关类型
export interface Schedule {
  id: string
  name: string
  status: string
  scheduleStartTime: string
  scheduleEndTime: string
  score: string
  createTime: string
  updateTime: string
}

export interface Assignment {
  id: string
  operationId: string
  resourceId: string
  startTime: string
  endTime: string
}
