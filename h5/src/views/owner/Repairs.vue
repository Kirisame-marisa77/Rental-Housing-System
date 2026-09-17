<template>
  <div>
    <el-card>
      <div class="toolbar">
        <span class="title">维修工单</span>
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
        <el-table-column label="操作" width="120">
          <template #default="s">
            <el-button v-if="[0, 1, 4].includes(s.row.status)" link type="primary" @click="openHandle(s.row)">
              处理
            </el-button>
            <span v-else>-</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="handleVisible" title="处理维修（上传证据）" width="520px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="维修说明">
          <el-input v-model="form.repairDescription" type="textarea" placeholder="处理过程和结果" />
        </el-form-item>
        <el-form-item label="处理证据">
          <el-input v-model="evidenceText" type="textarea" placeholder="证据图片URL，每行一张" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submit">提交（待验收）</el-button>
        <el-button @click="handleVisible = false">取消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getOwnerRepairs, handleOwnerRepairOrder } from '../../api'

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
    const data = await getOwnerRepairs({ pageNo: 1, pageSize: 100 })
    list.value = data.list || []
  } finally {
    loading.value = false
  }
}

const handleVisible = ref(false)
const evidenceText = ref('')
const form = reactive({ id: undefined, repairDescription: '' })

const openHandle = (row) => {
  form.id = row.id
  form.repairDescription = ''
  evidenceText.value = ''
  handleVisible.value = true
}

const toJsonArray = (text) => JSON.stringify(text.split(/[\n,，;；]/).map((s) => s.trim()).filter(Boolean))

const submit = async () => {
  await handleOwnerRepairOrder(form.id, form.repairDescription, toJsonArray(evidenceText.value))
  ElMessage.success('提交成功，等待管理员验收')
  handleVisible.value = false
  getList()
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
</style>
