<template>
  <nav class="tabbar">
    <div
      v-for="item in tabs"
      :key="item.path"
      :class="['tabbar__item', { 'tabbar__item--active': item.path === active }]"
      @click="go(item.path)"
    >
      <el-icon class="tabbar__icon"><component :is="item.icon" /></el-icon>
      <span class="tabbar__label">{{ item.title }}</span>
    </div>

    <!-- 「更多」不是一个真实菜单项，固定挂在最后 -->
    <div :class="['tabbar__item', { 'tabbar__item--active': isMoreActive }]" @click="go(morePath)">
      <el-icon class="tabbar__icon"><Grid /></el-icon>
      <span class="tabbar__label">更多</span>
    </div>
  </nav>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Grid } from '@element-plus/icons-vue'
import { activeMenuPath, tabMenus } from '../menus'

const props = defineProps({
  role: { type: String, required: true }
})

const route = useRoute()
const router = useRouter()

const tabs = computed(() => tabMenus(props.role))
const morePath = computed(() => `/${props.role}/more`)

const active = computed(() => activeMenuPath(props.role, route.path))

// 当前路径不属于任何一个 tab 时（收藏、公告、报修…），高亮「更多」
const isMoreActive = computed(() => {
  if (route.path === morePath.value) return true
  return !tabs.value.some((t) => t.path === active.value)
})

const go = (path) => {
  if (route.path !== path) {
    router.push(path)
  }
}
</script>

<style scoped>
.tabbar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  /* 与顶栏、主区用同一个最大宽度并居中，否则在桌面宽屏上
     4 个 tab 会被拉成每格几百像素宽（fixed 元素也能用 left/right + margin auto 居中） */
  max-width: var(--app-content-max);
  margin: 0 auto;
  /* 必须远小于 Element Plus 的 .el-overlay（2000），
     否则弹窗会被底部导航盖住。EP 每次打开弹窗还会递增 z-index */
  z-index: 100;
  display: flex;
  height: var(--app-tabbar-h);
  padding-bottom: var(--app-safe-b);
  background: #fff;
  border-top: 1px solid var(--el-border-color-lighter);
}

.tabbar__item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
  cursor: pointer;
  color: var(--el-text-color-secondary);
  transition: color 0.2s;
  /* 去掉移动端点击时的灰色高亮块 */
  -webkit-tap-highlight-color: transparent;
  user-select: none;
}

.tabbar__item--active {
  color: var(--el-color-primary);
}

.tabbar__icon {
  font-size: 20px;
}

.tabbar__label {
  font-size: 11px;
  line-height: 1;
}
</style>
