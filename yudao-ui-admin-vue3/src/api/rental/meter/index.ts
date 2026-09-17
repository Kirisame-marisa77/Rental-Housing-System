import request from '@/config/axios'

export interface MeterReadingVO {
  id?: number
  houseId?: number
  contractId?: number
  meterType?: number
  lastReading?: number
  currentReading?: number
  usageAmount?: number
  unitPrice?: number
  feeAmount?: number
  readingDate?: Date
  operatorId?: number
  billId?: number
  status?: number
  remark?: string
  images?: string
  reviewStatus?: number
  reviewReason?: string
  reviewerId?: number
  reviewTime?: Date
  ownerId?: number
  valleyReading?: number
  lastValleyReading?: number
  valleyUsage?: number
  peakPrice?: number
  valleyPrice?: number
  createTime?: Date
}

export interface MeterSettingsVO {
  waterPrice?: number
  electricityPrice?: number
  meterPeriod?: string
}

// 查询抄表记录分页
export const getMeterReadingPage = (params: PageParam) => {
  return request.get({ url: '/rental/meter/page', params })
}

// 查询抄表记录详情
export const getMeterReading = (id: number) => {
  return request.get({ url: '/rental/meter/get?id=' + id })
}

// 查询最新抄表记录
export const getLatestMeterReading = (houseId: number, contractId: number, meterType: number) => {
  return request.get<MeterReadingVO>({
    url: '/rental/meter/get-latest',
    params: { houseId, contractId, meterType }
  })
}

// 新增抄表记录
export const createMeterReading = (data: MeterReadingVO) => {
  return request.post({ url: '/rental/meter/create', data })
}

// 修改抄表记录
export const updateMeterReading = (data: MeterReadingVO) => {
  return request.put({ url: '/rental/meter/update', data })
}

// 删除抄表记录
export const deleteMeterReading = (id: number) => {
  return request.delete({ url: '/rental/meter/delete?id=' + id })
}

// 确认抄表并生成水电费账单
export const confirmMeterReading = (id: number) => {
  return request.put({ url: '/rental/meter/confirm?id=' + id })
}

// 审核抄表（业主上传）
export const reviewMeterReading = (id: number, pass: boolean, reason?: string) => {
  return request.put({ url: '/rental/meter/review', params: { id, pass, reason } })
}

// 查询抄表配置
export const getMeterSettings = () => {
  return request.get<MeterSettingsVO>({ url: '/rental/meter/get-settings' })
}

// 更新抄表配置
export const updateMeterSettings = (data: MeterSettingsVO) => {
  return request.put({ url: '/rental/meter/update-settings', data })
}
