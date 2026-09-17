<template>
  <el-card>
    <div class="toolbar">
      <span class="title">租房申请</span>
      <el-select v-model="query.status" placeholder="全部状态" clearable class="filter" @change="getList">
        <el-option v-for="s in statusMap" :key="s.value" :label="s.label" :value="s.value" />
      </el-select>
    </div>
    <el-table :data="list" v-loading="loading">
      <el-table-column prop="applyNo" label="申请编号" width="170" show-overflow-tooltip />
      <el-table-column label="房源" min-width="180" show-overflow-tooltip>
        <template #default="s">{{ houseText(s.row) }}</template>
      </el-table-column>
      <el-table-column prop="tenantName" label="租客" width="90" />
      <el-table-column prop="tenantPhone" label="联系电话" width="120" />
      <el-table-column prop="moveInDate" label="期望入住" width="110" />
      <el-table-column prop="leaseTerm" label="租期(月)" width="80" />
      <el-table-column prop="paymentMethod" label="付款方式" width="100" />
      <el-table-column prop="monthlyRent" label="月租金" width="90" />
      <el-table-column prop="depositAmount" label="押金" width="90" />
      <el-table-column prop="tenantRemark" label="备注" min-width="120" show-overflow-tooltip />
      <el-table-column label="状态" width="90">
        <template #default="s">
          <el-tag :type="statusType(s.row.status)">{{ statusLabel(s.row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="rejectReason" label="驳回原因" width="120" show-overflow-tooltip />
      <el-table-column label="操作" width="130" fixed="right">
        <template #default="s">
          <template v-if="s.row.status === 0">
            <el-button link type="success" @click="handleApprove(s.row)">同意</el-button>
            <el-button link type="danger" @click="handleReject(s.row)">驳回</el-button>
          </template>
          <span v-else>-</span>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getOwnerApplies, approveOwnerApply } from '../../api'

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
const statusLabel = (s) => statusMap.find((i) => i.value === s)?.label || '-'
const statusType = (s) => (s === 1 || s === 3 ? 'success' : s === 2 ? 'danger' : s === 4 ? 'info' : 'warning')

const houseText = (row) =>
  [row.communityName, row.buildingNo && `${row.buildingNo}/${row.roomNo}`].filter(Boolean).join(' ') || `房源 ${row.houseId}`

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
      '同意后系统将自动生成租房合同和首期账单（押金 + 首月租金）。房源仍会保持上架，直到有租客完成签约并缴费。确认同意该申请吗？',
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
.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}
.title {
  font-size: 16px;
  font-weight: 600;
}
.filter {
  width: 140px;
}
</style>
