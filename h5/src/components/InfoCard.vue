<template>
  <div :class="['info-card', { 'info-card--clickable': clickable }]" @click="onClick">
    <!-- 标题 / 副标题 -->
    <div v-if="title || subtitle || $slots.title" class="info-card__head">
      <div class="info-card__head-main">
        <div class="info-card__title">
          <slot name="title">{{ title }}</slot>
        </div>
        <div v-if="subtitle" class="info-card__subtitle ellipsis-1">{{ subtitle }}</div>
      </div>
      <div v-if="visibleTags.length || $slots.tags" class="info-card__tags">
        <slot name="tags">
          <el-tag
            v-for="(tag, i) in visibleTags"
            :key="i"
            :type="tag.type || 'info'"
            :effect="tag.plain ? 'plain' : 'light'"
            size="small"
          >
            {{ tag.text }}
          </el-tag>
        </slot>
      </div>
    </div>

    <!-- 字段区：两列网格 -->
    <div v-if="visibleFields.length" class="info-card__fields">
      <div
        v-for="(field, i) in visibleFields"
        :key="i"
        :class="['info-card__field', { 'info-card__field--span2': field.span === 2 }]"
      >
        <span class="info-card__label">{{ field.label }}</span>
        <span :class="['info-card__value', valueClass(field)]">
          <span :class="{ 'clamp-2': field.span === 2 && field.clamp === 2 }">{{ fieldText(field) }}</span>
        </span>
      </div>
    </div>

    <!-- 逃生口：进度条、自定义区块这类不适合塞进字段网格的内容 -->
    <slot name="extra" />

    <!-- 操作区：有该 slot 才渲染，避免空的分隔线 -->
    <div v-if="$slots.actions" class="info-card__actions" @click.stop>
      <slot name="actions" />
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

/**
 * 单条记录卡片 —— 取代 el-table 的一行
 *
 * 字段划分为三类：
 *   title/subtitle  标识这条记录是什么（房源地址、合同编号…）
 *   tags            值域是枚举的状态列（状态、审核结果…）
 *   fields          其余全部字段，按「标签—值」两列铺开
 *   actions         底部操作按钮，必须是 slot
 *
 * actions 刻意不做成配置数组：按钮的显隐是逐行的业务条件
 * （status === 0、未缴费、已通过审核…），一旦做成 DSL 就变成
 * 「在配置里写条件函数」，比直接写模板更难读也更容易错。
 */
const props = defineProps({
  title: { type: String, default: '' },
  subtitle: { type: String, default: '' },
  /** [{ text, type?, plain? }] */
  tags: { type: Array, default: () => [] },
  /**
   * [{ label, value: string|number|(row)=>any, span?: 1|2, clamp?: 1|2,
   *    type?: 'default'|'muted'|'primary'|'success'|'warning'|'danger'|'amount',
   *    strong?: boolean, hidden?: boolean|(row)=>boolean }]
   */
  fields: { type: Array, default: () => [] },
  /** 当前行数据，供 value/hidden 为函数时使用 */
  row: { type: Object, default: () => ({}) },
  clickable: { type: Boolean, default: false }
})

const emit = defineEmits(['click'])

const resolve = (v) => (typeof v === 'function' ? v(props.row) : v)

const visibleFields = computed(() => props.fields.filter((f) => !resolve(f.hidden)))

const visibleTags = computed(() => props.tags.filter((t) => !resolve(t.hidden)))

// 空值统一显示 '-'，与原来 el-table 的观感保持一致
const fieldText = (field) => {
  const value = resolve(field.value)
  return value === null || value === undefined || value === '' ? '-' : value
}

const valueClass = (field) => [
  `info-card__value--${field.type || 'default'}`,
  { 'info-card__value--strong': field.strong }
]

const onClick = () => {
  if (props.clickable) {
    emit('click', props.row)
  }
}
</script>

<style scoped>
.info-card {
  background: #fff;
  border-radius: var(--app-radius);
  padding: 12px 14px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);
}

.info-card--clickable {
  cursor: pointer;
  -webkit-tap-highlight-color: transparent;
}

.info-card--clickable:active {
  background: var(--el-fill-color-light);
}

/* ---------- 头部 ---------- */
.info-card__head {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  margin-bottom: 10px;
}

.info-card__head-main {
  flex: 1;
  min-width: 0; /* 不加这条，里面的 ellipsis 会失效、把卡片撑宽 */
}

.info-card__title {
  font-size: 15px;
  font-weight: 600;
  line-height: 1.4;
  overflow-wrap: anywhere;
}

.info-card__subtitle {
  margin-top: 2px;
  color: var(--el-text-color-secondary);
  font-size: 12px;
}

.info-card__tags {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 4px;
  flex-shrink: 0;
}

/* ---------- 字段网格 ---------- */
.info-card__fields {
  display: grid;
  /* minmax(0, 1fr) 是必须的：默认的 1fr 最小宽度是 auto，
     长地址/合同编号这类没有空格的连续串会把列撑宽，进而出现横向滚动条 */
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 6px 12px;
}

.info-card__field {
  display: flex;
  gap: 6px;
  font-size: 13px;
  min-width: 0;
}

.info-card__field--span2 {
  grid-column: 1 / -1;
}

.info-card__label {
  color: var(--el-text-color-secondary);
  flex-shrink: 0;
  font-size: 12px;
  line-height: 1.6;
}

.info-card__value {
  color: var(--el-text-color-primary);
  overflow-wrap: anywhere;
  line-height: 1.6;
  min-width: 0;
}

.info-card__value--muted {
  color: var(--el-text-color-secondary);
}
.info-card__value--primary {
  color: var(--el-color-primary);
}
.info-card__value--success {
  color: var(--el-color-success);
}
.info-card__value--warning {
  color: var(--el-color-warning);
}
.info-card__value--danger,
.info-card__value--amount {
  color: var(--el-color-danger);
}
.info-card__value--amount {
  font-weight: 600;
}
.info-card__value--strong {
  font-weight: 600;
}

/* ---------- 操作区 ---------- */
.info-card__actions {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 4px;
  margin-top: 10px;
  padding-top: 4px;
  border-top: 1px solid var(--el-border-color-lighter);
}
</style>
