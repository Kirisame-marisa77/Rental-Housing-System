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
      <el-table-column align="center" label="结算单号" prop="settlementNo" width="180" show-overflow-tooltip />
      <el-table-column align="center" label="合同 ID" prop="contractId" width="90" />
      <el-table-column align="center" label="房源 ID" prop="houseId" width="90" />
      <el-table-column align="center" label="租客 ID" prop="tenantUserId" width="90" />
      <el-table-column align="center" label="退租类型" width="100">
        <template #default="scope">{{ moveOutTypeLabel(scope.row.moveOutType) }}</template>
      </el-table-column>
      <el-table-column align="center" label="押金(元)" prop="depositAmount" width="100" />
      <el-table-column align="center" label="押金处理" prop="depositHandle" width="130" />
      <el-table-column align="center" label="剩余租金(元)" prop="remainingRent" width="110" />
      <el-table-column align="center" label="物业费欠费(元)" prop="propertyFeeArrears" width="120" />
      <el-table-column align="center" label="水电费欠费(元)" prop="utilityFeeArrears" width="120" />
      <el-table-column align="center" label="维修费(元)" prop="repairFee" width="100" />
      <el-table-column align="center" label="抵扣(元)" prop="deductionAmount" width="100" />
      <el-table-column align="center" label="退还/补缴(元)" prop="refundOrPay" width="120">
        <template #default="scope">
          <span :style="scope.row.refundOrPay >= 0 ? 'color:#67C23A' : 'color:#F56C6C'">{{ scope.row.refundOrPay }}</span>
        </template>
      </el-table-column>
      <el-table-column align="center" label="处理时间" prop="handleTime" :formatter="dateFormatter" width="170" />
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
import * as MoveOutApi from '@/api/rental/moveOut'
import { dateFormatter } from '@/utils/formatTime'

defineOptions({ name: 'RentalSettlementBill' })

const loading = ref(true)
const total = ref(0)
const list = ref([])
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  contractId: undefined,
  moveOutType: undefined
})
const queryFormRef = ref()

const moveOutTypeOptions = [
  { value: 0, label: '到期退租' },
  { value: 1, label: '提前退租' }
]
const moveOutTypeLabel = (type?: number) => moveOutTypeOptions.find((i) => i.value === type)?.label || '-'

const getList = async () => {
  loading.value = true
  try {
    const data = await MoveOutApi.getSettlementBillPage(queryParams)
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
