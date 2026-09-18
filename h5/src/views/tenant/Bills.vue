<template>
  <AppPage title="我的账单">
    <CardList :data="list" :loading="loading" empty-text="暂无账单">
      <template #item="{ row }">
        <InfoCard
          :title="`¥${row.totalAmount ?? 0}`"
          :subtitle="row.billNo"
          :tags="tagsOf(row)"
          :fields="fieldsOf(row)"
        >
          <template #actions>
            <el-button v-if="row.payStatus === 0" link type="success" @click="openPay(row)">
              立即缴费
            </el-button>
          </template>
        </InfoCard>
      </template>
    </CardList>

    <el-dialog v-model="payVisible" title="缴纳账单" width="460px">
      <el-form :model="payForm" label-width="100px">
        <el-form-item label="账单编号">
          <span>{{ currentBill?.billNo }}</span>
        </el-form-item>
        <el-form-item label="应缴总额">
          <span class="u-amount">¥{{ currentBill?.totalAmount }}</span>
        </el-form-item>
        <el-form-item label="缴费方式">
          <el-select v-model="payForm.payMethod" class="u-w-full">
            <el-option v-for="m in ['现金', '微信', '支付宝', '银行转账']" :key="m" :label="m" :value="m" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitPay">确认缴费</el-button>
        <el-button @click="payVisible = false">取消</el-button>
      </template>
    </el-dialog>
  </AppPage>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getTenantRentBills, payTenantRentBill } from '../../api'
import { payStatusLabel, payStatusType } from '../../utils/dict'
import AppPage from '../../components/AppPage.vue'
import CardList from '../../components/CardList.vue'
import InfoCard from '../../components/InfoCard.vue'

const loading = ref(false)
const list = ref([])

const tagsOf = (row) => [
  { text: row.billType === 0 ? '首期账单' : '周期账单', type: row.billType === 0 ? 'primary' : 'info' },
  { text: payStatusLabel(row.payStatus), type: payStatusType(row.payStatus) }
]

const fieldsOf = (row) => [
  { label: '租金', value: `¥${row.rentAmount ?? 0}` },
  { label: '押金', value: `¥${row.depositAmount ?? 0}` },
  { label: '缴费截止', value: row.dueDate },
  { label: '房源', value: `房源 ${row.houseId}` }
]

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
