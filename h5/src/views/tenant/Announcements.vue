<template>
  <AppPage title="公告">
    <template #actions>
      <el-select
        v-model="query.category"
        placeholder="全部分类"
        clearable
        class="filter"
        @change="getList"
      >
        <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
      </el-select>
    </template>

    <CardList :data="list" :loading="loading" empty-text="暂无公告">
      <template #item="{ row }">
        <!-- 原来是 el-table 的 @row-click，卡片化后必须显式接到 clickable 上，
             否则「点一行看公告」会无声失效（不报错，最难发现的一类） -->
        <InfoCard
          :title="row.title"
          :subtitle="row.publishTime"
          :tags="tagsOf(row)"
          :fields="fieldsOf(row)"
          clickable
          @click="openDetail(row)"
        />
      </template>
    </CardList>

    <el-dialog v-model="detailVisible" :title="detail?.title" width="640px" top="6vh">
      <div class="meta">
        <el-tag size="small">{{ detail?.category }}</el-tag>
        <span class="time">{{ detail?.publishTime }}</span>
      </div>
      <!-- 正文是管理端录入的富文本，v-html 渲染属预期行为；
           但 AnnouncementSaveReqVO 没有做内容过滤，这里存在存储型 XSS 面，仅演示环境可接受 -->
      <div class="content" v-html="detail?.content"></div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </AppPage>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { getTenantAnnouncement, getTenantAnnouncements, readTenantAnnouncement } from '../../api'
import AppPage from '../../components/AppPage.vue'
import CardList from '../../components/CardList.vue'
import InfoCard from '../../components/InfoCard.vue'

const loading = ref(false)
const list = ref([])
const query = reactive({ category: undefined })
const categories = ['缴费通知', '维修通知', '社区公告', '紧急通知']

const tagsOf = (row) => [
  ...(row.isTop === 1 ? [{ text: '置顶', type: 'danger' }] : []),
  { text: row.isRead ? '已读' : '未读', type: row.isRead ? 'info' : 'danger' }
]

const fieldsOf = (row) => [{ label: '分类', value: row.category }]

const getList = async () => {
  loading.value = true
  try {
    const data = await getTenantAnnouncements({ pageNo: 1, pageSize: 100, category: query.category })
    list.value = data.list || []
  } finally {
    loading.value = false
  }
}

const detailVisible = ref(false)
const detail = ref(null)

const openDetail = async (row) => {
  // 先取详情渲染，再单独标记已读 —— 不让 GET 带副作用
  detail.value = await getTenantAnnouncement(row.id)
  detailVisible.value = true
  if (!row.isRead) {
    await readTenantAnnouncement(row.id)
    // 就地更新，不重拉列表
    row.isRead = true
  }
}

onMounted(getList)
</script>

<style scoped>
.filter {
  width: 130px;
}
.meta {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}
.time {
  color: var(--el-text-color-secondary);
  font-size: 12px;
}
.content {
  line-height: 1.8;
  max-height: 60vh;
  overflow: auto;
  overflow-wrap: anywhere;
}
</style>
