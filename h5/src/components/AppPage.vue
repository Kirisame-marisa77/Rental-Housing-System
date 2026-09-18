<template>
  <div :class="['app-page', { 'app-page--has-footer': hasFooter }]">
    <div v-if="showBar" :class="['app-page__bar', { 'app-page__bar--sticky': stickyBar }]">
      <el-icon v-if="showBack" class="app-page__back" @click="onBack"><ArrowLeft /></el-icon>
      <span class="app-page__title">
        <slot name="title">{{ title }}</slot>
      </span>
      <span v-if="subtitle" class="app-page__subtitle">{{ subtitle }}</span>
      <span class="app-page__spacer" />
      <slot name="actions" />
    </div>

    <div v-loading="loading" class="app-page__body">
      <slot />
    </div>

    <div v-if="hasFooter" class="app-page__footer">
      <slot name="footer" />
    </div>
  </div>
</template>

<script setup>
import { computed, useSlots } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'

/**
 * 页面容器：取代原先每个页面都抄一遍的
 * <el-card><div class="toolbar"><span class="title">…</span></div>…
 *
 * 加载态由这里统一挂载，页面不用再自己写 v-loading。
 */
const props = defineProps({
  title: { type: String, default: '' },
  subtitle: { type: String, default: '' },
  showBack: { type: Boolean, default: false },
  loading: { type: Boolean, default: false },
  /** 标题栏是否吸顶。长列表滚动时保持可见 */
  stickyBar: { type: Boolean, default: true }
})

const emit = defineEmits(['back'])

const slots = useSlots()
const router = useRouter()

const hasFooter = computed(() => !!slots.footer)

// 没有标题、没有返回、也没有操作按钮时，整条标题栏不渲染（如 Home 这类纯入口页）
const showBar = computed(
  () => !!props.title || props.showBack || !!slots.actions || !!slots.title
)

const onBack = () => {
  emit('back')
  // 直接打开详情页链接时没有历史记录，router.back() 会退出应用，所以兜底回首页
  if (window.history.length > 1) {
    router.back()
  } else {
    router.push('/')
  }
}
</script>

<style scoped>
.app-page__bar {
  display: flex;
  align-items: center;
  gap: 8px;
  min-height: 40px;
  margin-bottom: var(--app-gap);
  padding: 8px 0;
  background: #f5f7fa;
}

.app-page__bar--sticky {
  position: sticky;
  /* 外壳的 header 是跟着滚走的（省屏幕高度），所以这里只需避开顶部安全区。
     若哪天把 header 改成吸顶，这里要跟着加上 var(--app-header-h) */
  top: var(--app-safe-top);
  /* 必须远小于 Element Plus 的 .el-overlay(2000)，否则弹窗会被盖住 */
  z-index: 90;
}

.app-page__back {
  font-size: 20px;
  cursor: pointer;
  color: var(--el-text-color-primary);
}

.app-page__title {
  font-size: 17px;
  font-weight: 600;
}

.app-page__subtitle {
  color: var(--el-text-color-secondary);
  font-size: 12px;
}

.app-page__spacer {
  flex: 1;
}

/* v-loading 的遮罩是 absolute 定位，容器必须有定位上下文和最小高度，
   否则加载态要么看不见、要么把高度塌成 0 */
.app-page__body {
  position: relative;
  min-height: 120px;
}

/* 底部固定操作条（如房源详情的 收藏/预约/申请）。
   要落在 tab 栏之上，所以 bottom 要加上 tab 栏高度与安全区 */
.app-page__footer {
  position: fixed;
  left: 0;
  right: 0;
  bottom: calc(var(--app-tabbar-h) + var(--app-safe-b));
  z-index: 95;
  display: flex;
  gap: 8px;
  /* 与顶栏、tab 栏同宽居中，宽屏下不会被拉满整屏 */
  max-width: var(--app-content-max);
  margin: 0 auto;
  padding: 8px var(--app-page-x);
  background: #fff;
  border-top: 1px solid var(--el-border-color-lighter);
}

/* 有底部操作条时多留出它的高度，否则最后一张卡片被永久遮住 */
.app-page--has-footer .app-page__body {
  padding-bottom: 64px;
}
</style>
