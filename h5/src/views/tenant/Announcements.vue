<template>
  <el-card>
    <div class="toolbar">
      <span class="title">公告</span>
      <el-select v-model="query.category" placeholder="全部分类" clearable class="filter" @change="getList">
        <el-option v-for="c in categories" :key="c" :label="c" :value="c" />
      </el-select>
    </div>
    <el-table :data="list" v-loading="loading" @row-click="openDetail">
      <el-table-column label="标题" min-width="220" show-overflow-tooltip>
        <template #default="s">
          <el-tag v-if="s.row.isTop === 1" type="danger" size="small" class="mr">置顶</el-tag>
          <span class="link">{{ s.row.title }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="category" label="分类" width="110" />
      <el-table-column prop="publishTime" label="发布时间" width="170" show-overflow-tooltip />
      <el-table-column label="状态" width="80">
        <template #default="s">
          <el-tag :type="s.row.isRead ? 'info' : 'danger'">{{ s.row.isRead ? '已读' : '未读' }}</el-tag>
        </template>
      </el-table-column>
    </el-table>

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
  </el-card>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getTenantAnnouncements, getTenantAnnouncement, readTenantAnnouncement } from '../../api'

const loading = ref(false)
const list = ref([])
const query = reactive({ category: undefined })
const categories = ['缴费通知', '维修通知', '社区公告', '紧急通知']

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
.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}
.title {
  font-size: 16px;
  font-weight: 600;
}
.filter {
  width: 140px;
}
.mr {
  margin-right: 6px;
}
.link {
  cursor: pointer;
}
.meta {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}
.time {
  color: #999;
  font-size: 12px;
}
.content {
  line-height: 1.8;
  max-height: 60vh;
  overflow: auto;
}
</style>
