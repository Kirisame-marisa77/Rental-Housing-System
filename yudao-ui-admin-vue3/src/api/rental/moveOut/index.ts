import request from '@/config/axios'

export interface MoveOutApplicationVO {
  id?: number
  applyNo?: string
  contractId?: number
  tenantUserId?: number
  houseId?: number
  moveOutType?: number
  moveOutReason?: string
  expectedMoveOutDate?: Date
  settlementId?: number
  status?: number
  handlerId?: number
  handleTime?: Date
  // 0-验收通过 1-轻微损坏 2-严重损坏（库里是 TINYINT）
  inspectionResult?: number
  repairFee?: number
  repairFeeDesc?: string
  remark?: string
  depositHandle?: string
  remainingRent?: number
  deductionAmount?: number
  refundOrPay?: number
  createTime?: Date
}

export interface MoveOutConfirmVO {
  id: number
  // 0-验收通过 1-轻微损坏 2-严重损坏
  inspectionResult?: number
  repairFee?: number
  repairFeeDesc?: string
  propertyFeeArrears?: number
  utilityFeeArrears?: number
  remainingRent?: number
  remark?: string
}

export interface SettlementBillVO {
  id?: number
  settlementNo?: string
  contractId?: number
  houseId?: number
  tenantUserId?: number
  moveOutApplicationId?: number
  moveOutType?: number
  depositAmount?: number
  depositHandle?: string
  remainingRent?: number
  propertyFeeArrears?: number
  utilityFeeArrears?: number
  repairFee?: number
  deductionAmount?: number
  refundOrPay?: number
  handlerId?: number
  handleTime?: Date
  remark?: string
  createTime?: Date
}

// 查询退租申请分页
export const getMoveOutApplicationPage = (params: PageParam) => {
  return request.get({ url: '/rental/move-out/page', params })
}

// 查询退租申请详情
export const getMoveOutApplication = (id: number) => {
  return request.get({ url: '/rental/move-out/get?id=' + id })
}

// 新增退租申请
export const createMoveOutApplication = (data: MoveOutApplicationVO) => {
  return request.post({ url: '/rental/move-out/create', data })
}

// 修改退租申请
export const updateMoveOutApplication = (data: MoveOutApplicationVO) => {
  return request.put({ url: '/rental/move-out/update', data })
}

// 删除退租申请
export const deleteMoveOutApplication = (id: number) => {
  return request.delete({ url: '/rental/move-out/delete?id=' + id })
}

// 处理退租申请（房屋验收 + 费用结算）
export const confirmMoveOutApplication = (data: MoveOutConfirmVO) => {
  return request.put({ url: '/rental/move-out/confirm', data })
}

// 查询退租结算单分页
export const getSettlementBillPage = (params: PageParam) => {
  return request.get({ url: '/rental/settlement-bill/page', params })
}

// 查询退租结算单详情
export const getSettlementBill = (id: number) => {
  return request.get({ url: '/rental/settlement-bill/get?id=' + id })
}
