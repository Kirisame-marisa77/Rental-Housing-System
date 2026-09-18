<template>
  <AppPage title="维修工单">
    <CardList :data="list" :loading="loading" empty-text="暂无维修工单">
      <template #item="{ row }">
        <InfoCard
          :title="row.repairType || '维修工单'"
          :subtitle="row.orderNo"
          :tags="tagsOf(row)"
          :fields="fieldsOf(row)"
        >
          <template #actions>
            <el-button
              v-if="[0, 1, 4].includes(row.status)"
              link
              type="primary"
              @click="openHandle(row)"
            >
              处理
            </el-button>
          </template>
        </InfoCard>
      </template>
    </CardList>

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
  </AppPage>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getOwnerRepairs, handleOwnerRepairOrder } from '../../api'
import AppPage from '../../components/AppPage.vue'
import CardList from '../../components/CardList.vue'
import InfoCard from '../../components/InfoCard.vue'

const loading = ref(false)
const list = ref([])

// 报修状态两端文案相同，但与共享字典里其它字典无关，暂留本页
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

const toJsonArray = (text) =>
  JSON.stringify(text.split(/[\n,，;；]/).map((s) => s.trim()).filter(Boolean))

const submit = async () => {
  await handleOwnerRepairOrder(form.id, form.repairDescription, toJsonArray(evidenceText.value))
  ElMessage.success('提交成功，等待管理员验收')
  handleVisible.value = false
  getList()
}

onMounted(getList)
</script>
