<template>
  <!-- 根节点必须有定位上下文与最小高度：v-loading 的遮罩是 absolute 定位，
       不加的话加载态要么看不见、要么容器高度塌成 0 -->
  <div v-loading="loading" class="card-list">
    <template v-if="data && data.length">
      <div v-for="(row, index) in data" :key="row[keyField] ?? index" class="card-list__item">
        <slot name="item" :row="row" :index="index" />
      </div>
    </template>

    <slot v-else name="empty">
      <el-empty :description="emptyText" :image-size="70" />
    </slot>
  </div>
</template>

<script setup>
/**
 * 卡片列表容器
 *
 * 把 v-loading、空态、卡片间距收在一处 —— 页面迁移时只需要提供一个 item 模板，
 * 不用每页各写一遍这三件事。
 */
defineProps({
  data: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false },
  emptyText: { type: String, default: '暂无数据' },
  /** 行主键字段。项目里所有列表都有 id */
  keyField: { type: String, default: 'id' }
})
</script>

<style scoped>
.card-list {
  position: relative;
  min-height: 120px;
}

.card-list__item + .card-list__item {
  margin-top: var(--app-gap);
}
</style>
