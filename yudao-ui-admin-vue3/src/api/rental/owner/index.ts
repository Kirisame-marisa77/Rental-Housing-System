import request from '@/config/axios'

export interface OwnerInfoVO {
  id?: number
  userId?: number
  name?: string
  idCard?: string
  bankCard?: string
  phone: string
  password?: string
  gender?: number
  emergencyContact?: string
  emergencyPhone?: string
  remark?: string
  createTime?: Date
}

// 查询业主分页
export const getOwnerInfoPage = (params: PageParam) => {
  return request.get({ url: '/rental/owner/page', params })
}

// 查询业主详情
export const getOwnerInfo = (id: number) => {
  return request.get({ url: '/rental/owner/get?id=' + id })
}

// 新增业主
export const createOwnerInfo = (data: OwnerInfoVO) => {
  return request.post({ url: '/rental/owner/create', data })
}

// 修改业主
export const updateOwnerInfo = (data: OwnerInfoVO) => {
  return request.put({ url: '/rental/owner/update', data })
}

// 删除业主
export const deleteOwnerInfo = (id: number) => {
  return request.delete({ url: '/rental/owner/delete?id=' + id })
}
