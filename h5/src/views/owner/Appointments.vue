<template>
  <AppPage title="看房预约">
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

    <CardList :data="list" :loading="loading" empty-text="暂无看房预约">
      <template #item="{ row }">
        <InfoCard
          :title="houseText(row, { style: 'slash', fallback: 'id' })"
          :subtitle="[row.appointmentDate, `${row.startTime}-${row.endTime}`].filter(Boolean).join(' ')"
          :tags="tagsOf(row)"
          :fields="fieldsOf(row)"
        >
          <template #actions>
            <el-button v-if="row.status === 0" link type="success" @click="handleConfirm(row)">
              确认
            </el-button>
            <el-button v-else-if="row.status === 1" link type="primary" @click="handleComplete(row)">
              完成
            </el-button>
          </template>
        </InfoCard>
      </template>
    </CardList>
  </AppPage>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { completeOwnerAppointment, confirmOwnerAppointment, getOwnerAppointments } from '../../api'
import { houseText } from '../../utils/house'
import AppPage from '../../components/AppPage.vue'
import CardList from '../../components/CardList.vue'
import InfoCard from '../../components/InfoCard.vue'

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

const tagsOf = (row) => [{ text: statusLabel(row.status), type: statusType(row.status) }]

// 租客姓名与电话合并成两格，方便直接拨号前辨认
const fieldsOf = (row) => [
  { label: '租客', value: row.tenantName },
  { label: '联系电话', value: row.tenantPhone },
  { label: '看房反馈', value: row.feedback, span: 2, clamp: 2 }
]

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
.filter {
  width: 130px;
}
</style>
