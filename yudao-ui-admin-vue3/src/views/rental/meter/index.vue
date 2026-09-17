<template>
  <!-- 搜索工作栏 -->
  <ContentWrap>
    <el-form ref="queryFormRef" :inline="true" :model="queryParams" class="-mb-15px" label-width="68px">
      <el-form-item label="房源 ID" prop="houseId">
        <el-input-number v-model="queryParams.houseId" :min="0" class="!w-160px" placeholder="房源 ID" />
      </el-form-item>
      <el-form-item label="合同 ID" prop="contractId">
        <el-input-number v-model="queryParams.contractId" :min="0" class="!w-160px" placeholder="合同 ID" />
      </el-form-item>
      <el-form-item label="抄表类型" prop="meterType">
        <el-select v-model="queryParams.meterType" class="!w-160px" clearable placeholder="请选择抄表类型">
          <el-option v-for="item in meterTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="审核状态" prop="reviewStatus">
        <el-select v-model="queryParams.reviewStatus" class="!w-160px" clearable placeholder="请选择审核状态">
          <el-option v-for="item in reviewStatusOptions" :key="item.value" :label="item.label" :value="item.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery">
          <Icon class="mr-5px" icon="ep:search" />
          搜索
        </el-button>
        <el-button @click="resetQuery">
          <Icon class="mr-5px" icon="ep:refresh" />
          重置
        </el-button>
        <el-button v-hasPermi="['rental:meter:create']" plain type="primary" @click="openForm('create')">
          <Icon class="mr-5px" icon="ep:plus" />
          新增
        </el-button>
        <el-button v-hasPermi="['rental:meter:update']" plain @click="openSettings">
          <Icon class="mr-5px" icon="ep:setting" />
          抄表配置
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <el-table v-loading="loading" :data="list">
      <el-table-column align="center" label="房源 ID" prop="houseId" width="90" />
      <el-table-column align="center" label="业主 ID" prop="ownerId" width="80" />
      <el-table-column align="center" label="合同 ID" prop="contractId" width="90" />
      <el-table-column align="center" label="抄表类型" width="90">
        <template #default="scope">{{ meterTypeLabel(scope.row.meterType) }}</template>
      </el-table-column>
      <el-table-column align="center" label="上期读数" prop="lastReading" width="100" />
      <el-table-column align="center" label="本期读数" prop="currentReading" width="100" />
      <el-table-column align="center" label="谷段读数" width="90">
        <template #default="scope">{{ scope.row.meterType === 1 ? scope.row.valleyReading : '-' }}</template>
      </el-table-column>
      <el-table-column align="center" label="用量" prop="usageAmount" width="90" />
      <el-table-column align="center" label="单价" prop="unitPrice" width="80" />
      <el-table-column align="center" label="费用(元)" prop="feeAmount" width="100" />
      <el-table-column align="center" label="抄表日期" prop="readingDate" :formatter="dateFormatter2" width="120" />
      <el-table-column align="center" label="账单 ID" prop="billId" width="90" />
      <el-table-column align="center" label="状态" width="90">
        <template #default="scope">
          <el-tag :type="statusTagType(scope.row.status)">{{ statusLabel(scope.row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column align="center" label="审核状态" width="90">
        <template #default="scope">
          <el-tag :type="reviewStatusTagType(scope.row.reviewStatus)">{{ reviewStatusLabel(scope.row.reviewStatus) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column align="center" label="操作" width="260">
        <template #default="scope">
          <el-button v-if="scope.row.reviewStatus === 0" v-hasPermi="['rental:meter:update']" link type="success" @click="openReview(scope.row.id)">
            审核
          </el-button>
          <el-button v-else-if="!scope.row.billId" v-hasPermi="['rental:meter:update']" link type="success" @click="handleConfirm(scope.row.id)">
            生成账单
          </el-button>
          <el-button v-hasPermi="['rental:meter:update']" link type="primary" @click="openForm('update', scope.row.id)">
            修改
          </el-button>
          <el-button v-hasPermi="['rental:meter:delete']" link type="danger" @click="handleDelete(scope.row.id)">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <!-- 分页 -->
    <Pagination
      v-model:limit="queryParams.pageSize"
      v-model:page="queryParams.pageNo"
      :total="total"
      @pagination="getList"
    />
  </ContentWrap>

  <!-- 表单弹窗：添加/修改 -->
  <MeterForm ref="formRef" @success="getList" />

  <!-- 审核弹窗 -->
  <Dialog v-model="reviewVisible" title="审核抄表">
    <el-form label-width="80px">
      <el-form-item label="审核结果">
        <el-select v-model="reviewData.pass" class="!w-full">
          <el-option :value="true" label="通过（生成账单）" />
          <el-option :value="false" label="驳回" />
        </el-select>
      </el-form-item>
      <el-form-item v-if="!reviewData.pass" label="驳回原因">
        <el-input v-model="reviewData.reason" type="textarea" placeholder="请输入驳回原因" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button type="primary" @click="submitReview">确 定</el-button>
      <el-button @click="reviewVisible = false">取 消</el-button>
    </template>
  </Dialog>

  <!-- 抄表配置弹窗 -->
  <Dialog v-model="settingsVisible" title="抄表配置">
    <el-form :model="settingsData" label-width="120px">
      <el-form-item label="水费单价(元/吨)" prop="waterPrice">
        <el-input-number v-model="settingsData.waterPrice" :min="0" :precision="2" class="!w-full" />
      </el-form-item>
      <el-form-item label="电费单价(元/度)" prop="electricityPrice">
        <el-input-number v-model="settingsData.electricityPrice" :min="0" :precision="2" class="!w-full" />
      </el-form-item>
      <el-form-item label="抄表周期" prop="meterPeriod">
        <el-select v-model="settingsData.meterPeriod" class="!w-full">
          <el-option label="按月" value="monthly" />
          <el-option label="按双月" value="bimonthly" />
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button type="primary" @click="submitSettings">确 定</el-button>
      <el-button @click="settingsVisible = false">取 消</el-button>
    </template>
  </Dialog>
</template>

<script lang="ts" setup>
import * as MeterApi from '@/api/rental/meter'
import MeterForm from './MeterForm.vue'
import { dateFormatter2 } from '@/utils/formatTime'

defineOptions({ name: 'RentalMeter' })

const message = useMessage()
const { t } = useI18n()

const loading = ref(true)
const total = ref(0)
const list = ref([])
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  houseId: undefined,
  contractId: undefined,
  meterType: undefined,
  reviewStatus: undefined
})
const queryFormRef = ref()

const meterTypeOptions = [
  { value: 0, label: '水表' },
  { value: 1, label: '电表' }
]
const meterTypeLabel = (type?: number) => meterTypeOptions.find((i) => i.value === type)?.label || '-'

const statusOptions = [
  { value: 0, label: '正常' },
  { value: 1, label: '异常' },
  { value: 2, label: '已作废' }
]
const statusLabel = (status?: number) => statusOptions.find((i) => i.value === status)?.label || '-'
const statusTagType = (status?: number) => {
  if (status === 1) return 'danger'
  if (status === 2) return 'info'
  return 'success'
}
const reviewStatusOptions = [
  { value: 0, label: '待审核' },
  { value: 1, label: '已通过' },
  { value: 2, label: '已驳回' }
]
const reviewStatusLabel = (s?: number) => reviewStatusOptions.find((i) => i.value === s)?.label || '-'
const reviewStatusTagType = (s?: number) => {
  if (s === 1) return 'success'
  if (s === 2) return 'danger'
  return 'warning'
}

const getList = async () => {
  loading.value = true
  try {
    const data = await MeterApi.getMeterReadingPage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

const resetQuery = () => {
  queryFormRef.value.resetFields()
  handleQuery()
}

const formRef = ref()
const openForm = (type: string, id?: number) => {
  formRef.value.open(type, id)
}

const handleDelete = async (id: number) => {
  try {
    await message.delConfirm()
    await MeterApi.deleteMeterReading(id)
    message.success(t('common.delSuccess'))
    await getList()
  } catch {}
}

const handleConfirm = async (id: number) => {
  try {
    await message.confirm('确认该抄表数据并生成水电费账单吗？')
    await MeterApi.confirmMeterReading(id)
    message.success('生成成功')
    await getList()
  } catch {}
}

// 审核
const reviewVisible = ref(false)
const reviewData = reactive<{ id?: number; pass: boolean; reason: string }>({ pass: true, reason: '' })
const openReview = (id: number) => {
  reviewData.id = id
  reviewData.pass = true
  reviewData.reason = ''
  reviewVisible.value = true
}
const submitReview = async () => {
  if (!reviewData.pass && !reviewData.reason) {
    message.warning('请填写驳回原因')
    return
  }
  await MeterApi.reviewMeterReading(reviewData.id!, reviewData.pass, reviewData.reason)
  message.success('审核完成')
  reviewVisible.value = false
  await getList()
}

// 抄表配置
const settingsVisible = ref(false)
const settingsData = reactive<MeterApi.MeterSettingsVO>({
  waterPrice: 0,
  electricityPrice: 0,
  meterPeriod: 'monthly'
})
const openSettings = async () => {
  const data = await MeterApi.getMeterSettings()
  settingsData.waterPrice = data.waterPrice ?? 0
  settingsData.electricityPrice = data.electricityPrice ?? 0
  settingsData.meterPeriod = data.meterPeriod || 'monthly'
  settingsVisible.value = true
}
const submitSettings = async () => {
  await MeterApi.updateMeterSettings(settingsData)
  message.success(t('common.updateSuccess'))
  settingsVisible.value = false
}

onMounted(() => {
  getList()
})
</script>
