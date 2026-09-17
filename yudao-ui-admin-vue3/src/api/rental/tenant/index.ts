import request from '@/config/axios'

export interface TenantInfoVO {
  id?: number
  userId?: number
  name?: string
  idCard?: string
  phone: string
  gender?: number
  emergencyContact?: string
  emergencyPhone?: string
  workUnit?: string
  authStatus?: number
  authFailCount?: number
  authTime?: Date
  remark?: string
  createTime?: Date
}

// 查询租客分页
export const getTenantInfoPage = (params: PageParam) => {
  return request.get({ url: '/rental/tenant/page', params })
}

// 查询租客详情
export const getTenantInfo = (id: number) => {
  return request.get({ url: '/rental/tenant/get?id=' + id })
}

// 新增租客
export const createTenantInfo = (data: TenantInfoVO) => {
  return request.post({ url: '/rental/tenant/create', data })
}

// 修改租客
export const updateTenantInfo = (data: TenantInfoVO) => {
  return request.put({ url: '/rental/tenant/update', data })
}

// 删除租客
export const deleteTenantInfo = (id: number) => {
  return request.delete({ url: '/rental/tenant/delete?id=' + id })
}
