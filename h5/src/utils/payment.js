/**
 * 付款方式「押N付M」工具
 *
 *   N = 押几个月租金 → 押金 = N × 月租金
 *   M = 一次付几个月租金 → 首期应缴 = 押金 + M × 月租金
 *
 * N 与 M 各自取 1~3，共 9 种组合。
 *
 * 这份规则与后端 cn.iocoder.yudao.module.rental.util.PaymentMethodUtils 一一对应，
 * 两边都改才算改完 —— 前端算出来的只是给用户看的预览，真正入账的金额以后端为准。
 */

const CN_NUMBERS = ['', '一', '二', '三']

const MIN_MONTHS = 1
const MAX_MONTHS = 3

export const DEFAULT_PAYMENT_METHOD = '押一付一'

/** 全部 9 种组合，押几优先排序 */
export const PAYMENT_METHOD_OPTIONS = CN_NUMBERS.slice(MIN_MONTHS).flatMap((cnDeposit, i) =>
  CN_NUMBERS.slice(MIN_MONTHS).map((cnPay, j) => ({
    label: `押${cnDeposit}付${cnPay}`,
    depositMonths: i + 1,
    payMonths: j + 1
  }))
)

/** 解析「押N付M」的某一段月数，认中文数字也认阿拉伯数字，认不出来按 1 处理 */
const parseMonths = (label, prefix) => {
  if (!label) {
    return MIN_MONTHS
  }
  const index = String(label).indexOf(prefix)
  if (index < 0 || index + 1 >= String(label).length) {
    return MIN_MONTHS
  }
  const ch = String(label).charAt(index + 1)
  if (ch >= '1' && ch <= '9') {
    return Math.min(Math.max(Number(ch), MIN_MONTHS), MAX_MONTHS)
  }
  const cnIndex = CN_NUMBERS.indexOf(ch)
  return cnIndex >= MIN_MONTHS ? cnIndex : MIN_MONTHS
}

export const depositMonthsOf = (label) => parseMonths(label, '押')
export const payMonthsOf = (label) => parseMonths(label, '付')

const round2 = (n) => Math.round((Number(n) || 0) * 100) / 100

/** 押金 = 押数 × 月租金 */
export const calcDeposit = (monthlyRent, paymentMethod) =>
  round2((Number(monthlyRent) || 0) * depositMonthsOf(paymentMethod))

/** 首期应缴 = 押金 + 付数 × 月租金 */
export const calcFirstPayment = (monthlyRent, deposit, paymentMethod) =>
  round2((Number(monthlyRent) || 0) * payMonthsOf(paymentMethod) + (Number(deposit) || 0))
