<template>
  <AppPage title="更多">
    <div class="grid">
      <div v-for="item in items" :key="item.path" class="grid__cell" @click="router.push(item.path)">
        <el-icon class="grid__icon"><component :is="item.icon" /></el-icon>
        <span class="grid__label">{{ item.title }}</span>
      </div>
    </div>

    <div class="logout">
      <el-button type="danger" plain class="u-w-full" @click="logout">退出登录</el-button>
      <div class="u-tip logout__tip">只退出当前角色，另一个角色的登录态不受影响</div>
    </div>
  </AppPage>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { clearSession, isRole, roleFromPath, ROLE_LABEL } from '../auth'
import { gridMenus } from '../menus'
import AppPage from '../components/AppPage.vue'

/**
 * 「更多」九宫格：业主端与租客端共用同一个页面，角色由路径推导。
 *
 * 这里列出的是整站地图（menus.js 里 hidden 的详情页除外），
 * 与底部 tab 会有少量重复 —— 这是刻意的：用户从 tab 进不来时，
 * 在「更多」里一定要能找到。
 */
const route = useRoute()
const router = useRouter()

const role = computed(() => {
  const fromPath = roleFromPath(route.path)
  return isRole(fromPath) ? fromPath : 'tenant'
})

const items = computed(() => gridMenus(role.value))

const logout = () => {
  clearSession(role.value)
  router.push('/login')
}
</script>

<style scoped>
.grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 8px;
  background: #fff;
  border-radius: var(--app-radius);
  padding: 12px 8px;
}

.grid__cell {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 12px 4px;
  border-radius: var(--app-radius-sm);
  cursor: pointer;
  -webkit-tap-highlight-color: transparent;
}

.grid__cell:active {
  background: var(--el-fill-color-light);
}

.grid__icon {
  font-size: 24px;
  color: var(--el-color-primary);
}

.grid__label {
  font-size: 12px;
  color: var(--el-text-color-primary);
  text-align: center;
  line-height: 1.3;
}

.logout {
  margin-top: 16px;
}

.logout__tip {
  margin-top: 8px;
  text-align: center;
}
</style>
