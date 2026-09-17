import request from '@/config/axios'

export interface UtilityBillVO {
  id?: number
  billNo?: string
  contractId?: number
  tenantUserId?: number
  houseId?: number
  meterReadingId?: number
  feeType?: number
  waterAmount?: number
  electricityAmount?: number
  totalAmount?: number
  payStatus?: number
  dueDate?: Date
  payTime?: Date
  payMethod?: string
  payee?: number
  createTime?: Date
}

// 查询水电费账单分页
export const getUtilityBillPage = (params: PageParam) => {
  return request.get({ url: '/rental/utility-bill/page', params })
}

// 查询水电费账单详情
export const getUtilityBill = (id: number) => {
  return request.get({ url: '/rental/utility-bill/get?id=' + id })
}

// 新增水电费账单
export const createUtilityBill = (data: UtilityBillVO) => {
  return request.post({ url: '/rental/utility-bill/create', data })
}

// 修改水电费账单
export const updateUtilityBill = (data: UtilityBillVO) => {
  return request.put({ url: '/rental/utility-bill/update', data })
}

// 删除水电费账单
export const deleteUtilityBill = (id: number) => {
  return request.delete({ url: '/rental/utility-bill/delete?id=' + id })
}

// 水电费缴费登记
export const payUtilityBill = (data: {
  id: number
  payMethod: string
  payee?: number
  transactionNo?: string
  remark?: string
}) => {
  return request.put({ url: '/rental/utility-bill/pay', params: data })
}
