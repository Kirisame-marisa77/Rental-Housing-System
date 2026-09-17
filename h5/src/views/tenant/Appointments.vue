<template>
  <el-card>
    <div class="toolbar">
      <span class="title">我的预约</span>
    </div>
    <el-table :data="list" v-loading="loading">
      <el-table-column label="房源" min-width="180" show-overflow-tooltip>
        <template #default="s">{{ houseText(s.row) }}</template>
      </el-table-column>
      <el-table-column prop="ownerName" label="房东" width="90" />
      <el-table-column prop="ownerPhone" label="房东电话" width="120" />
      <el-table-column prop="appointmentDate" label="预约日期" width="110" />
      <el-table-column label="时间段" width="130">
        <template #default="s">{{ s.row.startTime }}-{{ s.row.endTime }}</template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="s">
          <el-tag :type="statusType(s.row.status)">{{ statusLabel(s.row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="feedback" label="看房反馈" min-width="140" show-overflow-tooltip />
      <el-table-column label="操作" width="110" fixed="right">
        <template #default="s">
          <el-button
            v-if="s.row.status === 0 || s.row.status === 1"
            link
            type="danger"
            @click="handleCancel(s.row)"
          >
            取消预约
          </el-button>
          <span v-else>-</span>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getTenantAppointments, cancelTenantAppointment } from '../../api'

const loading = ref(false)
const list = ref([])

const statusMap = [
  { value: 0, label: '待确认' },
  { value: 1, label: '已确认' },
  { value: 2, label: '已完成' },
  { value: 3, label: '已取消' }
]
const statusLabel = (s) => statusMap.find((i) => i.value === s)?.label || '-'
const statusType = (s) => (s === 2 ? 'success' : s === 1 ? 'primary' : s === 3 ? 'info' : 'warning')

const houseText = (row) =>
  [row.communityName, row.buildingNo && `${row.buildingNo}/${row.roomNo}`].filter(Boolean).join(' ') || `房源 ${row.houseId}`

const getList = async () => {
  loading.value = true
  try {
    const data = await getTenantAppointments({ pageNo: 1, pageSize: 100 })
    list.value = data.list || []
  } finally {
    loading.value = false
  }
}

const handleCancel = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确认取消「${row.appointmentDate} ${row.startTime}-${row.endTime}」的看房预约吗？`,
      '取消预约',
      { type: 'warning' }
    )
    await cancelTenantAppointment(row.id)
    ElMessage.success('已取消预约')
    getList()
  } catch {}
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
