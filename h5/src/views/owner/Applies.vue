<template>
  <AppPage title="租房申请">
    <template #actions>
      <el-select
        v-model="query.status"
        placeholder="全部状态"
        clearable
        class="filter"
        @change="getList"
      >
        <el-option v-for="s in statusMap" :key="s.value" :label="s.label" :value="s.value" />
      </el-select>
    </template>

    <CardList :data="list" :loading="loading" empty-text="暂无租房申请">
      <template #item="{ row }">
        <InfoCard
          :title="houseText(row, { style: 'slash', fallback: 'id' })"
          :subtitle="row.applyNo"
          :tags="tagsOf(row)"
          :fields="fieldsOf(row)"
        >
          <template #actions>
            <template v-if="row.status === 0">
              <el-button link type="success" @click="handleApprove(row)">同意</el-button>
              <el-button link type="danger" @click="handleReject(row)">驳回</el-button>
            </template>
          </template>
        </InfoCard>
      </template>
    </CardList>
  </AppPage>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { approveOwnerApply, getOwnerApplies } from '../../api'
import { applyStatusLabel, applyStatusType } from '../../utils/dict'
import { houseText } from '../../utils/house'
import AppPage from '../../components/AppPage.vue'
import CardList from '../../components/CardList.vue'
import InfoCard from '../../components/InfoCard.vue'

const loading = ref(false)
const list = ref([])
const query = reactive({ status: undefined })

const statusMap = [
  { value: 0, label: '待审批' },
  { value: 1, label: '已通过' },
  { value: 2, label: '已驳回' },
  { value: 3, label: '已签约' },
  { value: 4, label: '已失效' }
]

const tagsOf = (row) => [{ text: applyStatusLabel(row.status), type: applyStatusType(row.status) }]

const fieldsOf = (row) => [
  { label: '月租金', value: `¥${row.monthlyRent ?? 0}`, type: 'amount' },
  { label: '押金', value: `¥${row.depositAmount ?? 0}` },
  { label: '付款方式', value: row.paymentMethod },
  { label: '期望入住', value: row.moveInDate },
  { label: '租期', value: row.leaseTerm != null ? `${row.leaseTerm} 个月` : '-' },
  { label: '租客', value: row.tenantName },
  { label: '联系电话', value: row.tenantPhone },
  { label: '租客备注', value: row.tenantRemark, span: 2, clamp: 2 },
  { label: '驳回原因', value: row.rejectReason, span: 2, clamp: 2 }
]

const getList = async () => {
  loading.value = true
  try {
    const data = await getOwnerApplies({ pageNo: 1, pageSize: 100, status: query.status })
    list.value = data.list || []
  } finally {
    loading.value = false
  }
}

const handleApprove = async (row) => {
  try {
    await ElMessageBox.confirm(
      '同意后系统将自动生成租房合同和首期账单（押金 + 按付款方式确定的月数租金）。房源仍会保持上架，直到有租客完成签约并缴费。确认同意该申请吗？',
      '确认同意',
      { type: 'warning' }
    )
    await approveOwnerApply(row.id, true)
    ElMessage.success('已同意，合同与首期账单已自动生成，请到「合同」中查看')
    getList()
  } catch {}
}

const handleReject = async (row) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入驳回原因', '驳回申请', {
      inputValidator: (v) => (v && v.trim() ? true : '请填写驳回原因')
    })
    await approveOwnerApply(row.id, false, value)
    ElMessage.success('已驳回')
    getList()
  } catch {}
}

onMounted(getList)
</script>

<style scoped>
.filter {
  width: 130px;
}
</style>
