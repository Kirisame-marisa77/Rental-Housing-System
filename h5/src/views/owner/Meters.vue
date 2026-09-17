<template>
  <div>
    <el-card>
      <div class="toolbar">
        <span class="title">水电抄表</span>
        <el-button type="primary" @click="openUpload">上传水电读数</el-button>
      </div>
      <el-table :data="list" v-loading="loading">
        <el-table-column prop="id" label="记录ID" width="70" />
        <el-table-column label="类型" width="70">
          <template #default="s">{{ s.row.meterType === 1 ? '电表' : '水表' }}</template>
        </el-table-column>
        <el-table-column prop="currentReading" label="本期读数" width="90" />
        <el-table-column label="谷段读数" width="90">
          <template #default="s">{{ s.row.meterType === 1 ? s.row.valleyReading : '-' }}</template>
        </el-table-column>
        <el-table-column prop="usageAmount" label="用量" width="80" />
        <el-table-column prop="feeAmount" label="费用(元)" width="90" />
        <el-table-column prop="readingDate" label="抄表日期" width="110" />
        <el-table-column label="审核" width="90">
          <template #default="s">
            <el-tag :type="reviewType(s.row.reviewStatus)">{{ reviewLabel(s.row.reviewStatus) }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="uploadVisible" title="上传水电读数" width="520px">
      <el-form :model="form" label-width="110px">
        <el-form-item label="房源">
          <el-select v-model="form.houseId" class="w-full" placeholder="请选择房源">
            <el-option v-for="h in houses" :key="h.id" :label="`${h.communityName} ${h.buildingNo}/${h.roomNo}`" :value="h.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="合同ID">
          <el-input-number v-model="form.contractId" :min="1" class="w-full" placeholder="请输入合同ID" />
        </el-form-item>
        <el-form-item label="抄表类型">
          <el-radio-group v-model="form.meterType">
            <el-radio :value="0">水表</el-radio>
            <el-radio :value="1">电表</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="form.meterType === 1 && isPeakValley ? '峰段读数' : '本期读数'">
          <el-input-number v-model="form.currentReading" :min="0" :precision="2" class="w-full" />
        </el-form-item>
        <el-form-item v-if="form.meterType === 1 && isPeakValley" label="谷段读数">
          <el-input-number v-model="form.valleyReading" :min="0" :precision="2" class="w-full" />
        </el-form-item>
        <el-form-item label="抄表日期">
          <el-date-picker v-model="form.readingDate" type="date" value-format="YYYY-MM-DD" class="w-full" />
        </el-form-item>
        <el-form-item label="表盘截图">
          <el-input v-model="imagesText" type="textarea" placeholder="图片URL，每行一张（水表1张，电表峰谷各1张）" />
        </el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submit">提交（待审核）</el-button>
        <el-button @click="uploadVisible = false">取消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getOwnerMeters, createOwnerMeterReading, getOwnerHouses } from '../../api'

const loading = ref(false)
const list = ref([])
const houses = ref([])

const reviewMap = [
  { value: 0, label: '待审核' },
  { value: 1, label: '已通过' },
  { value: 2, label: '已驳回' }
]
const reviewLabel = (s) => reviewMap.find((i) => i.value === s)?.label || '-'
const reviewType = (s) => (s === 1 ? 'success' : s === 2 ? 'danger' : 'warning')

const getList = async () => {
  loading.value = true
  try {
    const data = await getOwnerMeters({ pageNo: 1, pageSize: 100 })
    list.value = data.list || []
  } finally {
    loading.value = false
  }
}

const uploadVisible = ref(false)
const imagesText = ref('')
const form = reactive({
  houseId: undefined,
  contractId: undefined,
  meterType: 0,
  currentReading: undefined,
  valleyReading: undefined,
  readingDate: '',
  remark: ''
})

// 当前选中的房源是否峰谷电表
const isPeakValley = computed(() => {
  const house = houses.value.find((h) => h.id === form.houseId)
  return house?.electricityBillType === 1
})

const openUpload = async () => {
  Object.assign(form, {
    houseId: undefined,
    contractId: undefined,
    meterType: 0,
    currentReading: undefined,
    valleyReading: undefined,
    readingDate: '',
    remark: ''
  })
  imagesText.value = ''
  const data = await getOwnerHouses({ pageNo: 1, pageSize: 100 })
  houses.value = data.list || []
  uploadVisible.value = true
}

const toJsonArray = (text) => JSON.stringify(text.split(/[\n,，;；]/).map((s) => s.trim()).filter(Boolean))

const submit = async () => {
  if (!form.houseId || !form.contractId || form.currentReading == null || !form.readingDate) {
    ElMessage.warning('请填写房源、合同ID、读数和抄表日期')
    return
  }
  await createOwnerMeterReading({ ...form, images: toJsonArray(imagesText.value) })
  ElMessage.success('提交成功，等待管理员审核')
  uploadVisible.value = false
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
.w-full {
  width: 100%;
}
</style>
