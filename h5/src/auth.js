/**
 * 会话模块：业主 / 租客两套 token 各自独立存放
 *
 * 为什么按角色分键而不是单一键集：合并后两个角色共享同一个 origin，
 * 也就共享同一份 localStorage。若只用一套键，在第 2 个标签页登录租客会顶掉
 * 第 1 个标签页的业主会话，业主页的下一次请求就 401 —— 而这恰好废掉最常用的
 * 演示动线（一个窗口开业主、另一个开租客，走完申请→审批→合同→账单）。
 * 分键的代价只是多几个键，换来两套会话互不干扰、退出只登出一个角色。
 */

const ROLES = ['owner', 'tenant']

const K = {
  token: (r) => `h5_${r}_token`,
  id: (r) => `h5_${r}_id`,
  name: (r) => `h5_${r}_name`
}
const LAST_ROLE = 'h5_last_role'

export const ROLE_LABEL = { owner: '业主', tenant: '租客' }
export const ROLE_HOME = { owner: '/owner/home', tenant: '/tenant/home' }

export const isRole = (r) => ROLES.includes(r)

/**
 * 从路径解析角色。必须严格前缀匹配：
 * '/tenants'、'/owners-x' 这类不能被误判成角色路径
 */
export const roleFromPath = (path) => {
  if (path === '/owner' || path.startsWith('/owner/')) return 'owner'
  if (path === '/tenant' || path.startsWith('/tenant/')) return 'tenant'
  return null
}

export const getToken = (role) => localStorage.getItem(K.token(role)) || ''
export const getUserId = (role) => localStorage.getItem(K.id(role)) || ''
export const getUserName = (role) => localStorage.getItem(K.name(role)) || ''
export const isLoggedIn = (role) => !!getToken(role)

/**
 * 写入某个角色的会话
 *
 * token 缺失时直接抛错：否则漏改的视图会把字面量字符串 "undefined" 存进去，
 * if (token) 判断通过 → Authorization: Bearer undefined → 401 且提示误导。
 * 在会话边界大声失败，把一整类静默 bug 变成显式报错。
 */
export const setSession = (role, { token, userId, name }) => {
  if (!isRole(role)) throw new Error('未知角色: ' + role)
  if (!token) throw new Error('登录响应缺少 accessToken')
  localStorage.setItem(K.token(role), token)
  localStorage.setItem(K.id(role), String(userId ?? ''))
  localStorage.setItem(K.name(role), name || '')
  localStorage.setItem(LAST_ROLE, role)
}

export const setUserName = (role, name) => localStorage.setItem(K.name(role), name || '')

/** 只清除该角色自己的会话，不影响另一个角色 */
export const clearSession = (role) => {
  localStorage.removeItem(K.token(role))
  localStorage.removeItem(K.id(role))
  localStorage.removeItem(K.name(role))
  if (localStorage.getItem(LAST_ROLE) === role) localStorage.removeItem(LAST_ROLE)
}

/** 登录页默认选中上次使用的角色 */
export const getLastRole = () => (isRole(localStorage.getItem(LAST_ROLE)) ? localStorage.getItem(LAST_ROLE) : '')
