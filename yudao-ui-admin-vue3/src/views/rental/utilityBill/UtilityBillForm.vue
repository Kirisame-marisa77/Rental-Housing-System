<template>
  <Dialog v-model="dialogVisible" :title="dialogTitle">
    <el-form ref="formRef" v-loading="formLoading" :model="formData" :rules="formRules" label-width="110px">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="账单编号" prop="billNo">
            <el-input v-model="formData.billNo" placeholder="留空自动生成" />
          </el-form-item>
        </el-col>
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
          <el-form-item label="费用类型" prop="feeType">
            <el-select v-model="formData.feeType" class="!w-full">
              <el-option label="水费" :value="0" />
              <el-option label="电费" :value="1" />
              <el-option label="水+电" :value="2" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="水费金额(元)" prop="waterAmount">
            <el-input-number v-model="formData.waterAmount" :min="0" :precision="2" class="!w-full" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="电费金额(元)" prop="electricityAmount">
            <el-input-number v-model="formData.electricityAmount" :min="0" :precision="2" class="!w-full" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="应缴总金额(元)" prop="totalAmount">
            <el-input-number v-model="formData.totalAmount" :min="0" :precision="2" class="!w-full" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="缴费状态" prop="payStatus">
            <el-select v-model="formData.payStatus" class="!w-full" clearable placeholder="请选择缴费状态">
              <el-option label="待缴费" :value="0" />
              <el-option label="已缴费" :value="1" />
              <el-option label="已逾期" :value="2" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="缴费截止日期" prop="dueDate">
            <el-date-picker
              v-model="formData.dueDate"
              type="date"
              value-format="YYYY-MM-DD"
              class="!w-full"
              placeholder="请选择缴费截止日期"
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
import * as UtilityBillApi from '@/api/rental/utilityBill'

defineOptions({ name: 'RentalUtilityBillForm' })

const { t } = useI18n()
const message = useMessage()

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const formType = ref('')
const formData = ref<UtilityBillApi.UtilityBillVO>({
  id: undefined,
  billNo: '',
  contractId: undefined,
  tenantUserId: undefined,
  houseId: undefined,
  feeType: 0,
  waterAmount: undefined,
  electricityAmount: undefined,
  totalAmount: undefined,
  payStatus: 0,
  dueDate: undefined
})
const formRules = reactive({
  contractId: [{ required: true, message: '合同不能为空', trigger: 'blur' }],
  tenantUserId: [{ required: true, message: '租客不能为空', trigger: 'blur' }],
  houseId: [{ required: true, message: '房源不能为空', trigger: 'blur' }],
  totalAmount: [{ required: true, message: '应缴总金额不能为空', trigger: 'blur' }]
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
      formData.value = await UtilityBillApi.getUtilityBill(id)
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
      await UtilityBillApi.createUtilityBill(data)
      message.success(t('common.createSuccess'))
    } else {
      await UtilityBillApi.updateUtilityBill(data)
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
    billNo: '',
    contractId: undefined,
    tenantUserId: undefined,
    houseId: undefined,
    feeType: 0,
    waterAmount: undefined,
    electricityAmount: undefined,
    totalAmount: undefined,
    payStatus: 0,
    dueDate: undefined
  }
  formRef.value?.resetFields()
}
</script>
