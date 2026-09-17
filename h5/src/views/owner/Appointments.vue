<template>
  <el-card>
    <div class="toolbar">
      <span class="title">看房预约</span>
      <el-select v-model="query.status" placeholder="全部状态" clearable class="filter" @change="getList">
        <el-option v-for="s in statusMap" :key="s.value" :label="s.label" :value="s.value" />
      </el-select>
    </div>
    <el-table :data="list" v-loading="loading">
      <el-table-column prop="appointmentDate" label="预约日期" width="110" />
      <el-table-column label="时间段" width="130">
        <template #default="s">{{ s.row.startTime }}-{{ s.row.endTime }}</template>
      </el-table-column>
      <el-table-column label="房源" min-width="180" show-overflow-tooltip>
        <template #default="s">{{ houseText(s.row) }}</template>
      </el-table-column>
      <el-table-column prop="tenantName" label="租客" width="90" />
      <el-table-column prop="tenantPhone" label="联系电话" width="120" />
      <el-table-column label="状态" width="90">
        <template #default="s">
          <el-tag :type="statusType(s.row.status)">{{ statusLabel(s.row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="feedback" label="看房反馈" min-width="140" show-overflow-tooltip />
      <el-table-column label="操作" width="110" fixed="right">
        <template #default="s">
          <el-button v-if="s.row.status === 0" link type="success" @click="handleConfirm(s.row)">确认</el-button>
          <el-button v-else-if="s.row.status === 1" link type="primary" @click="handleComplete(s.row)">完成</el-button>
          <span v-else>-</span>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getOwnerAppointments, confirmOwnerAppointment, completeOwnerAppointment } from '../../api'

const loading = ref(false)
const list = ref([])
const query = reactive({ status: undefined })

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
    const data = await getOwnerAppointments({ pageNo: 1, pageSize: 100, status: query.status })
    list.value = data.list || []
  } finally {
    loading.value = false
  }
}

const handleConfirm = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确认租客「${row.tenantName}」在 ${row.appointmentDate} ${row.startTime}-${row.endTime} 的看房预约吗？确认后租客将看到「已确认」。`,
      '确认看房预约',
      { type: 'warning' }
    )
    await confirmOwnerAppointment(row.id)
    ElMessage.success('已确认')
    getList()
  } catch {}
}

const handleComplete = async (row) => {
  try {
    // 反馈选填，所以校验恒真，允许直接确定
    const { value } = await ElMessageBox.prompt('请输入看房反馈（选填）', '完成看房', {
      inputValidator: () => true
    })
    await completeOwnerAppointment(row.id, value)
    ElMessage.success('已完成')
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
