<template>
  <!-- 搜索工作栏 -->
  <ContentWrap>
    <el-form ref="queryFormRef" :inline="true" :model="queryParams" class="-mb-15px" label-width="68px">
      <el-form-item label="合同 ID" prop="contractId">
        <el-input-number v-model="queryParams.contractId" :min="0" class="!w-160px" placeholder="合同 ID" />
      </el-form-item>
      <el-form-item label="退租类型" prop="moveOutType">
        <el-select v-model="queryParams.moveOutType" class="!w-160px" clearable placeholder="请选择退租类型">
          <el-option v-for="item in moveOutTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
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
        <el-button v-hasPermi="['rental:moveout:create']" plain type="primary" @click="openForm('create')">
          <Icon class="mr-5px" icon="ep:plus" />
          新增
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <el-table v-loading="loading" :data="list">
      <el-table-column align="center" label="合同 ID" prop="contractId" width="90" />
      <el-table-column align="center" label="租客 ID" prop="tenantUserId" width="90" />
      <el-table-column align="center" label="申请编号" prop="applyNo" width="180" show-overflow-tooltip />
      <el-table-column align="center" label="房源 ID" prop="houseId" width="90" />
      <el-table-column align="center" label="退租类型" width="100">
        <template #default="scope">{{ moveOutTypeLabel(scope.row.moveOutType) }}</template>
      </el-table-column>
      <el-table-column align="center" label="预计退租日期" prop="expectedMoveOutDate" :formatter="dateFormatter2" width="120" />
      <el-table-column align="center" label="验收情况" width="110">
        <template #default="scope">{{ inspectionLabel(scope.row.inspectionResult) }}</template>
      </el-table-column>
      <el-table-column align="center" label="退还/补缴(元)" prop="refundOrPay" width="120" />
      <el-table-column align="center" label="状态" width="90">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : scope.row.status === 2 ? 'danger' : 'warning'">
            {{ statusLabel(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column align="center" label="操作" width="200">
        <template #default="scope">
          <!-- 退租处理已收归业主端（房东做房屋验收 + 费用结算）。管理端此处转为只读：
               不再提供「处理退租」入口，避免与业主端的处理并发产生重复结算。
               后端的 /rental/move-out/confirm 仍然保留并共用同一套 CAS 逻辑，作为运维兜底。 -->
          <el-button v-hasPermi="['rental:moveout:update']" link type="primary" @click="openForm('update', scope.row.id)">
            修改
          </el-button>
          <el-button v-hasPermi="['rental:moveout:delete']" link type="danger" @click="handleDelete(scope.row.id)">
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

  <!-- 表单弹窗：添加/修改 -->
  <MoveOutForm ref="formRef" @success="getList" />
</template>

<script lang="ts" setup>
import * as MoveOutApi from '@/api/rental/moveOut'
import MoveOutForm from './MoveOutForm.vue'
import { dateFormatter, dateFormatter2 } from '@/utils/formatTime'

defineOptions({ name: 'RentalMoveOut' })

const message = useMessage()
const { t } = useI18n()

const loading = ref(true)
const total = ref(0)
const list = ref([])
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  contractId: undefined,
  moveOutType: undefined,
  status: undefined
})
const queryFormRef = ref()

const moveOutTypeOptions = [
  { value: 0, label: '到期退租' },
  { value: 1, label: '提前退租' }
]
const moveOutTypeLabel = (type?: number) => moveOutTypeOptions.find((i) => i.value === type)?.label || '-'

const statusOptions = [
  { value: 0, label: '待处理' },
  { value: 1, label: '已处理' },
  { value: 2, label: '已驳回' }
]
const statusLabel = (status?: number) => statusOptions.find((i) => i.value === status)?.label || '-'

const inspectionOptions = [
  { value: 0, label: '通过' },
  { value: 1, label: '轻微损坏' },
  { value: 2, label: '严重损坏' }
]
const inspectionLabel = (value?: number) =>
  value == null ? '-' : inspectionOptions.find((i) => i.value === value)?.label || '-'

const getList = async () => {
  loading.value = true
  try {
    const data = await MoveOutApi.getMoveOutApplicationPage(queryParams)
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
    await MoveOutApi.deleteMoveOutApplication(id)
    message.success(t('common.delSuccess'))
    await getList()
  } catch {}
}

onMounted(() => {
  getList()
})
</script>
