import request from '@/config/axios'

export interface PaymentRecordVO {
  id?: number
  billType?: number
  billId?: number
  payAmount?: number
  payMethod?: string
  payTime?: Date
  payee?: number
  transactionNo?: string
  voucherNo?: string
  remark?: string
  createTime?: Date
}

// 查询缴费记录分页
export const getPaymentRecordPage = (params: PageParam) => {
  return request.get({ url: '/rental/payment-record/page', params })
}

// 查询缴费记录详情
export const getPaymentRecord = (id: number) => {
  return request.get({ url: '/rental/payment-record/get?id=' + id })
}
