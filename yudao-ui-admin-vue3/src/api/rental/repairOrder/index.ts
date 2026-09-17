import request from '@/config/axios'

export interface RepairOrderVO {
  id?: number
  orderNo?: string
  tenantUserId?: number
  houseId?: number
  repairType?: string
  description?: string
  images?: string
  expectedTime?: Date
  status?: number
  priority?: number
  repairerId?: number
  assignTime?: Date
  completeTime?: Date
  repairDescription?: string
  handleEvidence?: string
  handleTime?: Date
  reviewReason?: string
  ownerId?: number
  rating?: number
  evaluationContent?: string
  evaluationTime?: Date
  createTime?: Date
}

export interface RepairProgressVO {
  id?: number
  orderId?: number
  operatorId?: number
  operatorType?: number
  actionType?: string
  description?: string
  createTime?: Date
}

// 查询维修工单分页
export const getRepairOrderPage = (params: PageParam) => {
  return request.get({ url: '/rental/repair-order/page', params })
}

// 查询维修工单详情
export const getRepairOrder = (id: number) => {
  return request.get({ url: '/rental/repair-order/get?id=' + id })
}

// 新增维修工单
export const createRepairOrder = (data: RepairOrderVO) => {
  return request.post({ url: '/rental/repair-order/create', data })
}

// 修改维修工单
export const updateRepairOrder = (data: RepairOrderVO) => {
  return request.put({ url: '/rental/repair-order/update', data })
}

// 删除维修工单
export const deleteRepairOrder = (id: number) => {
  return request.delete({ url: '/rental/repair-order/delete?id=' + id })
}

// 分配维修人员
export const assignRepairOrder = (id: number, repairerId: number) => {
  return request.put({ url: '/rental/repair-order/assign', params: { id, repairerId } })
}

// 更新工单状态
export const updateRepairOrderStatus = (id: number, status: number) => {
  return request.put({ url: '/rental/repair-order/update-status', params: { id, status } })
}

// 维修完成
export const completeRepairOrder = (id: number, repairDescription?: string) => {
  return request.put({ url: '/rental/repair-order/complete', params: { id, repairDescription } })
}

// 业主处理维修并上传证据
export const handleRepairOrder = (id: number, ownerId: number, repairDescription?: string, handleEvidence?: string) => {
  return request.put({ url: '/rental/repair-order/handle', params: { id, ownerId, repairDescription, handleEvidence } })
}

// 管理员验收判定
export const reviewRepairOrder = (id: number, pass: boolean, reason?: string) => {
  return request.put({ url: '/rental/repair-order/review', params: { id, pass, reason } })
}

// 租客确认维修完成
export const confirmRepairOrder = (id: number) => {
  return request.put({ url: '/rental/repair-order/confirm', params: { id } })
}

// 查询维修进度列表
export const getRepairProgressList = (orderId: number) => {
  return request.get<RepairProgressVO[]>({ url: '/rental/repair-order/get-progress?orderId=' + orderId })
}
