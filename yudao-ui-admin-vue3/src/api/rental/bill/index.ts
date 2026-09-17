import request from '@/config/axios'

export interface RentBillVO {
  id?: number
  billNo?: string
  contractId?: number
  tenantUserId?: number
  houseId?: number
  billType?: number
  periodStart?: Date
  periodEnd?: Date
  rentAmount?: number
  depositAmount?: number
  propertyFeeAmount?: number
  totalAmount?: number
  paidAmount?: number
  payStatus?: number
  dueDate?: Date
  payTime?: Date
  payMethod?: string
  payee?: number
  feeDetail?: string
  lateFee?: number
  lateFeeWaived?: number
  lateFeeWaiveReason?: string
  createTime?: Date
}

// 查询租金账单分页
export const getRentBillPage = (params: PageParam) => {
  return request.get({ url: '/rental/rent-bill/page', params })
}

// 查询租金账单详情
export const getRentBill = (id: number) => {
  return request.get({ url: '/rental/rent-bill/get?id=' + id })
}

// 新增租金账单
export const createRentBill = (data: RentBillVO) => {
  return request.post({ url: '/rental/rent-bill/create', data })
}

// 修改租金账单
export const updateRentBill = (data: RentBillVO) => {
  return request.put({ url: '/rental/rent-bill/update', data })
}

// 删除租金账单
export const deleteRentBill = (id: number) => {
  return request.delete({ url: '/rental/rent-bill/delete?id=' + id })
}

// 租金账单缴费登记
export const payRentBill = (id: number, payMethod: string, transactionNo?: string, remark?: string) => {
  return request.put({ url: '/rental/rent-bill/pay', params: { id, payMethod, transactionNo, remark } })
}
