<template>
  <div>
    <el-card>
      <div class="toolbar">
        <span class="title">我的房源</span>
        <el-button type="primary" @click="openUpload">上传房源</el-button>
      </div>
      <el-table :data="list" v-loading="loading">
        <el-table-column prop="houseNo" label="房源编号" width="170" />
        <el-table-column prop="communityName" label="小区" show-overflow-tooltip />
        <el-table-column label="楼栋/房号" width="110">
          <template #default="s">{{ s.row.buildingNo }}/{{ s.row.roomNo }}</template>
        </el-table-column>
        <el-table-column prop="layout" label="户型" width="130" show-overflow-tooltip />
        <el-table-column prop="monthlyRent" label="月租金" width="90" />
        <el-table-column label="状态" width="90">
          <template #default="s">
            <el-tag :type="statusType(s.row.status)">{{ statusLabel(s.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="审核" width="90">
          <template #default="s">
            <el-tag :type="reviewType(s.row.reviewStatus)">{{ reviewLabel(s.row.reviewStatus) }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="uploadVisible" title="上传房源" width="640px" top="5vh">
      <el-form :model="form" label-width="110px">
        <el-row :gutter="12">
          <el-col :span="12"><el-form-item label="小区名称"><el-input v-model="form.communityName" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="所在区域"><el-input v-model="form.area" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="楼栋号"><el-input v-model="form.buildingNo" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="房号"><el-input v-model="form.roomNo" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="户型"><el-input v-model="form.layout" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="面积(㎡)"><el-input-number v-model="form.squareArea" :min="0" :precision="2" class="w-full" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="朝向"><el-input v-model="form.orientation" /></el-form-item></el-col>
          <el-col :span="12">
            <el-form-item label="装修">
              <el-select v-model="form.decoration" class="w-full">
                <el-option v-for="d in ['精装', '简装', '毛坯']" :key="d" :label="d" :value="d" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12"><el-form-item label="月租金(元)"><el-input-number v-model="form.monthlyRent" :min="0" :precision="2" class="w-full" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="押金(元)"><el-input-number v-model="form.deposit" :min="0" :precision="2" class="w-full" /></el-form-item></el-col>
          <el-col :span="24"><el-form-item label="房源描述"><el-input v-model="form.description" type="textarea" /></el-form-item></el-col>
        </el-row>

        <el-divider content-position="left">水电计价</el-divider>
        <el-form-item label="水费计价">
          <el-radio-group v-model="form.waterBillType">
            <el-radio :value="0">统一单价</el-radio>
            <el-radio :value="1">三档梯度</el-radio>
          </el-radio-group>
        </el-form-item>
        <template v-if="form.waterBillType === 0">
          <el-form-item label="水费单价(元/吨)"><el-input-number v-model="form.waterUnitPrice" :min="0" :precision="2" class="w-full" /></el-form-item>
        </template>
        <template v-else>
          <el-row :gutter="12">
            <el-col :span="12"><el-form-item label="档一上限(吨)"><el-input-number v-model="form.waterTier1Limit" :min="0" :precision="2" class="w-full" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="档一单价"><el-input-number v-model="form.waterTier1Price" :min="0" :precision="2" class="w-full" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="档二上限(吨)"><el-input-number v-model="form.waterTier2Limit" :min="0" :precision="2" class="w-full" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="档二单价"><el-input-number v-model="form.waterTier2Price" :min="0" :precision="2" class="w-full" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="档三单价"><el-input-number v-model="form.waterTier3Price" :min="0" :precision="2" class="w-full" /></el-form-item></el-col>
          </el-row>
        </template>
        <el-form-item label="电费计价">
          <el-radio-group v-model="form.electricityBillType">
            <el-radio :value="0">统一单价</el-radio>
            <el-radio :value="1">峰谷两价</el-radio>
          </el-radio-group>
        </el-form-item>
        <template v-if="form.electricityBillType === 0">
          <el-form-item label="电费单价(元/度)"><el-input-number v-model="form.electricityUnitPrice" :min="0" :precision="2" class="w-full" /></el-form-item>
        </template>
        <template v-else>
          <el-row :gutter="12">
            <el-col :span="12"><el-form-item label="电费峰价(元/度)"><el-input-number v-model="form.electricityPeakPrice" :min="0" :precision="2" class="w-full" /></el-form-item></el-col>
            <el-col :span="12"><el-form-item label="电费谷价(元/度)"><el-input-number v-model="form.electricityValleyPrice" :min="0" :precision="2" class="w-full" /></el-form-item></el-col>
          </el-row>
        </template>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="submit">提交（待审核）</el-button>
        <el-button @click="uploadVisible = false">取消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getOwnerHouses, createOwnerHouse } from '../../api'

const loading = ref(false)
const list = ref([])

const statusMap = [
  { value: 0, label: '下架' },
  { value: 1, label: '上架' },
  { value: 2, label: '已锁定' },
  { value: 3, label: '已出租' }
]
const statusLabel = (s) => statusMap.find((i) => i.value === s)?.label || '-'
const statusType = (s) => (s === 1 ? 'success' : s === 3 ? 'info' : s === 2 ? 'warning' : 'danger')

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
    const data = await getOwnerHouses({ pageNo: 1, pageSize: 100 })
    list.value = data.list || []
  } finally {
    loading.value = false
  }
}

const uploadVisible = ref(false)
const form = reactive({
  communityName: '',
  area: '',
  buildingNo: '',
  roomNo: '',
  layout: '',
  squareArea: undefined,
  orientation: '',
  decoration: '精装',
  monthlyRent: undefined,
  deposit: undefined,
  facilities: '',
  description: '',
  waterBillType: 0,
  waterUnitPrice: undefined,
  waterTier1Limit: undefined,
  waterTier1Price: undefined,
  waterTier2Limit: undefined,
  waterTier2Price: undefined,
  waterTier3Price: undefined,
  electricityBillType: 1,
  electricityUnitPrice: undefined,
  electricityPeakPrice: undefined,
  electricityValleyPrice: undefined
})

const openUpload = () => {
  Object.assign(form, {
    communityName: '',
    area: '',
    buildingNo: '',
    roomNo: '',
    layout: '',
    squareArea: undefined,
    orientation: '',
    decoration: '精装',
    monthlyRent: undefined,
    deposit: undefined,
    facilities: '',
    description: '',
    waterBillType: 0,
    waterUnitPrice: undefined,
    waterTier1Limit: undefined,
    waterTier1Price: undefined,
    waterTier2Limit: undefined,
    waterTier2Price: undefined,
    waterTier3Price: undefined,
    electricityBillType: 1,
    electricityUnitPrice: undefined,
    electricityPeakPrice: undefined,
    electricityValleyPrice: undefined
  })
  uploadVisible.value = true
}

const submit = async () => {
  if (!form.communityName || !form.area || !form.buildingNo || !form.roomNo || !form.layout || form.squareArea == null || form.monthlyRent == null) {
    ElMessage.warning('请填写完整的基本信息')
    return
  }
  await createOwnerHouse(form)
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
