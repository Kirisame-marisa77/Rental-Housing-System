<template>
  <el-card>
    <div class="toolbar">
      <span class="title">我的申请</span>
    </div>
    <el-table :data="list" v-loading="loading">
      <el-table-column prop="applyNo" label="申请编号" width="170" show-overflow-tooltip />
      <el-table-column label="房源" min-width="180" show-overflow-tooltip>
        <template #default="s">{{ houseText(s.row) }}</template>
      </el-table-column>
      <el-table-column prop="ownerName" label="房东" width="90" />
      <el-table-column prop="ownerPhone" label="房东电话" width="120" />
      <el-table-column prop="moveInDate" label="期望入住" width="110" />
      <el-table-column prop="leaseTerm" label="租期(月)" width="90" />
      <el-table-column prop="paymentMethod" label="付款方式" width="100" />
      <el-table-column prop="monthlyRent" label="月租金" width="90" />
      <el-table-column label="状态" width="90">
        <template #default="s">
          <el-tag :type="statusType(s.row.status)">{{ statusLabel(s.row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="驳回/失效原因" min-width="140" show-overflow-tooltip>
        <template #default="s">{{ s.row.rejectReason || s.row.timeoutReason }}</template>
      </el-table-column>
      <el-table-column label="操作" width="110" fixed="right">
        <template #default="s">
          <el-button v-if="s.row.contractId" link type="primary" @click="router.push('/tenant/contracts')">查看合同</el-button>
          <span v-else>-</span>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getTenantApplies } from '../../api'

const router = useRouter()
const loading = ref(false)
const list = ref([])

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
    const data = await getTenantApplies({ pageNo: 1, pageSize: 100 })
    list.value = data.list || []
  } finally {
    loading.value = false
  }
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
</style>
