<template>
  <Dialog v-model="dialogVisible" :title="dialogTitle">
    <el-form ref="formRef" v-loading="formLoading" :model="formData" :rules="formRules" label-width="90px">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="小区名称" prop="communityName">
            <el-input v-model="formData.communityName" placeholder="请输入小区名称" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="所在区域" prop="area">
            <el-input v-model="formData.area" placeholder="请输入所在区域" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="楼栋号" prop="buildingNo">
            <el-input v-model="formData.buildingNo" placeholder="请输入楼栋号" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="房号" prop="roomNo">
            <el-input v-model="formData.roomNo" placeholder="请输入房号" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="户型" prop="layout">
            <el-input v-model="formData.layout" placeholder="如：两室一厅一卫" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="建筑面积(㎡)" prop="squareArea">
            <el-input-number v-model="formData.squareArea" :min="0" :precision="2" class="!w-full" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="月租金(元)" prop="monthlyRent">
            <el-input-number v-model="formData.monthlyRent" :min="0" :precision="2" class="!w-full" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="押金(元)" prop="deposit">
            <el-input-number v-model="formData.deposit" :min="0" :precision="2" class="!w-full" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="朝向" prop="orientation">
            <el-input v-model="formData.orientation" placeholder="如：南北通透" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="装修情况" prop="decoration">
            <el-select v-model="formData.decoration" class="!w-full" clearable placeholder="请选择装修情况">
              <el-option label="精装" value="精装" />
              <el-option label="简装" value="简装" />
              <el-option label="毛坯" value="毛坯" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="所在楼层" prop="floor">
            <el-input-number v-model="formData.floor" :min="0" class="!w-full" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="总楼层" prop="totalFloor">
            <el-input-number v-model="formData.totalFloor" :min="0" class="!w-full" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="状态" prop="status">
            <el-radio-group v-model="formData.status">
              <el-radio :value="0">下架</el-radio>
              <el-radio :value="1">上架</el-radio>
              <el-radio :value="2">已锁定</el-radio>
              <el-radio :value="3">已出租</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="房源描述" prop="description">
            <el-input v-model="formData.description" type="textarea" placeholder="请输入房源描述/周边环境" />
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="实景图">
            <UploadImgs v-model="realityImages" :limit="10" :file-size="5" :file-type="['image/jpeg', 'image/png', 'image/webp']" />
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="户型图">
            <UploadImgs v-model="layoutImages" :limit="10" :file-size="5" :file-type="['image/jpeg', 'image/png', 'image/webp']" />
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
    <template #footer>
      <el-button :disabled="formLoading" type="primary" @click="submitForm">确 定</el-button>
      <el-button @click="dialogVisible = false">取 消</el-button>
    </template>
  </Dialog>
</template>

<script lang="ts" setup>
import * as HouseApi from '@/api/rental/house'

defineOptions({ name: 'RentalHouseForm' })

const { t } = useI18n()
const message = useMessage()

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const formType = ref('')
// 图片列表独立维护，避免污染 HouseVO
const realityImages = ref<string[]>([])
const layoutImages = ref<string[]>([])
const formData = ref<HouseApi.HouseVO>({
  id: undefined,
  houseNo: '',
  communityName: '',
  area: '',
  buildingNo: '',
  roomNo: '',
  layout: '',
  squareArea: undefined,
  orientation: '',
  floor: undefined,
  totalFloor: undefined,
  decoration: '',
  monthlyRent: undefined,
  deposit: undefined,
  facilities: '',
  status: 1,
  description: ''
})
const formRules = reactive({
  communityName: [{ required: true, message: '小区名称不能为空', trigger: 'blur' }],
  area: [{ required: true, message: '所在区域不能为空', trigger: 'blur' }],
  buildingNo: [{ required: true, message: '楼栋号不能为空', trigger: 'blur' }],
  roomNo: [{ required: true, message: '房号不能为空', trigger: 'blur' }],
  layout: [{ required: true, message: '户型不能为空', trigger: 'blur' }],
  squareArea: [{ required: true, message: '建筑面积不能为空', trigger: 'blur' }],
  monthlyRent: [{ required: true, message: '月租金不能为空', trigger: 'blur' }]
})
const formRef = ref()

const open = async (type: string, id?: number) => {
  dialogVisible.value = true
  dialogTitle.value = t('action.' + type)
  formType.value = type
  resetForm()
  if (id) {
    formLoading.value = true
    try {
      formData.value = await HouseApi.getHouse(id)
      const images = await HouseApi.getHouseImageList(id)
      realityImages.value = images.filter((i) => i.imageType === 0).map((i) => i.imageUrl)
      layoutImages.value = images.filter((i) => i.imageType === 1).map((i) => i.imageUrl)
    } finally {
      formLoading.value = false
    }
  }
}
defineExpose({ open })

const emit = defineEmits(['success'])
const submitForm = async () => {
  if (!formRef) return
  const valid = await formRef.value.validate()
  if (!valid) return
  formLoading.value = true
  try {
    const data = formData.value
    let houseId: number
    if (formType.value === 'create') {
      houseId = await HouseApi.createHouse(data)
      message.success(t('common.createSuccess'))
    } else {
      await HouseApi.updateHouse(data)
      houseId = data.id!
      message.success(t('common.updateSuccess'))
    }
    // 保存图片（整体覆盖）
    await HouseApi.saveHouseImage({
      houseId,
      realityImages: realityImages.value,
      layoutImages: layoutImages.value
    })
    dialogVisible.value = false
    emit('success')
  } finally {
    formLoading.value = false
  }
}

const resetForm = () => {
  formData.value = {
    id: undefined,
    houseNo: '',
    communityName: '',
    area: '',
    buildingNo: '',
    roomNo: '',
    layout: '',
    squareArea: undefined,
    orientation: '',
    floor: undefined,
    totalFloor: undefined,
    decoration: '',
    monthlyRent: undefined,
    deposit: undefined,
    facilities: '',
    status: 1,
    description: ''
  }
  realityImages.value = []
  layoutImages.value = []
  formRef.value?.resetFields()
}
</script>
