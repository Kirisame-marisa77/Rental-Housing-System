import request from '@/config/axios'

export interface HouseVO {
  id?: number
  houseNo?: string
  communityName: string
  area: string
  buildingNo: string
  roomNo: string
  layout: string
  squareArea?: number
  orientation?: string
  floor?: number
  totalFloor?: number
  decoration?: string
  monthlyRent?: number
  deposit?: number
  facilities?: string
  status?: number
  reviewStatus?: number
  reviewReason?: string
  ownerId?: number
  waterBillType?: number
  waterUnitPrice?: number
  waterTier1Limit?: number
  waterTier1Price?: number
  waterTier2Limit?: number
  waterTier2Price?: number
  waterTier3Price?: number
  electricityPeakPrice?: number
  electricityValleyPrice?: number
  onlineTime?: Date
  description?: string
  createTime?: Date
}

// 查询房源分页
export const getHousePage = (params: PageParam) => {
  return request.get({ url: '/rental/house/page', params })
}

// 查询房源详情
export const getHouse = (id: number) => {
  return request.get({ url: '/rental/house/get?id=' + id })
}

// 新增房源
export const createHouse = (data: HouseVO) => {
  return request.post({ url: '/rental/house/create', data })
}

// 修改房源
export const updateHouse = (data: HouseVO) => {
  return request.put({ url: '/rental/house/update', data })
}

// 删除房源
export const deleteHouse = (id: number) => {
  return request.delete({ url: '/rental/house/delete?id=' + id })
}

// 审核房源
export const reviewHouse = (id: number, pass: boolean, reason?: string) => {
  return request.put({ url: '/rental/house/review', params: { id, pass, reason } })
}

// ========== 房源图片 ==========
export interface HouseImageVO {
  id?: number
  houseId?: number
  imageUrl: string
  sort?: number
  imageType?: number
}

// 查询房源图片列表
export const getHouseImageList = (houseId: number) => {
  return request.get<HouseImageVO[]>({ url: '/rental/house-image/list?houseId=' + houseId })
}

// 保存房源图片（整体覆盖）
export const saveHouseImage = (data: { houseId: number; realityImages?: string[]; layoutImages?: string[] }) => {
  return request.post({ url: '/rental/house-image/save', data })
}
