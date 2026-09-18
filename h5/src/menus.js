import {
  Bell,
  Calendar,
  Document,
  Grid,
  HomeFilled,
  House,
  List,
  Money,
  Refresh,
  Search,
  Star,
  Tickets,
  Tools,
  User
} from '@element-plus/icons-vue'

/**
 * 菜单唯一数据源
 *
 * 三处消费：router.js（生成路由 children）、AppTabbar（底部 tab）、More.vue（九宫格）。
 *
 * 加一个页面只需要在这里加一行 —— 路由、底部 tab、「更多」九宫格会同时生效。
 * 之前菜单定义散在 Layout.vue / router.js / Home.vue 三处，本项目已经因此漏改过两次
 * （加了菜单但首页九宫格没加），现在从机制上消除了这个问题。
 *
 * 字段：
 *   path   完整路径。子路由直接用绝对路径，Vue Router 4 支持
 *   file   src/views 下的相对路径，由 router.js 用 import.meta.glob 解析
 *   title  页面标题（同时写进 meta.title）
 *   desc   一句话说明，首页九宫格用
 *   icon   底部 tab / 九宫格 / 首页入口的图标组件
 *   tab    true = 出现在底部 tab（每端恰好 4 个）
 *   parent 多级页面（如房源详情）所属的一级路径，用于让 tab 正确高亮
 *   hidden 不进 tab、不进九宫格、不进首页入口，但仍注册路由
 */

export const MENUS = {
  tenant: [
    {
      path: '/tenant/home', file: 'tenant/Home.vue', title: '首页', icon: HomeFilled, tab: true,
      desc: '把常用的功能都放在这里'
    },
    {
      path: '/tenant/houses', file: 'tenant/Houses.vue', title: '找房', icon: Search, tab: true,
      desc: '浏览上架房源，收藏 / 预约看房 / 提交申请'
    },
    {
      path: '/tenant/bills', file: 'tenant/Bills.vue', title: '我的账单', icon: Money, tab: true,
      desc: '查看并缴纳租金与水电费账单'
    },
    {
      path: '/tenant/profile', file: 'tenant/Profile.vue', title: '我的', icon: User, tab: true,
      desc: '维护个人资料、修改密码、注销账号'
    },

    {
      path: '/tenant/favorites', file: 'tenant/Favorites.vue', title: '我的收藏', icon: Star,
      desc: '收藏的房源，随时回看并申请'
    },
    {
      path: '/tenant/appointments', file: 'tenant/Appointments.vue', title: '我的预约', icon: Calendar,
      desc: '查看看房预约及房东确认进度'
    },
    {
      path: '/tenant/applies', file: 'tenant/Applies.vue', title: '我的申请', icon: Tickets,
      desc: '查看租房申请及房东审批进度'
    },
    {
      path: '/tenant/contracts', file: 'tenant/Contracts.vue', title: '我的合同', icon: Document,
      desc: '查看合同、确认签约'
    },
    {
      path: '/tenant/move-outs', file: 'tenant/MoveOuts.vue', title: '退租申请', icon: Refresh,
      desc: '发起退租申请，查看房东验收与结算结果'
    },
    {
      path: '/tenant/announcements', file: 'tenant/Announcements.vue', title: '公告', icon: Bell,
      desc: '查看社区公告与通知'
    },
    {
      path: '/tenant/repairs', file: 'tenant/Repairs.vue', title: '报修', icon: Tools,
      desc: '提交报修工单并跟踪处理进度'
    },

    { path: '/tenant/more', file: 'More.vue', title: '更多', icon: Grid, hidden: true },
    {
      path: '/tenant/houses/:id',
      file: 'tenant/HouseDetail.vue',
      title: '房源详情',
      hidden: true,
      parent: '/tenant/houses'
    }
  ],

  owner: [
    {
      path: '/owner/home', file: 'owner/Home.vue', title: '首页', icon: HomeFilled, tab: true,
      desc: '把常用的功能都放在这里'
    },
    {
      path: '/owner/houses', file: 'owner/Houses.vue', title: '我的房源', icon: House, tab: true,
      desc: '上传房源、上下架、查看审核状态'
    },
    {
      path: '/owner/applies', file: 'owner/Applies.vue', title: '租房申请', icon: List, tab: true,
      desc: '查看租客申请，同意后自动生成合同与首期账单'
    },
    {
      path: '/owner/profile', file: 'owner/Profile.vue', title: '我的', icon: User, tab: true,
      desc: '维护个人资料、修改密码、注销账号'
    },

    {
      path: '/owner/appointments', file: 'owner/Appointments.vue', title: '看房预约', icon: Calendar,
      desc: '确认租客的看房预约，完成后填写反馈'
    },
    {
      path: '/owner/meters', file: 'owner/Meters.vue', title: '水电抄表', icon: Tickets,
      desc: '上传水电读数 + 表盘截图，等待管理员审核'
    },
    {
      path: '/owner/repairs', file: 'owner/Repairs.vue', title: '维修工单', icon: Tools,
      desc: '查看租客报修，处理并上传处理证据'
    },
    {
      path: '/owner/contracts', file: 'owner/Contracts.vue', title: '合同', icon: Document,
      desc: '查看合同详情并确认签约'
    },
    {
      path: '/owner/move-outs', file: 'owner/MoveOuts.vue', title: '退租处理', icon: Refresh,
      desc: '处理租客的退租申请：房屋验收 + 费用结算'
    },
    {
      path: '/owner/bills', file: 'owner/Bills.vue', title: '我的账单', icon: Money,
      desc: '查看名下房源的租金与水电费账单（只读）'
    },

    { path: '/owner/more', file: 'More.vue', title: '更多', icon: Grid, hidden: true }
  ]
}

export const menusOf = (role) => MENUS[role] || []

/** 底部 tab（每端 4 个） */
export const tabMenus = (role) => menusOf(role).filter((m) => m.tab)

/** 「更多」九宫格：整站地图，排除详情页这类不该有入口的 */
export const gridMenus = (role) => menusOf(role).filter((m) => !m.hidden)

/** 「更多」页里排除掉已经在底部 tab 上的，避免重复入口 */
export const moreMenus = (role) => menusOf(role).filter((m) => !m.tab && !m.hidden)

/**
 * 由当前路径反查应该高亮哪个 tab
 *
 * 详情页（/tenant/houses/3）靠 parent 回指到 /tenant/houses。
 * 这条取代了原先「取路径前两段」的写法 —— 那种写法遇到 /tenant/spaces/...
 * 这类同层多页时会判错。
 */
export const activeMenuPath = (role, path) => {
  const hit = menusOf(role)
    .filter((m) => path === m.path || path.startsWith(m.path + '/'))
    // 取匹配最长的那个（如 /a/b/c 同时命中 /a 和 /a/b 时选后者）
    .sort((a, b) => b.path.length - a.path.length)[0]
  // parent 让带参数的详情页也能高亮回它所属的一级页面
  return hit?.parent || hit?.path || ''
}
