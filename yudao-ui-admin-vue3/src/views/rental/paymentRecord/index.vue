<template>
  <!-- 搜索工作栏 -->
  <ContentWrap>
    <el-form ref="queryFormRef" :inline="true" :model="queryParams" class="-mb-15px" label-width="68px">
      <el-form-item label="账单类型" prop="billType">
        <el-select v-model="queryParams.billType" class="!w-180px" clearable placeholder="请选择账单类型">
          <el-option v-for="item in billTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="账单 ID" prop="billId">
        <el-input-number v-model="queryParams.billId" :min="0" class="!w-160px" placeholder="账单 ID" />
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
      <el-table-column align="center" label="账单类型" width="120">
        <template #default="scope">{{ billTypeLabel(scope.row.billType) }}</template>
      </el-table-column>
      <el-table-column align="center" label="账单 ID" prop="billId" width="90" />
      <el-table-column align="center" label="缴费金额(元)" prop="payAmount" width="110" />
      <el-table-column align="center" label="缴费方式" prop="payMethod" width="100" />
      <el-table-column align="center" label="缴费时间" prop="payTime" :formatter="dateFormatter" width="180" />
      <el-table-column align="center" label="收款人 ID" prop="payee" width="90" />
      <el-table-column align="center" label="交易流水号" prop="transactionNo" width="160" show-overflow-tooltip />
      <el-table-column align="center" label="凭证编号" prop="voucherNo" width="140" show-overflow-tooltip />
      <el-table-column align="center" label="备注" prop="remark" min-width="140" show-overflow-tooltip />
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
import * as PaymentRecordApi from '@/api/rental/paymentRecord'
import { dateFormatter } from '@/utils/formatTime'

defineOptions({ name: 'RentalPaymentRecord' })

const loading = ref(true)
const total = ref(0)
const list = ref([])
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  billType: undefined,
  billId: undefined
})
const queryFormRef = ref()

const billTypeOptions = [
  { value: 0, label: '租金/物业费' },
  { value: 1, label: '水电费' },
  { value: 2, label: '退租结算' }
]
const billTypeLabel = (type?: number) => billTypeOptions.find((i) => i.value === type)?.label || '-'

const getList = async () => {
  loading.value = true
  try {
    const data = await PaymentRecordApi.getPaymentRecordPage(queryParams)
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
