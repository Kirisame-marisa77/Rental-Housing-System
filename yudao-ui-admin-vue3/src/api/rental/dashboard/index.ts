import request from '@/config/axios'

export interface DashboardOverviewVO {
  houseCount?: number
  rentedCount?: number
  onSaleCount?: number
  offSaleCount?: number
  pendingApplyCount?: number
  pendingRepairCount?: number
  pendingBillCount?: number
  expiringContractCount?: number
}

export interface DashboardDistributionVO {
  area?: string
  count?: number
}

// 获得首页综合数据
export const getOverview = () => {
  return request.get<DashboardOverviewVO>({ url: '/rental/dashboard/get-overview' })
}

// 获得片区房源分布
export const getDistribution = () => {
  return request.get<DashboardDistributionVO[]>({ url: '/rental/dashboard/get-distribution' })
}
