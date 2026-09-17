<template>
  <!-- 搜索工作栏 -->
  <ContentWrap>
    <el-form ref="queryFormRef" :inline="true" :model="queryParams" class="-mb-15px" label-width="68px">
      <el-form-item label="小区名称" prop="communityName">
        <el-input
          v-model="queryParams.communityName"
          class="!w-240px"
          clearable
          placeholder="请输入小区名称"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="所在区域" prop="area">
        <el-input
          v-model="queryParams.area"
          class="!w-240px"
          clearable
          placeholder="请输入所在区域"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="户型" prop="layout">
        <el-input
          v-model="queryParams.layout"
          class="!w-240px"
          clearable
          placeholder="请输入户型"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" class="!w-240px" clearable placeholder="请选择房源状态">
          <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="审核状态" prop="reviewStatus">
        <el-select v-model="queryParams.reviewStatus" class="!w-240px" clearable placeholder="请选择审核状态">
          <el-option v-for="item in reviewStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
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

  <!-- 列表 -->
  <ContentWrap>
    <el-table v-loading="loading" :data="list">
      <el-table-column align="center" label="房源编号" prop="houseNo" width="170" />
      <el-table-column align="center" label="业主 ID" prop="ownerId" width="80" />
      <el-table-column align="center" label="小区名称" prop="communityName" show-overflow-tooltip />
      <el-table-column align="center" label="区域" prop="area" width="120" />
      <el-table-column align="center" label="楼栋/房号" width="120">
        <template #default="scope">{{ scope.row.buildingNo }}/{{ scope.row.roomNo }}</template>
      </el-table-column>
      <el-table-column align="center" label="户型" prop="layout" width="140" />
      <el-table-column align="center" label="面积(㎡)" prop="squareArea" width="90" />
      <el-table-column align="center" label="月租金(元)" prop="monthlyRent" width="110" />
      <el-table-column align="center" label="状态" width="90">
        <template #default="scope">
          <el-tag :type="statusTagType(scope.row.status)">{{ statusLabel(scope.row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column align="center" label="审核状态" width="90">
        <template #default="scope">
          <el-tag :type="reviewStatusTagType(scope.row.reviewStatus)">{{ reviewStatusLabel(scope.row.reviewStatus) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column align="center" label="操作" width="140">
        <template #default="scope">
          <el-button v-if="scope.row.reviewStatus === 0" v-hasPermi="['rental:house:update']" link type="success" @click="handleApprove(scope.row.id)">
            通过
          </el-button>
          <el-button v-if="scope.row.reviewStatus === 0 || scope.row.reviewStatus === 1" v-hasPermi="['rental:house:update']" link type="danger" @click="openReject(scope.row.id)">
            驳回
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

  <!-- 驳回弹窗 -->
  <Dialog v-model="rejectVisible" title="驳回房源">
    <el-form label-width="80px">
      <el-form-item label="驳回原因">
        <el-input v-model="rejectReason" type="textarea" placeholder="请输入驳回原因" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button type="danger" @click="submitReject">确 定</el-button>
      <el-button @click="rejectVisible = false">取 消</el-button>
    </template>
  </Dialog>
</template>

<script lang="ts" setup>
import * as HouseApi from '@/api/rental/house'

defineOptions({ name: 'RentalHouse' })

const message = useMessage()

const loading = ref(true)
const total = ref(0)
const list = ref([])
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  communityName: '',
  area: '',
  layout: '',
  status: undefined,
  reviewStatus: undefined
})
const queryFormRef = ref()

const statusOptions = [
  { value: 0, label: '下架' },
  { value: 1, label: '上架' },
  { value: 2, label: '已锁定' },
  { value: 3, label: '已出租' }
]
const statusLabel = (status?: number) => statusOptions.find((i) => i.value === status)?.label || '-'
const statusTagType = (status?: number) => {
  if (status === 1) return 'success'
  if (status === 3) return 'info'
  if (status === 2) return 'warning'
  return 'danger'
}
const reviewStatusOptions = [
  { value: 0, label: '待审核' },
  { value: 1, label: '已通过' },
  { value: 2, label: '已驳回' }
]
const reviewStatusLabel = (s?: number) => reviewStatusOptions.find((i) => i.value === s)?.label || '-'
const reviewStatusTagType = (s?: number) => {
  if (s === 1) return 'success'
  if (s === 2) return 'danger'
  return 'warning'
}

const getList = async () => {
  loading.value = true
  try {
    const data = await HouseApi.getHousePage(queryParams)
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

// 通过审批
const handleApprove = async (id: number) => {
  try {
    await message.confirm('确认通过该房源的审批吗？')
    await HouseApi.reviewHouse(id, true)
    message.success('已通过')
    await getList()
  } catch {}
}

// 驳回
const rejectVisible = ref(false)
const rejectId = ref<number>()
const rejectReason = ref('')
const openReject = (id: number) => {
  rejectId.value = id
  rejectReason.value = ''
  rejectVisible.value = true
}
const submitReject = async () => {
  if (!rejectReason.value) {
    message.warning('请填写驳回原因')
    return
  }
  await HouseApi.reviewHouse(rejectId.value!, false, rejectReason.value)
  message.success('已驳回')
  rejectVisible.value = false
  await getList()
}

onMounted(() => {
  getList()
})
</script>
