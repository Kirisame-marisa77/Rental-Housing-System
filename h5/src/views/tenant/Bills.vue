<template>
  <el-card>
    <div class="toolbar">
      <span class="title">我的账单</span>
    </div>
    <el-table :data="list" v-loading="loading">
      <el-table-column prop="billNo" label="账单编号" width="200" show-overflow-tooltip />
      <el-table-column label="房源" min-width="180" show-overflow-tooltip>
        <template #default="s">房源 {{ s.row.houseId }}</template>
      </el-table-column>
      <el-table-column label="类型" width="90">
        <template #default="s">{{ s.row.billType === 0 ? '首期账单' : '周期账单' }}</template>
      </el-table-column>
      <el-table-column prop="rentAmount" label="租金" width="90" />
      <el-table-column prop="depositAmount" label="押金" width="90" />
      <el-table-column prop="totalAmount" label="应缴总额" width="100" />
      <el-table-column prop="dueDate" label="缴费截止" width="110" />
      <el-table-column label="状态" width="90">
        <template #default="s">
          <el-tag :type="payType(s.row.payStatus)">{{ payLabel(s.row.payStatus) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="110">
        <template #default="s">
          <el-button v-if="s.row.payStatus === 0" link type="success" @click="openPay(s.row)">立即缴费</el-button>
          <span v-else>-</span>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="payVisible" title="缴纳账单" width="460px">
      <el-form :model="payForm" label-width="100px">
        <el-form-item label="账单编号">
          <span>{{ currentBill?.billNo }}</span>
        </el-form-item>
        <el-form-item label="应缴总额">
          <span class="amount">¥{{ currentBill?.totalAmount }}</span>
        </el-form-item>
        <el-form-item label="缴费方式">
          <el-select v-model="payForm.payMethod" class="w-full">
            <el-option v-for="m in ['现金', '微信', '支付宝', '银行转账']" :key="m" :label="m" :value="m" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitPay">确认缴费</el-button>
        <el-button @click="payVisible = false">取消</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getTenantRentBills, payTenantRentBill } from '../../api'

const loading = ref(false)
const list = ref([])

const payMap = [
  { value: 0, label: '待缴费' },
  { value: 1, label: '已缴费' },
  { value: 2, label: '已逾期' }
]
const payLabel = (s) => payMap.find((i) => i.value === s)?.label || '-'
const payType = (s) => (s === 1 ? 'success' : s === 2 ? 'danger' : 'warning')

const getList = async () => {
  loading.value = true
  try {
    const data = await getTenantRentBills({ pageNo: 1, pageSize: 100 })
    list.value = data.list || []
  } finally {
    loading.value = false
  }
}

const payVisible = ref(false)
const currentBill = ref(null)
const payForm = reactive({ payMethod: '微信' })

const openPay = (bill) => {
  currentBill.value = bill
  payForm.payMethod = '微信'
  payVisible.value = true
}

const submitPay = async () => {
  await payTenantRentBill(currentBill.value.id, payForm.payMethod)
  ElMessage.success('缴费成功，双方签署后合同即生效')
  payVisible.value = false
  getList()
}

onMounted(getList)
</script>

<style scoped>
.toolbar {
  margin-bottom: 12px;
}
.title {
  font-size: 16px;
  font-weight: 600;
}
.amount {
  color: #f56c6c;
  font-weight: 600;
}
.w-full {
  width: 100%;
}
</style>
