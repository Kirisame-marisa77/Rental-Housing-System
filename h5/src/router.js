import { createRouter, createWebHistory } from 'vue-router'
import { isLoggedIn, roleFromPath, ROLE_HOME } from './auth'
import { MENUS } from './menus'
import AppLayout from './components/AppLayout.vue'

/**
 * 路由不再手写 children，而是从 menus.js 生成。
 *
 * 这样「加一个页面」只需要改 menus.js 一行：路由、底部 tab、更多九宫格同时生效。
 * 之前三处手写、漏改过两次（加了菜单但首页九宫格没加）。
 */

// 懒加载：保留按页面分包的行为
const pages = import.meta.glob('./views/**/*.vue')

const page = (file) => {
  const component = pages[`./views/${file}`]
  if (!component) {
    // 与 auth.js 的「在边界大声失败」保持一致：宁可启动就报错，
    // 也不要等到用户点进那个菜单才白屏
    throw new Error(`menus.js 引用了不存在的页面: src/views/${file}`)
  }
  return component
}

const childrenOf = (role) =>
  MENUS[role].map((menu) => ({
    path: menu.path, // 绝对路径，Vue Router 4 允许子路由用绝对路径
    name: menu.path,
    component: page(menu.file),
    meta: { title: menu.title, role, menuPath: menu.parent || menu.path }
  }))

const routes = [
  { path: '/login', component: () => import('./views/Login.vue') },
  { path: '/register', component: () => import('./views/Register.vue') },
  {
    // 两个角色共用同一个外壳组件，角色由路径推导（auth.js 的 roleFromPath）
    path: '/owner',
    component: AppLayout,
    redirect: '/owner/home',
    children: childrenOf('owner')
  },
  {
    path: '/tenant',
    component: AppLayout,
    redirect: '/tenant/home',
    children: childrenOf('tenant')
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
  // 兜底：未注册路径不再白屏
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
