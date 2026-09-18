<template>
  <AppPage>
    <div class="hello">
      <div class="hello__title">你好，{{ tenantName }}</div>
      <div class="u-tip">这里是租客端，可以完成下面的操作</div>
    </div>

    <div class="grid">
      <div
        v-for="item in entries"
        :key="item.path"
        class="grid__cell"
        @click="router.push(item.path)"
      >
        <el-icon class="grid__icon"><component :is="item.icon" /></el-icon>
        <div class="grid__text">
          <div class="grid__title">{{ item.title }}</div>
          <div class="grid__desc">{{ item.desc }}</div>
        </div>
      </div>
    </div>
  </AppPage>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { getUserName } from '../../auth'
import { menusOf } from '../../menus'
import AppPage from '../../components/AppPage.vue'

/**
 * 入口列表直接来自 menus.js —— 这里刻意不再自己维护一份菜单数组。
 * 之前三处各写一份，加菜单时漏改过两次（加了菜单但首页入口没加）。
 */
const router = useRouter()
const tenantName = getUserName('tenant') || '租客'

// 首页自身不需要入口；详情页这类 hidden 的也不列
const entries = computed(() => menusOf('tenant').filter((m) => !m.hidden && m.path !== '/tenant/home'))
</script>

<style scoped>
.hello {
  margin-bottom: 12px;
}

.hello__title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 4px;
}

.grid {
  display: flex;
  flex-direction: column;
  gap: var(--app-gap);
}

.grid__cell {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px;
  background: #fff;
  border-radius: var(--app-radius);
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);
  cursor: pointer;
  -webkit-tap-highlight-color: transparent;
}

.grid__cell:active {
  background: var(--el-fill-color-light);
}

.grid__icon {
  font-size: 24px;
  color: var(--el-color-primary);
  flex-shrink: 0;
}

.grid__text {
  min-width: 0;
}

.grid__title {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 2px;
}

.grid__desc {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  line-height: 1.5;
}
</style>
