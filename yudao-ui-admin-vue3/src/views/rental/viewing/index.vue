<template>
  <!-- 搜索工作栏 -->
  <ContentWrap>
    <el-form ref="queryFormRef" :inline="true" :model="queryParams" class="-mb-15px" label-width="68px">
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" class="!w-240px" clearable placeholder="请选择状态">
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
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表：只读监管，不提供新增/修改/删除 -->
  <ContentWrap>
    <el-table v-loading="loading" :data="list">
      <el-table-column align="center" label="预约日期" prop="appointmentDate" :formatter="dateFormatter2" width="120" />
      <el-table-column align="center" label="时间段" width="130">
        <template #default="scope">{{ scope.row.startTime }}-{{ scope.row.endTime }}</template>
      </el-table-column>
      <el-table-column align="center" label="小区" prop="communityName" min-width="140" show-overflow-tooltip />
      <el-table-column align="center" label="楼栋/房号" width="110">
        <template #default="scope">{{ scope.row.buildingNo }}/{{ scope.row.roomNo }}</template>
      </el-table-column>
      <el-table-column align="center" label="租客" prop="tenantName" width="90" />
      <el-table-column align="center" label="租客电话" prop="tenantPhone" width="120" />
      <el-table-column align="center" label="房东" prop="ownerName" width="90" />
      <el-table-column align="center" label="房东电话" prop="ownerPhone" width="120" />
      <el-table-column align="center" label="状态" width="100">
        <template #default="scope">
          <el-tag :type="statusTagType(scope.row.status)">{{ statusLabel(scope.row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column align="center" label="看房反馈" prop="feedback" min-width="140" show-overflow-tooltip />
      <el-table-column
        align="center"
        label="创建时间"
        prop="createTime"
        :formatter="dateFormatter"
        width="180"
      />
    </el-table>
    <!-- 分页 -->
    <Pagination
      v-model:limit="queryParams.pageSize"
      v-model:page="queryParams.pageNo"
      :total="total"
      @pagination="getList"
    />
  </ContentWrap>
</template>

<script lang="ts" setup>
import * as ViewingApi from '@/api/rental/viewing'
import { dateFormatter, dateFormatter2 } from '@/utils/formatTime'

defineOptions({ name: 'RentalViewing' })

const loading = ref(true)
const total = ref(0)
const list = ref([])
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  status: undefined
})
const queryFormRef = ref()

const statusOptions = [
  { value: 0, label: '待确认' },
  { value: 1, label: '已确认' },
  { value: 2, label: '已完成' },
  { value: 3, label: '已取消' }
]
const statusLabel = (status?: number) => statusOptions.find((i) => i.value === status)?.label || '-'
const statusTagType = (status?: number) => {
  if (status === 2) return 'success'
  if (status === 1) return 'primary'
  if (status === 3) return 'info'
  return 'warning'
}

const getList = async () => {
  loading.value = true
  try {
    const data = await ViewingApi.getViewingAppointmentPage(queryParams)
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

onMounted(() => {
  getList()
})
</script>
