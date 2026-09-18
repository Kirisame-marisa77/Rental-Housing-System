<template>
  <AppPage title="我的预约">
    <CardList :data="list" :loading="loading" empty-text="还没有看房预约">
      <template #item="{ row }">
        <InfoCard
          :title="houseText(row, { style: 'slash', fallback: 'id' })"
          :subtitle="[row.appointmentDate, `${row.startTime}-${row.endTime}`].filter(Boolean).join(' ')"
          :tags="tagsOf(row)"
          :fields="fieldsOf(row)"
        >
          <template #actions>
            <el-button
              v-if="row.status === 0 || row.status === 1"
              link
              type="danger"
              @click="handleCancel(row)"
            >
              取消预约
            </el-button>
          </template>
        </InfoCard>
      </template>
    </CardList>
  </AppPage>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { cancelTenantAppointment, getTenantAppointments } from '../../api'
import { houseText } from '../../utils/house'
import AppPage from '../../components/AppPage.vue'
import CardList from '../../components/CardList.vue'
import InfoCard from '../../components/InfoCard.vue'

const loading = ref(false)
const list = ref([])

// 预约状态字典两端不同（业主端没有「取消」这一项的文案差异），留在本页
const statusMap = [
  { value: 0, label: '待确认' },
  { value: 1, label: '已确认' },
  { value: 2, label: '已完成' },
  { value: 3, label: '已取消' }
]
const statusLabel = (s) => statusMap.find((i) => i.value === s)?.label || '-'
const statusType = (s) => (s === 2 ? 'success' : s === 1 ? 'primary' : s === 3 ? 'info' : 'warning')

const tagsOf = (row) => [{ text: statusLabel(row.status), type: statusType(row.status) }]

const fieldsOf = (row) => [
  { label: '房东', value: row.ownerName },
  { label: '房东电话', value: row.ownerPhone },
  { label: '看房反馈', value: row.feedback, span: 2, clamp: 2 }
]

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
