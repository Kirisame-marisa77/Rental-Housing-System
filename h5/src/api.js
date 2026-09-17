import axios from 'axios'
import { ElMessage } from 'element-plus'
import { getToken } from './auth'

/**
 * 两个 axios 实例，只有「携带哪个角色的 token」不同
 *
 * 不用「单实例 + 按 window.location.pathname 判断角色」：那会逼本文件引入 router，
 * 形成 router → views → api → router 的真循环；而且登录页没有角色前缀，
 * 恰恰是最需要发请求的时候。
 */
const makeRequest = (role) => {
  const request = axios.create({
    baseURL: '/admin-api',
    timeout: 15000
  })

  // 请求拦截：携带该角色的 token
  request.interceptors.request.use((config) => {
    const token = getToken(role)
    if (token) {
      config.headers.Authorization = 'Bearer ' + token
    }
    return config
  })

  // 响应拦截：解包 CommonResult
  request.interceptors.response.use(
    (response) => {
      const res = response.data
      if (res.code !== 0) {
        ElMessage.error(res.msg || '请求失败')
        return Promise.reject(new Error(res.msg))
      }
      return res.data
    },
    (error) => {
      ElMessage.error(error.response?.data?.msg || '网络错误')
      return Promise.reject(error)
    }
  )

  return request
}

const ownerRequest = makeRequest('owner')
const tenantRequest = makeRequest('tenant')

// ============================================================
// 业主端（/rental/owner-app/**）
// ============================================================

// ===== 登录 / 注册 =====
export const ownerLogin = async (phone, password) => {
  const d = await ownerRequest.post('/rental/owner-app/login', null, { params: { phone, password } })
  return { token: d.accessToken, userId: d.ownerId, name: d.name }
}
export const ownerRegister = async (data) => {
  const d = await ownerRequest.post('/rental/owner-app/register', data)
  return { token: d.accessToken, userId: d.ownerId, name: d.name }
}

// ===== 个人信息 =====
export const getOwnerInfo = () => ownerRequest.get('/rental/owner-app/get')
export const updateOwnerInfo = (data) => ownerRequest.put('/rental/owner-app/update', data)
export const changeOwnerPassword = (oldPassword, newPassword) =>
  ownerRequest.put('/rental/owner-app/update-password', null, { params: { oldPassword, newPassword } })

// ===== 房源 =====
export const getOwnerHouses = (params = {}) => ownerRequest.get('/rental/owner-app/house/page', { params })
export const createOwnerHouse = (data) => ownerRequest.post('/rental/owner-app/house/create', data)

// ===== 租房申请 =====
export const getOwnerApplies = (params = {}) => ownerRequest.get('/rental/owner-app/apply/page', { params })
export const approveOwnerApply = (id, pass, reason) =>
  ownerRequest.put('/rental/owner-app/apply/approve', null, { params: { id, pass, reason } })

// ===== 看房预约 =====
export const getOwnerAppointments = (params = {}) =>
  ownerRequest.get('/rental/owner-app/viewing-appointment/page', { params })
export const confirmOwnerAppointment = (id) =>
  ownerRequest.put('/rental/owner-app/viewing-appointment/confirm', null, { params: { id } })
export const completeOwnerAppointment = (id, feedback) =>
  ownerRequest.put('/rental/owner-app/viewing-appointment/complete', null, { params: { id, feedback } })

// ===== 抄表 =====
export const getOwnerMeters = (params = {}) => ownerRequest.get('/rental/owner-app/meter/page', { params })
export const createOwnerMeterReading = (data) => ownerRequest.post('/rental/owner-app/meter/create', data)

// ===== 维修 =====
export const getOwnerRepairs = (params = {}) => ownerRequest.get('/rental/owner-app/repair/page', { params })
export const handleOwnerRepairOrder = (id, repairDescription, handleEvidence) =>
  ownerRequest.put('/rental/owner-app/repair/handle', null, {
    params: { id, repairDescription, handleEvidence }
  })

// ===== 合同 =====
export const getOwnerContracts = () => ownerRequest.get('/rental/owner-app/contract/page')
export const getOwnerContract = (id) =>
  ownerRequest.get('/rental/owner-app/contract/get', { params: { id } })
export const signOwnerContract = (id) =>
  ownerRequest.put('/rental/owner-app/contract/sign', null, { params: { id } })

// ===== 我的账单（只读，按房源归属自动过滤，无需传 houseId）=====
export const getOwnerRentBills = (params = {}) =>
  ownerRequest.get('/rental/owner-app/rent-bill/page', { params })
export const getOwnerUtilityBills = (params = {}) =>
  ownerRequest.get('/rental/owner-app/utility-bill/page', { params })

// ============================================================
// 租客端（/rental/tenant-app/**）
// ============================================================

// ===== 登录 / 注册 =====
export const tenantLogin = async (phone, password) => {
  const d = await tenantRequest.post('/rental/tenant-app/login', null, { params: { phone, password } })
  return { token: d.accessToken, userId: d.tenantId, name: d.name }
}
export const tenantRegister = async (data) => {
  const d = await tenantRequest.post('/rental/tenant-app/register', data)
  return { token: d.accessToken, userId: d.tenantId, name: d.name }
}

// ===== 个人信息 =====
export const getTenantInfo = () => tenantRequest.get('/rental/tenant-app/get')
export const updateTenantInfo = (data) => tenantRequest.put('/rental/tenant-app/update', data)
export const changeTenantPassword = (oldPassword, newPassword) =>
  tenantRequest.put('/rental/tenant-app/update-password', null, { params: { oldPassword, newPassword } })

// ===== 房源 =====
export const getTenantHouses = (params = {}) => tenantRequest.get('/rental/tenant-app/house/page', { params })

// ===== 租房申请 =====
export const createTenantApply = (data) => tenantRequest.post('/rental/tenant-app/apply/create', data)
export const getTenantApplies = (params = {}) => tenantRequest.get('/rental/tenant-app/apply/page', { params })

// ===== 合同 =====
export const getTenantContracts = (params = {}) => tenantRequest.get('/rental/tenant-app/contract/page', { params })
export const getTenantContract = (id) =>
  tenantRequest.get('/rental/tenant-app/contract/get', { params: { id } })
export const signTenantContract = (id) =>
  tenantRequest.put('/rental/tenant-app/contract/sign', null, { params: { id } })

// ===== 退租申请 =====
export const createTenantMoveOut = (data) => tenantRequest.post('/rental/tenant-app/move-out/create', data)
export const getTenantMoveOuts = (params = {}) =>
  tenantRequest.get('/rental/tenant-app/move-out/page', { params })

// ===== 租金账单 =====
export const getTenantRentBills = (params = {}) => tenantRequest.get('/rental/tenant-app/rent-bill/page', { params })
export const payTenantRentBill = (id, payMethod) =>
  tenantRequest.put('/rental/tenant-app/rent-bill/pay', null, { params: { id, payMethod } })

// ===== 房源收藏 =====
export const addTenantFavorite = (houseId) =>
  tenantRequest.post('/rental/tenant-app/house-favorite/add', null, { params: { houseId } })
export const cancelTenantFavorite = (houseId) =>
  tenantRequest.put('/rental/tenant-app/house-favorite/cancel', null, { params: { houseId } })
export const getTenantFavorites = (params = {}) =>
  tenantRequest.get('/rental/tenant-app/house-favorite/page', { params })
export const getTenantFavoriteHouseIds = () => tenantRequest.get('/rental/tenant-app/house-favorite/ids')

// ===== 看房预约 =====
export const createTenantAppointment = (data) =>
  tenantRequest.post('/rental/tenant-app/viewing-appointment/create', data)
export const getTenantAppointments = (params = {}) =>
  tenantRequest.get('/rental/tenant-app/viewing-appointment/page', { params })
export const cancelTenantAppointment = (id) =>
  tenantRequest.put('/rental/tenant-app/viewing-appointment/cancel', null, { params: { id } })

// ===== 公告 =====
export const getTenantAnnouncements = (params = {}) =>
  tenantRequest.get('/rental/tenant-app/announcement/page', { params })
export const getTenantAnnouncement = (id) =>
  tenantRequest.get('/rental/tenant-app/announcement/get', { params: { id } })
export const readTenantAnnouncement = (id) =>
  tenantRequest.put('/rental/tenant-app/announcement/read', null, { params: { id } })
export const getTenantUnreadAnnouncementCount = () =>
  tenantRequest.get('/rental/tenant-app/announcement/unread-count')

// ===== 报修 =====
export const createTenantRepairOrder = (data) => tenantRequest.post('/rental/tenant-app/repair/create', data)
export const getTenantRepairs = (params = {}) => tenantRequest.get('/rental/tenant-app/repair/page', { params })
export const confirmTenantRepairOrder = (id) =>
  tenantRequest.put('/rental/tenant-app/repair/confirm', null, { params: { id } })
