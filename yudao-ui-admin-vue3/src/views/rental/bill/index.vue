<template>
  <!-- 搜索工作栏 -->
  <ContentWrap>
    <el-form ref="queryFormRef" :inline="true" :model="queryParams" class="-mb-15px" label-width="68px">
      <el-form-item label="账单编号" prop="billNo">
        <el-input
          v-model="queryParams.billNo"
          class="!w-240px"
          clearable
          placeholder="请输入账单编号"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="缴费状态" prop="payStatus">
        <el-select v-model="queryParams.payStatus" class="!w-240px" clearable placeholder="请选择缴费状态">
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
      <el-table-column align="center" label="租客 ID" prop="tenantUserId" width="90" />
      <el-table-column align="center" label="账单类型" width="100">
        <template #default="scope">{{ billTypeLabel(scope.row.billType) }}</template>
      </el-table-column>
      <el-table-column align="center" label="租金(元)" prop="rentAmount" width="100" />
      <el-table-column align="center" label="押金(元)" prop="depositAmount" width="100" />
      <el-table-column align="center" label="应缴金额(元)" prop="totalAmount" width="110" />
      <el-table-column align="center" label="已缴金额(元)" prop="paidAmount" width="110" />
      <el-table-column align="center" label="缴费状态" width="100">
        <template #default="scope">
          <el-tag :type="payStatusTagType(scope.row.payStatus)">{{ payStatusLabel(scope.row.payStatus) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column align="center" label="缴费截止" prop="dueDate" :formatter="dateFormatter2" width="120" />
      <el-table-column align="center" label="创建时间" prop="createTime" :formatter="dateFormatter" width="180" />
      <el-table-column align="center" label="操作" width="200">
        <template #default="scope">
          <el-button
            v-if="scope.row.payStatus === 0"
            v-hasPermi="['rental:bill:update']"
            link
            type="success"
            @click="openPay(scope.row)"
          >
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

  <!-- 缴费登记弹窗 -->
  <Dialog v-model="payVisible" title="缴费登记" width="460px">
    <el-form :model="payForm" label-width="100px">
      <el-form-item label="账单编号">
        <span>{{ payRow?.billNo }}</span>
      </el-form-item>
      <el-form-item label="应缴总额">
        <span class="text-red-500 font-bold">¥{{ payRow?.totalAmount }}</span>
      </el-form-item>
      <el-form-item label="缴费方式">
        <el-select v-model="payForm.payMethod" class="!w-full">
          <el-option v-for="m in payMethods" :key="m" :label="m" :value="m" />
        </el-select>
      </el-form-item>
      <el-form-item label="流水号">
        <el-input v-model="payForm.transactionNo" placeholder="选填" />
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="payForm.remark" placeholder="选填" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button type="primary" @click="submitPay">确认缴费</el-button>
      <el-button @click="payVisible = false">取消</el-button>
    </template>
  </Dialog>

  <!-- 表单弹窗：添加/修改 -->
  <BillForm ref="formRef" @success="getList" />
</template>

<script lang="ts" setup>
import * as BillApi from '@/api/rental/bill'
import BillForm from './BillForm.vue'
import { dateFormatter, dateFormatter2 } from '@/utils/formatTime'

defineOptions({ name: 'RentalBill' })

const message = useMessage()
const { t } = useI18n()

const loading = ref(true)
const total = ref(0)
const list = ref([])
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  billNo: '',
  payStatus: undefined
})
const queryFormRef = ref()

const billTypeOptions = [
  { value: 0, label: '首期账单' },
  { value: 1, label: '周期账单' }
]
const billTypeLabel = (type?: number) => billTypeOptions.find((i) => i.value === type)?.label || '-'

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
    const data = await BillApi.getRentBillPage(queryParams)
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
    await BillApi.deleteRentBill(id)
    message.success(t('common.delSuccess'))
    await getList()
  } catch {}
}

const payMethods = ['现金', '微信', '支付宝', '银行转账']
const payVisible = ref(false)
const payRow = ref<any>()
const payForm = reactive({ payMethod: '微信', transactionNo: '', remark: '' })

const openPay = (row: any) => {
  payRow.value = row
  payForm.payMethod = '微信'
  payForm.transactionNo = ''
  payForm.remark = ''
  payVisible.value = true
}

const submitPay = async () => {
  await BillApi.payRentBill(payRow.value.id, payForm.payMethod, payForm.transactionNo, payForm.remark)
  message.success('缴费登记成功')
  payVisible.value = false
  await getList()
}

onMounted(() => {
  getList()
})
</script>
