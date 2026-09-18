/**
 * 跨页面完全相同的状态字典
 *
 * 只收「两个及以上页面逐字一致」的字典。凡是文案或取值集合有差异的
 * （比如合同状态两端不同、房源详情页的文案不同），一律留在各自页面里 ——
 * 硬合并会悄悄改掉某个页面的显示。
 *
 * 每个字典都导出 label / type 两个函数：label 给文案，type 给 el-tag 的配色。
 * 沿用原先各页面 `xxxLabel(s) || '-'` 的兜底行为。
 */

const byValue = (map, value) => map.find((i) => i.value === value)

/** 租房申请状态。owner/Applies.vue 与 tenant/Applies.vue 原样相同 */
const APPLY_STATUS_MAP = [
  { value: 0, label: '待审批' },
  { value: 1, label: '已通过' },
  { value: 2, label: '已驳回' },
  { value: 3, label: '已签约' },
  { value: 4, label: '已失效' }
]
export const applyStatusLabel = (s) => byValue(APPLY_STATUS_MAP, s)?.label || '-'
export const applyStatusType = (s) =>
  s === 1 || s === 3 ? 'success' : s === 2 ? 'danger' : s === 4 ? 'info' : 'warning'
export const APPLY_STATUS_OPTIONS = APPLY_STATUS_MAP

/** 房源状态。owner/Houses.vue 的 statusMap 与 tenant/Favorites.vue 的 houseMap 原样相同 */
const HOUSE_STATUS_MAP = [
  { value: 0, label: '下架' },
  { value: 1, label: '上架' },
  { value: 2, label: '已锁定' },
  { value: 3, label: '已出租' }
]
export const houseStatusLabel = (s) => byValue(HOUSE_STATUS_MAP, s)?.label || '-'
export const houseStatusType = (s) =>
  s === 1 ? 'success' : s === 3 ? 'info' : s === 2 ? 'warning' : 'danger'

/** 账单缴费状态。tenant/Bills.vue 与 owner/Bills.vue 原样相同 */
const PAY_STATUS_MAP = [
  { value: 0, label: '待缴费' },
  { value: 1, label: '已缴费' },
  { value: 2, label: '已逾期' }
]
export const payStatusLabel = (s) => byValue(PAY_STATUS_MAP, s)?.label || '-'
export const payStatusType = (s) => (s === 1 ? 'success' : s === 2 ? 'danger' : 'warning')

/** 审核状态。owner/Houses.vue 与 owner/Meters.vue 原样相同 */
const REVIEW_STATUS_MAP = [
  { value: 0, label: '待审核' },
  { value: 1, label: '已通过' },
  { value: 2, label: '已驳回' }
]
export const reviewStatusLabel = (s) => byValue(REVIEW_STATUS_MAP, s)?.label || '-'
export const reviewStatusType = (s) => (s === 1 ? 'success' : s === 2 ? 'danger' : 'warning')
