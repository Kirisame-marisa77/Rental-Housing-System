import request from '@/config/axios'

export interface ViewingAppointmentVO {
  id?: number
  houseId?: number
  tenantUserId?: number
  appointmentDate?: Date
  startTime?: string
  endTime?: string
  status?: number
  feedback?: string
  createTime?: Date
  houseNo?: string
  communityName?: string
  buildingNo?: string
  roomNo?: string
  layout?: string
  squareArea?: number
  monthlyRent?: number
  houseStatus?: number
  ownerId?: number
  ownerName?: string
  ownerPhone?: string
  tenantName?: string
  tenantPhone?: string
}

// 查询看房预约分页（管理端只读监管）
export const getViewingAppointmentPage = (params: PageParam) => {
  return request.get({ url: '/rental/viewing-appointment/page', params })
}

// 查询看房预约详情
export const getViewingAppointment = (id: number) => {
  return request.get({ url: '/rental/viewing-appointment/get?id=' + id })
}
