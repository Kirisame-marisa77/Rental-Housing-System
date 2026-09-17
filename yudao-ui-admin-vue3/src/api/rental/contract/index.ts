import request from '@/config/axios'

export interface ContractVO {
  id?: number
  contractNo?: string
  houseId?: number
  tenantUserId?: number
  rentStartDate?: Date
  rentEndDate?: Date
  monthlyRent?: number
  depositAmount?: number
  paymentMethod?: string
  propertyFeeUnit?: number
  status?: number
  signTime?: Date
  ownerSignTime?: Date
  sourceApplyId?: number
  templateId?: number
  content?: string
  createTime?: Date
  // ===== 以下为后端补全信息，非数据库字段 =====
  firstBillNo?: string
  firstBillPaid?: boolean
  houseNo?: string
  communityName?: string
  area?: string
  buildingNo?: string
  roomNo?: string
  layout?: string
  squareArea?: number
  ownerId?: number
  ownerName?: string
  ownerPhone?: string
  tenantName?: string
  tenantPhone?: string
}

// 查询合同分页
export const getContractPage = (params: PageParam) => {
  return request.get({ url: '/rental/contract/page', params })
}

// 查询合同详情
export const getContract = (id: number) => {
  return request.get({ url: '/rental/contract/get?id=' + id })
}

// 新增合同
export const createContract = (data: ContractVO) => {
  return request.post({ url: '/rental/contract/create', data })
}

// 修改合同
export const updateContract = (data: ContractVO) => {
  return request.put({ url: '/rental/contract/update', data })
}

// 删除合同
export const deleteContract = (id: number) => {
  return request.delete({ url: '/rental/contract/delete?id=' + id })
}

// 租约续签
export const renewContract = (id: number, newEndDate: string) => {
  return request.put({ url: '/rental/contract/renew', params: { id, newEndDate } })
}

// 取消合同
export const cancelContract = (id: number) => {
  return request.put({ url: '/rental/contract/cancel?id=' + id })
}

// 查询即将到期的合同
export const getExpiringContractList = (days = 60) => {
  return request.get<ContractVO[]>({ url: '/rental/contract/get-expiring?days=' + days })
}
