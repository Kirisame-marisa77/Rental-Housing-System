<template>
  <Dialog v-model="dialogVisible" :title="dialogTitle">
    <el-form ref="formRef" v-loading="formLoading" :model="formData" :rules="formRules" label-width="110px">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="合同 ID" prop="contractId">
            <el-input-number v-model="formData.contractId" :min="1" class="!w-full" />
          </el-form-item>
        </el-col>
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
          <el-form-item label="退租类型" prop="moveOutType">
            <el-radio-group v-model="formData.moveOutType">
              <el-radio :value="0">到期退租</el-radio>
              <el-radio :value="1">提前退租</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="预计退租日期" prop="expectedMoveOutDate">
            <el-date-picker
              v-model="formData.expectedMoveOutDate"
              type="date"
              value-format="YYYY-MM-DD"
              class="!w-full"
              placeholder="请选择预计退租日期"
            />
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="退租原因" prop="moveOutReason">
            <el-input v-model="formData.moveOutReason" type="textarea" placeholder="退租原因说明" />
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
import * as MoveOutApi from '@/api/rental/moveOut'

defineOptions({ name: 'RentalMoveOutForm' })

const { t } = useI18n()
const message = useMessage()

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const formType = ref('')
const formData = ref<MoveOutApi.MoveOutApplicationVO>({
  id: undefined,
  contractId: undefined,
  tenantUserId: undefined,
  houseId: undefined,
  moveOutType: 0,
  moveOutReason: '',
  expectedMoveOutDate: undefined
})
const formRules = reactive({
  contractId: [{ required: true, message: '合同不能为空', trigger: 'blur' }],
  tenantUserId: [{ required: true, message: '租客不能为空', trigger: 'blur' }],
  houseId: [{ required: true, message: '房源不能为空', trigger: 'blur' }],
  moveOutType: [{ required: true, message: '退租类型不能为空', trigger: 'blur' }],
  expectedMoveOutDate: [{ required: true, message: '预计退租日期不能为空', trigger: 'blur' }]
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
      formData.value = await MoveOutApi.getMoveOutApplication(id)
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
      await MoveOutApi.createMoveOutApplication(data)
      message.success(t('common.createSuccess'))
    } else {
      await MoveOutApi.updateMoveOutApplication(data)
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
    contractId: undefined,
    tenantUserId: undefined,
    houseId: undefined,
    moveOutType: 0,
    moveOutReason: '',
    expectedMoveOutDate: undefined
  }
  formRef.value?.resetFields()
}
</script>
