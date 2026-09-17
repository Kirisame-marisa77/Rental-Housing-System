<template>
  <!-- 搜索工作栏 -->
  <ContentWrap>
    <el-form ref="queryFormRef" :inline="true" :model="queryParams" class="-mb-15px" label-width="68px">
      <el-form-item label="公告标题" prop="title">
        <el-input v-model="queryParams.title" class="!w-200px" clearable placeholder="请输入公告标题" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="公告分类" prop="category">
        <el-select v-model="queryParams.category" class="!w-160px" clearable placeholder="请选择公告分类">
          <el-option label="缴费通知" value="缴费通知" />
          <el-option label="维修通知" value="维修通知" />
          <el-option label="社区公告" value="社区公告" />
          <el-option label="紧急通知" value="紧急通知" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" class="!w-160px" clearable placeholder="请选择状态">
          <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery">
          <Icon class="mr-5px" icon="ep:search" />
          搜索
        </el-button>
        <el-button @click="resetQuery">
          <Icon class="mr-5px" icon="ep:refresh" />
          重置
        </el-button>
        <el-button v-hasPermi="['rental:announcement:create']" plain type="primary" @click="openForm('create')">
          <Icon class="mr-5px" icon="ep:plus" />
          新增
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <el-table v-loading="loading" :data="list">
      <el-table-column align="center" label="公告编号" prop="noticeNo" width="180" show-overflow-tooltip />
      <el-table-column align="center" label="标题" prop="title" min-width="160" show-overflow-tooltip />
      <el-table-column align="center" label="分类" prop="category" width="100" />
      <el-table-column align="center" label="置顶" width="80">
        <template #default="scope">
          <el-tag v-if="scope.row.isTop === 1" type="danger">置顶</el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column align="center" label="状态" width="90">
        <template #default="scope">
          <el-tag :type="statusTagType(scope.row.status)">{{ statusLabel(scope.row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column align="center" label="发布时间" prop="publishTime" :formatter="dateFormatter" width="170" />
      <el-table-column align="center" label="操作" width="240">
        <template #default="scope">
          <el-button v-if="scope.row.status === 0" v-hasPermi="['rental:announcement:update']" link type="success" @click="handlePublish(scope.row.id)">
            发布
          </el-button>
          <el-button v-hasPermi="['rental:announcement:update']" link @click="handleTop(scope.row.id, scope.row.isTop === 1 ? 0 : 1)">
            {{ scope.row.isTop === 1 ? '取消置顶' : '置顶' }}
          </el-button>
          <el-button v-hasPermi="['rental:announcement:update']" link type="primary" @click="openForm('update', scope.row.id)">
            修改
          </el-button>
          <el-button v-hasPermi="['rental:announcement:delete']" link type="danger" @click="handleDelete(scope.row.id)">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页 -->
    <Pagination
      v-model:limit="queryParams.pageSize"
      v-model:page="queryParams.pageNo"
      :total="total"
      @pagination="getList"
    />
  </ContentWrap>

  <!-- 表单弹窗 -->
  <AnnouncementForm ref="formRef" @success="getList" />
</template>

<script lang="ts" setup>
import * as AnnouncementApi from '@/api/rental/announcement'
import AnnouncementForm from './AnnouncementForm.vue'
import { dateFormatter } from '@/utils/formatTime'

defineOptions({ name: 'RentalAnnouncement' })

const message = useMessage()
const { t } = useI18n()

const loading = ref(true)
const total = ref(0)
const list = ref([])
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  title: '',
  category: undefined,
  status: undefined
})
const queryFormRef = ref()

const statusOptions = [
  { value: 0, label: '草稿' },
  { value: 1, label: '已发布' },
  { value: 2, label: '已删除' }
]
const statusLabel = (status?: number) => statusOptions.find((i) => i.value === status)?.label || '-'
const statusTagType = (status?: number) => {
  if (status === 1) return 'success'
  if (status === 2) return 'info'
  return 'warning'
}

const getList = async () => {
  loading.value = true
  try {
    const data = await AnnouncementApi.getAnnouncementPage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

const resetQuery = () => {
  queryFormRef.value.resetFields()
  handleQuery()
}

const formRef = ref()
const openForm = (type: string, id?: number) => {
  formRef.value.open(type, id)
}

const handleDelete = async (id: number) => {
  try {
    await message.delConfirm()
    await AnnouncementApi.deleteAnnouncement(id)
    message.success(t('common.delSuccess'))
    await getList()
  } catch {}
}

const handlePublish = async (id: number) => {
  await AnnouncementApi.publishAnnouncement(id)
  message.success('发布成功')
  await getList()
}

const handleTop = async (id: number, isTop: number) => {
  await AnnouncementApi.updateAnnouncementTop(id, isTop)
  message.success('操作成功')
  await getList()
}

onMounted(() => {
  getList()
})
</script>
