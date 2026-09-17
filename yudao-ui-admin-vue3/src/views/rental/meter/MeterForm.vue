<template>
  <Dialog v-model="dialogVisible" :title="dialogTitle">
    <el-form ref="formRef" v-loading="formLoading" :model="formData" :rules="formRules" label-width="100px">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="房源 ID" prop="houseId">
            <el-input-number v-model="formData.houseId" :min="1" class="!w-full" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="合同 ID" prop="contractId">
            <el-input-number v-model="formData.contractId" :min="1" class="!w-full" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="抄表类型" prop="meterType">
            <el-radio-group v-model="formData.meterType">
              <el-radio :value="0">水表</el-radio>
              <el-radio :value="1">电表</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="本期读数" prop="currentReading">
            <el-input-number v-model="formData.currentReading" :min="0" :precision="2" class="!w-full" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="单价(选填)" prop="unitPrice">
            <el-input-number v-model="formData.unitPrice" :min="0" :precision="2" class="!w-full" placeholder="留空取抄表配置" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="抄表日期" prop="readingDate">
            <el-date-picker
              v-model="formData.readingDate"
              type="date"
              value-format="YYYY-MM-DD"
              class="!w-full"
              placeholder="请选择抄表日期"
            />
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="备注" prop="remark">
            <el-input v-model="formData.remark" type="textarea" placeholder="备注（异常说明等）" />
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
import * as MeterApi from '@/api/rental/meter'

defineOptions({ name: 'RentalMeterForm' })

const { t } = useI18n()
const message = useMessage()

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const formType = ref('')
const formData = ref<MeterApi.MeterReadingVO>({
  id: undefined,
  houseId: undefined,
  contractId: undefined,
  meterType: 0,
  currentReading: undefined,
  unitPrice: undefined,
  readingDate: undefined,
  remark: ''
})
const formRules = reactive({
  houseId: [{ required: true, message: '房源不能为空', trigger: 'blur' }],
  contractId: [{ required: true, message: '合同不能为空', trigger: 'blur' }],
  meterType: [{ required: true, message: '抄表类型不能为空', trigger: 'blur' }],
  currentReading: [{ required: true, message: '本期读数不能为空', trigger: 'blur' }],
  readingDate: [{ required: true, message: '抄表日期不能为空', trigger: 'blur' }]
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
      formData.value = await MeterApi.getMeterReading(id)
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
    if (formType.value === 'create') {
      await MeterApi.createMeterReading(data)
      message.success(t('common.createSuccess'))
    } else {
      await MeterApi.updateMeterReading(data)
      message.success(t('common.updateSuccess'))
    }
    dialogVisible.value = false
    emit('success')
  } finally {
    formLoading.value = false
  }
}

const resetForm = () => {
  formData.value = {
    id: undefined,
    houseId: undefined,
    contractId: undefined,
    meterType: 0,
    currentReading: undefined,
    unitPrice: undefined,
    readingDate: undefined,
    remark: ''
  }
  formRef.value?.resetFields()
}
</script>
