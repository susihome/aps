import axiosInstance from './axios'

interface AjaxResult<T> {
  code: number
  message: string
  data: T | null
}

export interface Order {
  id: string
  orderNo: string
  productCode: string
  productName: string
  quantity: number
  priority: string
  status: string
  dueDate: string
  createTime?: string
  updateTime?: string
}

export interface CreateOrderRequest {
  orderNo: string
  productCode: string
  productName: string
  quantity: number
  priority: string
  dueDate: string
}

export const orderApi = {
  /**
   * 获取工单列表
   */
  async list(): Promise<Order[]> {
    const response = await axiosInstance.get<AjaxResult<Order[]>>('/orders')
    if (response.data.code !== 200 || !response.data.data) {
      throw new Error(response.data.message || '获取工单列表失败')
    }
    return response.data.data
  },

  /**
   * 获取工单详情
   */
  async getById(id: string): Promise<Order> {
    const response = await axiosInstance.get<AjaxResult<Order>>(`/orders/${id}`)
    if (response.data.code !== 200 || !response.data.data) {
      throw new Error(response.data.message || '获取工单详情失败')
    }
    return response.data.data
  },

  /**
   * 创建工单
   */
  async create(data: CreateOrderRequest): Promise<Order> {
    const response = await axiosInstance.post<AjaxResult<Order>>('/orders', data)
    if (response.data.code !== 200 || !response.data.data) {
      throw new Error(response.data.message || '创建工单失败')
    }
    return response.data.data
  },

  /**
   * 删除工单
   */
  async delete(id: string): Promise<void> {
    const response = await axiosInstance.delete<AjaxResult<void>>(`/orders/${id}`)
    if (response.data.code !== 200) {
      throw new Error(response.data.message || '删除工单失败')
    }
  }
}
