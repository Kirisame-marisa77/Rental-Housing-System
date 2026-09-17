import { createRouter, createWebHistory } from 'vue-router'
import { roleFromPath, isLoggedIn, ROLE_HOME } from './auth'

/**
 * 路由必须按角色加前缀：houses / applies / appointments / contracts / repairs / profile
 * 在两个角色的子路由里重名，而 createWebHistory 的路径必须唯一
 */
const routes = [
  { path: '/login', component: () => import('./views/Login.vue') },
  { path: '/register', component: () => import('./views/Register.vue') },
  {
    path: '/owner',
    component: () => import('./views/owner/Layout.vue'),
    redirect: '/owner/home',
    children: [
      { path: 'home', component: () => import('./views/owner/Home.vue'), meta: { title: '首页' } },
      { path: 'houses', component: () => import('./views/owner/Houses.vue'), meta: { title: '我的房源' } },
      { path: 'applies', component: () => import('./views/owner/Applies.vue'), meta: { title: '租房申请' } },
      { path: 'appointments', component: () => import('./views/owner/Appointments.vue'), meta: { title: '看房预约' } },
      { path: 'meters', component: () => import('./views/owner/Meters.vue'), meta: { title: '水电抄表' } },
      { path: 'repairs', component: () => import('./views/owner/Repairs.vue'), meta: { title: '维修工单' } },
      { path: 'contracts', component: () => import('./views/owner/Contracts.vue'), meta: { title: '合同' } },
      { path: 'bills', component: () => import('./views/owner/Bills.vue'), meta: { title: '我的账单' } },
      { path: 'profile', component: () => import('./views/owner/Profile.vue'), meta: { title: '个人信息' } }
    ]
  },
  {
    path: '/tenant',
    component: () => import('./views/tenant/Layout.vue'),
    redirect: '/tenant/home',
    children: [
      { path: 'home', component: () => import('./views/tenant/Home.vue'), meta: { title: '首页' } },
      { path: 'houses', component: () => import('./views/tenant/Houses.vue'), meta: { title: '找房' } },
      { path: 'favorites', component: () => import('./views/tenant/Favorites.vue'), meta: { title: '我的收藏' } },
      { path: 'appointments', component: () => import('./views/tenant/Appointments.vue'), meta: { title: '我的预约' } },
      { path: 'announcements', component: () => import('./views/tenant/Announcements.vue'), meta: { title: '公告' } },
      { path: 'applies', component: () => import('./views/tenant/Applies.vue'), meta: { title: '我的申请' } },
      { path: 'contracts', component: () => import('./views/tenant/Contracts.vue'), meta: { title: '我的合同' } },
      { path: 'bills', component: () => import('./views/tenant/Bills.vue'), meta: { title: '我的账单' } },
      { path: 'move-outs', component: () => import('./views/tenant/MoveOuts.vue'), meta: { title: '退租申请' } },
      { path: 'repairs', component: () => import('./views/tenant/Repairs.vue'), meta: { title: '报修' } },
      { path: 'profile', component: () => import('./views/tenant/Profile.vue'), meta: { title: '个人信息' } }
    ]
  },
  {
    // 根路径：按已有会话选边；两端都有会话时业主优先（确定性行为）
    path: '/',
    redirect: () => {
      if (isLoggedIn('owner')) return ROLE_HOME.owner
      if (isLoggedIn('tenant')) return ROLE_HOME.tenant
      return '/login'
    }
  },
  // 兜底：未注册路径不再白屏（原两个 App 都存在这个隐患）
  { path: '/:pathMatch(.*)*', redirect: '/' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

const PUBLIC = ['/login', '/register']

router.beforeEach((to) => {
  // 登录/注册页永不拦截。绝不能写成「已登录就自动跳走」——那样在只有租客 token 时会成环
  if (PUBLIC.includes(to.path)) return true
  const role = roleFromPath(to.path)
  if (!role) return '/login' // 正常已被 catch-all 拦下，这里是双保险
  if (!isLoggedIn(role)) {
    // 带上回跳目标，登录后可以回到原本想去的页面
    return { path: '/login', query: { role, redirect: to.fullPath } }
  }
  return true
})

export default router
