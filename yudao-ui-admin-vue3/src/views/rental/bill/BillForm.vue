<template>
  <Dialog v-model="dialogVisible" :title="dialogTitle">
    <el-form ref="formRef" v-loading="formLoading" :model="formData" :rules="formRules" label-width="100px">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="账单编号" prop="billNo">
            <el-input v-model="formData.billNo" placeholder="请输入账单编号" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="账单类型" prop="billType">
            <el-radio-group v-model="formData.billType">
              <el-radio :value="0">首期账单</el-radio>
              <el-radio :value="1">周期账单</el-radio>
            </el-radio-group>
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
          <el-form-item label="缴费状态" prop="payStatus">
            <el-select v-model="formData.payStatus" class="!w-full" clearable placeholder="请选择缴费状态">
              <el-option label="待缴费" :value="0" />
              <el-option label="已缴费" :value="1" />
              <el-option label="已逾期" :value="2" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="周期开始日期" prop="periodStart">
            <el-date-picker
              v-model="formData.periodStart"
              type="date"
              value-format="YYYY-MM-DD"
              class="!w-full"
              placeholder="请选择周期开始日期"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="周期结束日期" prop="periodEnd">
            <el-date-picker
              v-model="formData.periodEnd"
              type="date"
              value-format="YYYY-MM-DD"
              class="!w-full"
              placeholder="请选择周期结束日期"
            />
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
        <el-col :span="12">
          <el-form-item label="本期租金(元)" prop="rentAmount">
            <el-input-number v-model="formData.rentAmount" :min="0" :precision="2" class="!w-full" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="押金(元)" prop="depositAmount">
            <el-input-number v-model="formData.depositAmount" :min="0" :precision="2" class="!w-full" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="本期物业费(元)" prop="propertyFeeAmount">
            <el-input-number v-model="formData.propertyFeeAmount" :min="0" :precision="2" class="!w-full" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="应缴总金额(元)" prop="totalAmount">
            <el-input-number v-model="formData.totalAmount" :min="0" :precision="2" class="!w-full" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="已缴金额(元)" prop="paidAmount">
            <el-input-number v-model="formData.paidAmount" :min="0" :precision="2" class="!w-full" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="缴费方式" prop="payMethod">
            <el-input v-model="formData.payMethod" placeholder="如：微信" />
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
import * as BillApi from '@/api/rental/bill'

defineOptions({ name: 'RentalBillForm' })

const { t } = useI18n()
const message = useMessage()

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const formType = ref('')
const formData = ref<BillApi.RentBillVO>({
  id: undefined,
  billNo: '',
  contractId: undefined,
  tenantUserId: undefined,
  houseId: undefined,
  billType: 1,
  periodStart: undefined,
  periodEnd: undefined,
  rentAmount: undefined,
  depositAmount: undefined,
  propertyFeeAmount: undefined,
  totalAmount: undefined,
  paidAmount: undefined,
  payStatus: 0,
  dueDate: undefined,
  payMethod: ''
})
const formRules = reactive({
  contractId: [{ required: true, message: '合同不能为空', trigger: 'blur' }],
  tenantUserId: [{ required: true, message: '租客不能为空', trigger: 'blur' }],
  houseId: [{ required: true, message: '房源不能为空', trigger: 'blur' }],
  periodStart: [{ required: true, message: '周期开始日期不能为空', trigger: 'blur' }],
  periodEnd: [{ required: true, message: '周期结束日期不能为空', trigger: 'blur' }],
  totalAmount: [{ required: true, message: '应缴总金额不能为空', trigger: 'blur' }],
  dueDate: [{ required: true, message: '缴费截止日期不能为空', trigger: 'blur' }]
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
      formData.value = await BillApi.getRentBill(id)
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
      await BillApi.createRentBill(data)
      message.success(t('common.createSuccess'))
    } else {
      await BillApi.updateRentBill(data)
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
    billType: 1,
    periodStart: undefined,
    periodEnd: undefined,
    rentAmount: undefined,
    depositAmount: undefined,
    propertyFeeAmount: undefined,
    totalAmount: undefined,
    paidAmount: undefined,
    payStatus: 0,
    dueDate: undefined,
    payMethod: ''
  }
  formRef.value?.resetFields()
}
</script>
