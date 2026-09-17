<template>
  <div>
    <el-card>
      <div class="toolbar">
        <span class="title">我的报修</span>
        <el-button type="primary" @click="openReport">我要报修</el-button>
      </div>
      <el-table :data="list" v-loading="loading">
        <el-table-column prop="orderNo" label="工单编号" width="170" show-overflow-tooltip />
        <el-table-column prop="repairType" label="报修类型" width="100" />
        <el-table-column prop="description" label="问题描述" show-overflow-tooltip />
        <el-table-column label="状态" width="90">
          <template #default="s">
            <el-tag :type="statusType(s.row.status)">{{ statusLabel(s.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="repairDescription" label="维修说明" show-overflow-tooltip />
        <el-table-column label="操作" width="110">
          <template #default="s">
            <el-button v-if="s.row.status === 2" link type="success" @click="handleConfirm(s.row.id)">确认完成</el-button>
            <span v-else>-</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="reportVisible" title="我要报修" width="520px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="房源 ID">
          <el-input-number v-model="form.houseId" :min="1" class="w-full" placeholder="请输入房源 ID（见我的合同）" />
        </el-form-item>
        <el-form-item label="报修类型">
          <el-select v-model="form.repairType" class="w-full">
            <el-option v-for="t in ['水电维修', '家电维修', '管道疏通', '门窗维修', '墙面地面', '其他']" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>
        <el-form-item label="问题描述">
          <el-input v-model="form.description" type="textarea" placeholder="请描述问题" />
        </el-form-item>
        <el-form-item label="期望上门时间">
          <el-date-picker v-model="form.expectedTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" class="w-full" />
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getTenantRepairs, createTenantRepairOrder, confirmTenantRepairOrder } from '../../api'

const loading = ref(false)
const list = ref([])

const statusMap = [
  { value: 0, label: '待处理' },
  { value: 1, label: '处理中' },
  { value: 2, label: '待验收' },
  { value: 3, label: '已处理' },
  { value: 4, label: '已驳回' }
]
const statusLabel = (s) => statusMap.find((i) => i.value === s)?.label || '-'
const statusType = (s) => (s === 3 ? 'success' : s === 4 ? 'danger' : s === 2 ? 'warning' : 'info')

const getList = async () => {
  loading.value = true
  try {
    const data = await getTenantRepairs({ pageNo: 1, pageSize: 100 })
    list.value = data.list || []
  } finally {
    loading.value = false
  }
}

const reportVisible = ref(false)
const form = reactive({
  houseId: undefined,
  repairType: '水电维修',
  description: '',
  expectedTime: '',
  priority: 0
})

const openReport = () => {
  form.houseId = undefined
  form.repairType = '水电维修'
  form.description = ''
  form.expectedTime = ''
  form.priority = 0
  reportVisible.value = true
}

const submitReport = async () => {
  if (!form.houseId || !form.repairType || !form.description) {
    ElMessage.warning('请填写房源、报修类型和问题描述')
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
.w-full {
  width: 100%;
}
</style>
