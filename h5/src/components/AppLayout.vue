<template>
  <div class="app-layout">
    <header class="app-header">
      <span class="app-header__title">社区房屋租赁 · {{ roleLabel }}端</span>
      <span class="app-header__spacer" />
      <span class="app-header__name">{{ userName }}</span>
      <el-button link type="danger" @click="logout">退出</el-button>
    </header>

    <main class="app-main">
      <router-view />
    </main>

    <AppTabbar :role="role" />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { clearSession, getUserName, isRole, roleFromPath, ROLE_LABEL } from '../auth'
import AppTabbar from './AppTabbar.vue'

/**
 * 业主端与租客端共用同一个外壳
 *
 * 原先两个 Layout.vue 逐字节雷同，只有标题、菜单项数量、会话 key 不同，
 * 现在合并成一个按 role 参数化的组件，角色由当前路径推导。
 */
const route = useRoute()
const router = useRouter()

const role = computed(() => {
  const fromPath = roleFromPath(route.path)
  // 理论上不会走到兜底：路由守卫已经把非角色路径拦掉了
  return isRole(fromPath) ? fromPath : 'tenant'
})

const roleLabel = computed(() => ROLE_LABEL[role.value])
const userName = computed(() => getUserName(role.value) || ROLE_LABEL[role.value])

const logout = () => {
  // 只清本角色会话：另一个角色在别的标签页里的登录态必须保留，
  // 否则「一个窗口业主、一个窗口租客」的演示动线就断了
  clearSession(role.value)
  router.push('/login')
}
</script>

<!--
  骨架样式（.app-layout / .app-header / .app-main）放在 src/styles/base.css：
  它们是全局唯一的一套，收在全局样式里便于统一调整尺寸。
-->
