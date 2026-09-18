<template>
  <AppPage title="我的报修">
    <template #actions>
      <el-button type="primary" @click="openReport">我要报修</el-button>
    </template>

    <CardList :data="list" :loading="loading" empty-text="还没有报修记录">
      <template #item="{ row }">
        <InfoCard
          :title="row.repairType || '报修'"
          :subtitle="row.orderNo"
          :tags="tagsOf(row)"
          :fields="fieldsOf(row)"
        >
          <template #actions>
            <el-button v-if="row.status === 2" link type="success" @click="handleConfirm(row.id)">
              确认完成
            </el-button>
          </template>
        </InfoCard>
      </template>
    </CardList>

    <el-dialog v-model="reportVisible" title="我要报修" width="520px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="报修房源">
          <el-select
            v-model="form.houseId"
            class="u-w-full"
            placeholder="请选择要报修的房源"
            :loading="housesLoading"
          >
            <el-option v-for="h in myHouses" :key="h.houseId" :label="h.label" :value="h.houseId" />
          </el-select>
          <div class="u-tip">只列出你正在租住的房源</div>
        </el-form-item>
        <el-form-item label="报修类型">
          <el-select v-model="form.repairType" class="u-w-full">
            <el-option
              v-for="t in ['水电维修', '家电维修', '管道疏通', '门窗维修', '墙面地面', '其他']"
              :key="t"
              :label="t"
              :value="t"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="问题描述">
          <el-input v-model="form.description" type="textarea" placeholder="请描述问题" />
        </el-form-item>
        <el-form-item label="期望上门时间">
          <el-date-picker
            v-model="form.expectedTime"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            class="u-w-full"
          />
        </el-form-item>
        <el-form-item label="优先级">
          <el-radio-group v-model="form.priority">
            <el-radio :value="0">普通</el-radio>
            <el-radio :value="1">紧急</el-radio>
            <el-radio :value="2">特急</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submitReport">提交报修</el-button>
        <el-button @click="reportVisible = false">取消</el-button>
      </template>
    </el-dialog>
  </AppPage>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  confirmTenantRepairOrder,
  createTenantRepairOrder,
  getTenantContracts,
  getTenantRepairs
} from '../../api'
import AppPage from '../../components/AppPage.vue'
import CardList from '../../components/CardList.vue'
import InfoCard from '../../components/InfoCard.vue'

const loading = ref(false)
const list = ref([])

// 报修状态两端不同（业主端把 4 叫「已驳回(退回重做)」），留在本页
const statusMap = [
  { value: 0, label: '待处理' },
  { value: 1, label: '处理中' },
  { value: 2, label: '待验收' },
  { value: 3, label: '已处理' },
  { value: 4, label: '已驳回' }
]
const statusLabel = (s) => statusMap.find((i) => i.value === s)?.label || '-'
const statusType = (s) => (s === 3 ? 'success' : s === 4 ? 'danger' : s === 2 ? 'warning' : 'info')

const tagsOf = (row) => [{ text: statusLabel(row.status), type: statusType(row.status) }]

const fieldsOf = (row) => [
  { label: '问题描述', value: row.description, span: 2, clamp: 2 },
  { label: '维修说明', value: row.repairDescription, span: 2, clamp: 2 }
]

const getList = async () => {
  loading.value = true
  try {
    const data = await getTenantRepairs({ pageNo: 1, pageSize: 100 })
    list.value = data.list || []
  } finally {
    loading.value = false
  }
}

// ===== 报修 =====
const reportVisible = ref(false)
const form = reactive({
  houseId: undefined,
  repairType: '水电维修',
  description: '',
  expectedTime: '',
  priority: 0
})

// 报修房源下拉：数据来自「我租住的房源」，即合同处于 2-生效中 / 3-即将到期 / 4-退租处理中。
// 5-已到期 / 6-已退租 / 7-已取消 的合同不再列出（人已经不住那儿了）。
// 后端还会再校验一次，前端只是让选的时候不会选错。
const ACTIVE_CONTRACT_STATUS = [2, 3, 4]

const housesLoading = ref(false)
const myHouses = ref([])

const contractHouseLabel = (contract) =>
  `${contract.communityName || ''}${contract.buildingNo || ''}号楼${contract.roomNo || ''}`

const loadMyHouses = async () => {
  housesLoading.value = true
  try {
    const data = await getTenantContracts({ pageNo: 1, pageSize: 100 })
    const seen = new Set()
    myHouses.value = (data.list || [])
      .filter((c) => ACTIVE_CONTRACT_STATUS.includes(c.status))
      // 同一房源可能有多份历史合同，按 houseId 去重
      .filter((c) => (seen.has(c.houseId) ? false : seen.add(c.houseId)))
      .map((c) => ({ houseId: c.houseId, label: contractHouseLabel(c) }))
  } finally {
    housesLoading.value = false
  }
}

const openReport = async () => {
  form.houseId = undefined
  form.repairType = '水电维修'
  form.description = ''
  form.expectedTime = ''
  form.priority = 0
  reportVisible.value = true
  await loadMyHouses()
  // 只有一处房源时直接选中，省一步操作
  if (myHouses.value.length === 1) {
    form.houseId = myHouses.value[0].houseId
  }
}

const submitReport = async () => {
  if (!form.houseId || !form.repairType || !form.description) {
    ElMessage.warning('请选择报修房源，并填写报修类型和问题描述')
    return
  }
  await createTenantRepairOrder(form)
  ElMessage.success('报修已提交')
  reportVisible.value = false
  getList()
}

const handleConfirm = async (id) => {
  try {
    await ElMessageBox.confirm('确认维修已完成吗？', '确认完成', { type: 'warning' })
    await confirmTenantRepairOrder(id)
    ElMessage.success('已确认完成')
    getList()
  } catch {}
}

onMounted(getList)
</script>
