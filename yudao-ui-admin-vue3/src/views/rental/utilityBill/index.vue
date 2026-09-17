<template>
  <!-- 搜索工作栏 -->
  <ContentWrap>
    <el-form ref="queryFormRef" :inline="true" :model="queryParams" class="-mb-15px" label-width="68px">
      <el-form-item label="账单编号" prop="billNo">
        <el-input v-model="queryParams.billNo" class="!w-200px" clearable placeholder="请输入账单编号" @keyup.enter="handleQuery" />
      </el-form-item>
      <el-form-item label="房源 ID" prop="houseId">
        <el-input-number v-model="queryParams.houseId" :min="0" class="!w-160px" placeholder="房源 ID" />
      </el-form-item>
      <el-form-item label="缴费状态" prop="payStatus">
        <el-select v-model="queryParams.payStatus" class="!w-160px" clearable placeholder="请选择缴费状态">
          <el-option v-for="item in payStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
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
        <el-button v-hasPermi="['rental:bill:create']" plain type="primary" @click="openForm('create')">
          <Icon class="mr-5px" icon="ep:plus" />
          新增
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <el-table v-loading="loading" :data="list">
      <el-table-column align="center" label="账单编号" prop="billNo" width="170" show-overflow-tooltip />
      <el-table-column align="center" label="合同 ID" prop="contractId" width="90" />
      <el-table-column align="center" label="房源 ID" prop="houseId" width="90" />
      <el-table-column align="center" label="费用类型" width="90">
        <template #default="scope">{{ feeTypeLabel(scope.row.feeType) }}</template>
      </el-table-column>
      <el-table-column align="center" label="水费(元)" prop="waterAmount" width="90" />
      <el-table-column align="center" label="电费(元)" prop="electricityAmount" width="90" />
      <el-table-column align="center" label="应缴(元)" prop="totalAmount" width="100" />
      <el-table-column align="center" label="缴费状态" width="100">
        <template #default="scope">
          <el-tag :type="payStatusTagType(scope.row.payStatus)">{{ payStatusLabel(scope.row.payStatus) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column align="center" label="缴费截止" prop="dueDate" :formatter="dateFormatter2" width="120" />
      <el-table-column align="center" label="操作" width="220">
        <template #default="scope">
          <el-button v-if="scope.row.payStatus !== 1" v-hasPermi="['rental:bill:update']" link type="success" @click="openPay(scope.row.id)">
            缴费登记
          </el-button>
          <el-button v-hasPermi="['rental:bill:update']" link type="primary" @click="openForm('update', scope.row.id)">
            修改
          </el-button>
          <el-button v-hasPermi="['rental:bill:delete']" link type="danger" @click="handleDelete(scope.row.id)">
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
  <UtilityBillForm ref="formRef" @success="getList" />

  <!-- 缴费登记弹窗 -->
  <Dialog v-model="payVisible" title="缴费登记">
    <el-form ref="payFormRef" :model="payData" label-width="100px">
      <el-form-item label="缴费方式" prop="payMethod">
        <el-select v-model="payData.payMethod" class="!w-full" placeholder="请选择缴费方式">
          <el-option label="现金" value="现金" />
          <el-option label="微信" value="微信" />
          <el-option label="支付宝" value="支付宝" />
          <el-option label="银行转账" value="银行转账" />
        </el-select>
      </el-form-item>
      <el-form-item label="收款人 ID" prop="payee">
        <el-input-number v-model="payData.payee" :min="0" class="!w-full" />
      </el-form-item>
      <el-form-item label="交易流水号" prop="transactionNo">
        <el-input v-model="payData.transactionNo" placeholder="选填" />
      </el-form-item>
      <el-form-item label="备注" prop="remark">
        <el-input v-model="payData.remark" placeholder="选填" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button type="primary" @click="submitPay">确 定</el-button>
      <el-button @click="payVisible = false">取 消</el-button>
    </template>
  </Dialog>
</template>

<script lang="ts" setup>
import * as UtilityBillApi from '@/api/rental/utilityBill'
import UtilityBillForm from './UtilityBillForm.vue'
import { dateFormatter, dateFormatter2 } from '@/utils/formatTime'

defineOptions({ name: 'RentalUtilityBill' })

const message = useMessage()
const { t } = useI18n()

const loading = ref(true)
const total = ref(0)
const list = ref([])
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  billNo: '',
  houseId: undefined,
  payStatus: undefined
})
const queryFormRef = ref()

const feeTypeOptions = [
  { value: 0, label: '水费' },
  { value: 1, label: '电费' },
  { value: 2, label: '水+电' }
]
const feeTypeLabel = (type?: number) => feeTypeOptions.find((i) => i.value === type)?.label || '-'

const payStatusOptions = [
  { value: 0, label: '待缴费' },
  { value: 1, label: '已缴费' },
  { value: 2, label: '已逾期' }
]
const payStatusLabel = (status?: number) => payStatusOptions.find((i) => i.value === status)?.label || '-'
const payStatusTagType = (status?: number) => {
  if (status === 1) return 'success'
  if (status === 2) return 'danger'
  return 'warning'
}

const getList = async () => {
  loading.value = true
  try {
    const data = await UtilityBillApi.getUtilityBillPage(queryParams)
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
    await UtilityBillApi.deleteUtilityBill(id)
    message.success(t('common.delSuccess'))
    await getList()
  } catch {}
}

// 缴费登记
const payVisible = ref(false)
const payFormRef = ref()
const payData = reactive({
  id: undefined,
  payMethod: '',
  payee: undefined,
  transactionNo: '',
  remark: ''
})
const openPay = (id: number) => {
  payData.id = id
  payData.payMethod = ''
  payData.payee = undefined
  payData.transactionNo = ''
  payData.remark = ''
  payVisible.value = true
}
const submitPay = async () => {
  if (!payData.payMethod) {
    message.warning('请选择缴费方式')
    return
  }
  await UtilityBillApi.payUtilityBill(payData)
  message.success('缴费成功')
  payVisible.value = false
  await getList()
}

onMounted(() => {
  getList()
})
</script>
