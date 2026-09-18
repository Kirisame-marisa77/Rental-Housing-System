<template>
  <AppPage title="水电抄表">
    <template #actions>
      <el-button type="primary" @click="openUpload">上传水电读数</el-button>
    </template>

    <CardList :data="list" :loading="loading" empty-text="还没有抄表记录">
      <template #item="{ row }">
        <!-- 抄表接口只返回 houseId，没有房源地址，所以副标题用 houseId -->
        <InfoCard
          :title="`${row.meterType === 1 ? '电表' : '水表'} · ${row.readingDate || ''}`"
          :subtitle="row.houseId ? `房源 ${row.houseId}` : ''"
          :tags="tagsOf(row)"
          :fields="fieldsOf(row)"
        />
      </template>
    </CardList>

    <el-dialog v-model="uploadVisible" title="上传水电读数" width="520px">
      <el-form :model="form" label-width="110px">
        <el-form-item label="房源">
          <el-select v-model="form.houseId" class="u-w-full" placeholder="请选择房源">
            <el-option
              v-for="h in houses"
              :key="h.id"
              :label="`${h.communityName} ${h.buildingNo}/${h.roomNo}`"
              :value="h.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="合同ID">
          <el-input-number v-model="form.contractId" :min="1" class="u-w-full" placeholder="请输入合同ID" />
        </el-form-item>
        <el-form-item label="抄表类型">
          <el-radio-group v-model="form.meterType">
            <el-radio :value="0">水表</el-radio>
            <el-radio :value="1">电表</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="form.meterType === 1 && isPeakValley ? '峰段读数' : '本期读数'">
          <el-input-number v-model="form.currentReading" :min="0" :precision="2" class="u-w-full" />
        </el-form-item>
        <el-form-item v-if="form.meterType === 1 && isPeakValley" label="谷段读数">
          <el-input-number v-model="form.valleyReading" :min="0" :precision="2" class="u-w-full" />
        </el-form-item>
        <el-form-item label="抄表日期">
          <el-date-picker v-model="form.readingDate" type="date" value-format="YYYY-MM-DD" class="u-w-full" />
        </el-form-item>
        <el-form-item label="表盘截图">
          <el-input
            v-model="imagesText"
            type="textarea"
            placeholder="图片URL，每行一张（水表1张，电表峰谷各1张）"
          />
        </el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submit">提交（待审核）</el-button>
        <el-button @click="uploadVisible = false">取消</el-button>
      </template>
    </el-dialog>
  </AppPage>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { createOwnerMeterReading, getOwnerHouses, getOwnerMeters } from '../../api'
import { reviewStatusLabel, reviewStatusType } from '../../utils/dict'
import AppPage from '../../components/AppPage.vue'
import CardList from '../../components/CardList.vue'
import InfoCard from '../../components/InfoCard.vue'

const loading = ref(false)
const list = ref([])
const houses = ref([])

const tagsOf = (row) => [
  { text: reviewStatusLabel(row.reviewStatus), type: reviewStatusType(row.reviewStatus) }
]

// 记录 ID 是内部主键，不上卡片；谷段读数只有峰谷电表才有，用 hidden 控制显隐
const fieldsOf = (row) => [
  { label: '本期读数', value: row.currentReading },
  { label: '谷段读数', value: row.valleyReading, hidden: row.meterType !== 1 },
  { label: '用量', value: row.usageAmount },
  { label: '费用', value: `¥${row.feeAmount ?? 0}`, type: 'amount' }
]

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

const toJsonArray = (text) =>
  JSON.stringify(text.split(/[\n,，;；]/).map((s) => s.trim()).filter(Boolean))

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
