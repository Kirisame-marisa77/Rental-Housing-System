<template>
  <Dialog v-model="dialogVisible" :title="dialogTitle">
    <el-form ref="formRef" v-loading="formLoading" :model="formData" :rules="formRules" label-width="110px">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="租客 ID" prop="tenantUserId">
            <el-input-number v-model="formData.tenantUserId" :min="1" class="!w-full" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="房源 ID" prop="houseId">
            <el-input-number v-model="formData.houseId" :min="1" class="!w-full" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="报修类型" prop="repairType">
            <el-select v-model="formData.repairType" class="!w-full" placeholder="请选择报修类型">
              <el-option label="水电维修" value="水电维修" />
              <el-option label="家电维修" value="家电维修" />
              <el-option label="管道疏通" value="管道疏通" />
              <el-option label="门窗维修" value="门窗维修" />
              <el-option label="墙面地面" value="墙面地面" />
              <el-option label="其他" value="其他" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="优先级" prop="priority">
            <el-radio-group v-model="formData.priority">
              <el-radio :value="0">普通</el-radio>
              <el-radio :value="1">紧急</el-radio>
              <el-radio :value="2">特急</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="问题描述" prop="description">
            <el-input v-model="formData.description" type="textarea" placeholder="详细描述问题情况" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="期望上门时间" prop="expectedTime">
            <el-date-picker
              v-model="formData.expectedTime"
              type="datetime"
              value-format="YYYY-MM-DD HH:mm:ss"
              class="!w-full"
              placeholder="请选择期望上门时间"
            />
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
import * as RepairOrderApi from '@/api/rental/repairOrder'

defineOptions({ name: 'RentalRepairOrderForm' })

const { t } = useI18n()
const message = useMessage()

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const formType = ref('')
const formData = ref<RepairOrderApi.RepairOrderVO>({
  id: undefined,
  tenantUserId: undefined,
  houseId: undefined,
  repairType: '',
  description: '',
  expectedTime: undefined,
  priority: 0
})
const formRules = reactive({
  tenantUserId: [{ required: true, message: '租客不能为空', trigger: 'blur' }],
  houseId: [{ required: true, message: '房源不能为空', trigger: 'blur' }],
  repairType: [{ required: true, message: '报修类型不能为空', trigger: 'blur' }],
  description: [{ required: true, message: '问题描述不能为空', trigger: 'blur' }]
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
      formData.value = await RepairOrderApi.getRepairOrder(id)
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
      await RepairOrderApi.createRepairOrder(data)
      message.success(t('common.createSuccess'))
    } else {
      await RepairOrderApi.updateRepairOrder(data)
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
    tenantUserId: undefined,
    houseId: undefined,
    repairType: '',
    description: '',
    expectedTime: undefined,
    priority: 0
  }
  formRef.value?.resetFields()
}
</script>
